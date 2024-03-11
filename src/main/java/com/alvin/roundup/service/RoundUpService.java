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
            return repo.findByJobId(jobId); //TODO what is IEEE rest response for not found? what does repo return if not found?
        } catch (Exception e){
            return null;
        }
    }

    public Set<LocalDate> createRoundUpJobs(RoundUpJobRequest jobRequest) throws IOException, InterruptedException {
        validateRoundUpJobRequest(jobRequest);

        LocalDateTime startDateTime = LocalDateTime.of(LocalDate.parse(jobRequest.getStartDate()), LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(LocalDate.parse(jobRequest.getEndDate()), LocalTime.MAX);

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

        return jobMap.keySet();
    }

    private void validateRoundUpJobRequest(RoundUpJobRequest request) {
        if (!DateUtils.validDate(request.getStartDate()) || !DateUtils.validDate(request.getEndDate())) {
            throw new IllegalArgumentException("Invalid date range");
        }
        if (!StringUtils.hasText(request.getAccountId()) || !StringUtils.hasText(request.getCategoryId())) {
            throw new IllegalArgumentException("Account ID and Category ID are required");
        }
    }

    private Map<LocalDate, RoundUpJob> initialiseJobMap(List<LocalDate> dates, String accountId, String categoryId){
        Map<LocalDate, RoundUpJob> jobMap = new HashMap<>();
        dates.forEach(date -> jobMap.put(date, new RoundUpJob(date.toString(), accountId, categoryId)));
        return jobMap;
    }

    private void updateJobMapWithTransactions(Map<LocalDate, RoundUpJob> jobMap, FeedItems feedItems) {
        for (FeedItems.FeedItem transaction : feedItems.getFeedItems()) {
            LocalDate transactionDate = transaction.getTransactionTime().toLocalDate();

            if(FeedItems.FeedItem.Status.SETTLED == transaction.getStatus()) {
                long transactionSaving = roundUp(transaction.getAmount().getMinorUnits());
                jobMap.get(transactionDate).addValueToRoundUp(transactionSaving);
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

}
