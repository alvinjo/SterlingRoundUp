package com.alvin.roundup.service;

import com.alvin.common.DateUtils;
import com.alvin.roundup.repo.RoundUpRepo;
import com.alvin.roundup.repo.domain.RoundUpJob;
import com.alvin.roundup.repo.domain.RoundUpJobRequest;
import com.alvin.roundup.repo.domain.RoundUpMessage;
import com.alvin.starling.domain.FeedItems;
import com.alvin.starling.service.TransactionFeedService;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.alvin.roundup.service.RoundUpJobListener.ROUND_UP_JOB_DLQ;
import static com.alvin.roundup.service.RoundUpJobListener.ROUND_UP_JOB_QUEUE;

@Service
@NoArgsConstructor
public class RoundUpService {

    private RoundUpRepo repo;

    private TransactionFeedService transactionFeedService;

    private JmsMessagingTemplate jmsTemplate;

    @Autowired
    public RoundUpService(RoundUpRepo repo, TransactionFeedService transactionFeedService, JmsMessagingTemplate jmsTemplate) {
        this.repo = repo;
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

        //create and initialise a job map for calculating our round up totals
        Map<LocalDate, RoundUpJob> jobMap = initialiseJobMap(dates, jobRequest.getAccountId(), jobRequest.getCategoryId());

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
        if (!StringUtils.hasText(request.getAccountId()) || !StringUtils.hasText(request.getCategoryId())) {
            throw new IllegalArgumentException("Account ID and Category ID are required");
        }

        LocalDateTime startDateTime = LocalDateTime.of(LocalDate.parse(request.getStartDate()), LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(LocalDate.parse(request.getEndDate()), LocalTime.MAX);
        if(startDateTime.isAfter(endDateTime)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
    }

    private Map<LocalDate, RoundUpJob> initialiseJobMap(List<LocalDate> dates, String accountId, String categoryId){
        Map<LocalDate, RoundUpJob> jobMap = new HashMap<>();
        dates.forEach(date -> {
            //check if we have already processed this job
            var existingJob = repo.findById(date.toString());
            if(existingJob.isEmpty() || RoundUpJob.JobStatus.COMPLETE != existingJob.get().getStatus()) {
                jobMap.put(date, new RoundUpJob(date.toString(), accountId, categoryId));
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
