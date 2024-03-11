package com.alvin.roundup.service;

import com.alvin.roundup.repo.RoundUpRepo;
import com.alvin.roundup.repo.domain.RoundUpJob;
import com.alvin.roundup.repo.domain.RoundUpJobRequest;
import com.alvin.roundup.repo.domain.RoundUpMessage;
import com.alvin.starling.domain.FeedItems;
import com.alvin.starling.service.TransactionService;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.alvin.roundup.service.JobListener.ROUND_UP_JOB_DLQ;
import static com.alvin.roundup.service.JobListener.ROUND_UP_JOB_QUEUE;

@Service
@NoArgsConstructor
public class RoundUpService {

    private RoundUpRepo repo;

    private TransactionService transactionService;

    private JmsMessagingTemplate jmsTemplate;

    @Autowired
    public RoundUpService(RoundUpRepo repo, TransactionService transactionService, JmsMessagingTemplate jmsTemplate) {
        this.repo = repo;
        this.transactionService = transactionService;
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

    public Set<LocalDate> createRoundUpJobs(RoundUpJobRequest jobRequest) {
        if(!validDate(jobRequest.getStartDate()) || !validDate(jobRequest.getEndDate())){
            throw new RuntimeException(""); //TODO double check exceptions
        }
        if(!StringUtils.hasText(jobRequest.getAccountId()) || !StringUtils.hasText(jobRequest.getCategoryId())){
            throw new RuntimeException(""); //TODO double check exceptions
        }

        LocalDateTime startDateTime = LocalDateTime.of(LocalDate.parse(jobRequest.getStartDate()), LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(LocalDate.parse(jobRequest.getEndDate()), LocalTime.MAX);

        //split range into days of format yyyy-MM-dd
        List<LocalDate> dates = generateDateList(LocalDate.parse(jobRequest.getStartDate()), LocalDate.parse(jobRequest.getEndDate()));

        //create and populate a job map calculating round up totals
        Map<LocalDate, RoundUpJob> jobTransactionsMap = new HashMap<>();
        dates.forEach(date -> jobTransactionsMap.put(date, new RoundUpJob(date.toString(), jobRequest.getAccountId(), jobRequest.getCategoryId())));

        //perform transactions fetch on entire range
        FeedItems feedItems = transactionService.fetchTransactionsWithinRange(startDateTime, endDateTime);

        //calculate round up and update job map
        for (FeedItems.FeedItem transaction : feedItems.getFeedItems()) {
            LocalDate transactionDate = transaction.getTransactionTime().toLocalDate();

            if(FeedItems.FeedItem.Status.SETTLED == transaction.getStatus()) {
                long transactionSaving = roundUp(transaction.getAmount().getMinorUnits());
                jobTransactionsMap.get(transactionDate).addValueToRoundUp(transactionSaving);
            } else {
                jobTransactionsMap.get(transactionDate).setHasUnsettledTransactions(true);
            }
        }

        //create message for each job and send to their appropriate queues
        jobTransactionsMap.values().forEach(job -> {
            String destinationQueue = job.isHasUnsettledTransactions() ? ROUND_UP_JOB_DLQ : ROUND_UP_JOB_QUEUE;
            jmsTemplate.convertAndSend(destinationQueue, new RoundUpMessage(job));
        });

        return jobTransactionsMap.keySet();
    }

    private long roundUp(long value) {
        long roundedUpValue = (value + 99) / 100 * 100;
        return roundedUpValue - value;
    }

    private boolean validDate(String date) {
        try {
            DateTimeFormatter.ofPattern("yyyy-MM-dd").parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private List<LocalDate> generateDateList(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dateList = new ArrayList<>();

        // Iterate through each date in the range
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            dateList.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }

        return dateList;
    }

    public RoundUpJob test(){
        repo.save(new RoundUpJob().setJobId(("2024-01-01")).setStatus(RoundUpJob.JobStatus.COMPLETE));
        return repo.findByJobId(("2024-01-01"));
    }

}
