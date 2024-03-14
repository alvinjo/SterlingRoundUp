package com.alvin.roundup.service;

import com.alvin.common.utils.DateUtils;
import com.alvin.roundup.domain.RoundUpJob;
import com.alvin.roundup.domain.RoundUpJobRequest;
import com.alvin.roundup.domain.RoundUpMessage;
import com.alvin.roundup.repo.RoundUpRepo;
import com.alvin.starling.domain.FeedItems;
import com.alvin.starling.service.SavingsService;
import com.alvin.starling.service.TransactionFeedService;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static com.alvin.roundup.service.RoundUpJobListener.ROUND_UP_JOB_DLQ;
import static com.alvin.roundup.service.RoundUpJobListener.ROUND_UP_JOB_QUEUE;

@Service
@NoArgsConstructor
public class RoundUpService {

    private RoundUpRepo repo;

    private SavingsService savingsService;

    private TransactionFeedService transactionFeedService;

    private JmsMessagingTemplate jmsTemplate;

    @Autowired
    public RoundUpService(RoundUpRepo repo, SavingsService savingsService, TransactionFeedService transactionFeedService, JmsMessagingTemplate jmsTemplate) {
        this.repo = repo;
        this.savingsService = savingsService;
        this.transactionFeedService = transactionFeedService;
        this.jmsTemplate = jmsTemplate;
    }

    public RoundUpJob getRoundUpJob(String jobId) {
        try {
            LocalDate.parse(jobId);
            return repo.findById(jobId).orElseThrow(); //TODO what is IEEE rest response for not found? what does repo return if not found?
        } catch (Exception e){
            return null;
        }
    }

    public Set<LocalDate> createRoundUpJobs(RoundUpJobRequest jobRequest) throws IOException, InterruptedException {
        validateRoundUpJobRequest(jobRequest);

        LocalDateTime startDateTime = LocalDate.parse(jobRequest.getStartDate()).atStartOfDay();
        LocalDateTime endDateTime = LocalDate.parse(jobRequest.getEndDate()).atTime(LocalTime.MAX);

        //split range into days of format yyyy-MM-dd
        List<LocalDate> dates = DateUtils.generateDateList(LocalDate.parse(jobRequest.getStartDate()), LocalDate.parse(jobRequest.getEndDate()));

        //create and initialise new jobs map for calculating our round up totals
        Map<LocalDate, RoundUpJob> jobMap = initialiseNewJobsMap(dates, jobRequest.getAccountId(), jobRequest.getSavingsGoalId());

        //if there are no new jobs to process, return with the job ids
        if(jobMap.isEmpty()) {
          return new HashSet<>(dates);
        }

        //perform transactions fetch on entire range
        FeedItems feedItems = transactionFeedService.fetchTransactionsWithinRange(startDateTime, endDateTime, jobRequest.getAccountId());

        //calculate round up for each transaction and update job map
        updateJobMapWithTransactions(jobMap, feedItems);

        //create message for each job and send to their appropriate queues
        createAndSendMessages(jobMap);

        //save the jobs to db
        saveJobs(jobMap.values().stream().toList());

        return jobMap.keySet();
    }

    private void validateRoundUpJobRequest(RoundUpJobRequest request) {
        if (!DateUtils.validDate(request.getStartDate()) || !DateUtils.validDate(request.getEndDate())) {
            throw new IllegalArgumentException("Invalid date range");
        }
        if (!StringUtils.hasText(request.getAccountId()) || !StringUtils.hasText(request.getSavingsGoalId())) {
            throw new IllegalArgumentException("Account ID and savings goal ID are required");
        }

        LocalDateTime startDateTime = LocalDateTime.of(LocalDate.parse(request.getStartDate()), LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(LocalDate.parse(request.getEndDate()), LocalTime.MAX);
        if(startDateTime.isAfter(endDateTime)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        var savingGoalsResponse = savingsService.getSavingsGoalsList(request.getAccountId());
        if(savingGoalsResponse == null || CollectionUtils.isEmpty(savingGoalsResponse.getSavingsGoals())) {
            throw new RuntimeException("Account doesn't have any savings goals setup");
        }

        boolean savingsGoalIdNotFound = savingGoalsResponse.getSavingsGoals().stream().noneMatch(savingsGoal -> request.getSavingsGoalId().equals(savingsGoal.getSavingsGoalUid()));
        if(savingsGoalIdNotFound) {
            throw new IllegalArgumentException("Could not find the provided savings goal in the account");
        }
    }

    private Map<LocalDate, RoundUpJob> initialiseNewJobsMap(List<LocalDate> dates, String accountId, String categoryId){
        Map<LocalDate, RoundUpJob> jobMap = new HashMap<>();
        dates.forEach(date -> {
            //check if we have already processed this job
            var existingJob = repo.findById(date.toString());
            if(existingJob.isEmpty() || RoundUpJob.JobStatus.COMPLETE != existingJob.get().getStatus()) {
                jobMap.put(date, new RoundUpJob(date.toString(), accountId, categoryId).setStatus(RoundUpJob.JobStatus.PROCESSING));
            }
        });
        return jobMap;
    }

    private void updateJobMapWithTransactions(Map<LocalDate, RoundUpJob> jobMap, FeedItems feedItems) {
        List<FeedItems.FeedItem> expenses = feedItems.getFeedItems().stream()
                .filter(item -> FeedItems.FeedItem.Direction.OUT == item.getDirection()).toList();

        for (FeedItems.FeedItem transaction : expenses) {
            LocalDate transactionDate = LocalDateTime.parse(transaction.getTransactionTime().replace("Z", "")).toLocalDate();

            if(FeedItems.FeedItem.Status.SETTLED == transaction.getStatus()) {
                String currency = transaction.getSourceAmount().getCurrency();
                long transactionSaving = roundUp(transaction.getAmount().getMinorUnits());

                jobMap.get(transactionDate).setCurrency(currency).addValueToRoundUp(transactionSaving);
            } else {
                jobMap.get(transactionDate).setHasUnsettledTransactions(true);
            }
        }
    }

    private long roundUp(long value) {
        long roundedUpValue = (value + 99) / 100 * 100;
        return roundedUpValue - value;
    }

    private void createAndSendMessages(Map<LocalDate, RoundUpJob> jobMap) {
        jobMap.values().forEach(job -> {
            String destinationQueue = job.isHasUnsettledTransactions() ? ROUND_UP_JOB_DLQ : ROUND_UP_JOB_QUEUE;
            jmsTemplate.convertAndSend(destinationQueue, new RoundUpMessage(job));
        });
    }

    private void saveJobs(List<RoundUpJob> jobs) {
        repo.saveAll(jobs);
    }

}
