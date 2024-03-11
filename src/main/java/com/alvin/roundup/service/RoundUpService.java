package com.alvin.roundup.service;

import com.alvin.roundup.repo.RoundUpRepo;
import com.alvin.roundup.repo.domain.RoundUpJob;
import com.alvin.roundup.repo.domain.RoundUpJobRequest;
import com.alvin.starling.domain.FeedItems;
import com.alvin.starling.service.TransactionService;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@NoArgsConstructor
public class RoundUpService {

    private RoundUpRepo repo;

    private TransactionService transactionService;

    @Autowired
    public RoundUpService(RoundUpRepo repo, TransactionService transactionService) {
        this.repo = repo;
        this.transactionService = transactionService;
    }

    public RoundUpJob getRoundUpJob(LocalDate jobId) {
        return repo.findByJobId(jobId); //TODO what is IEEE rest response for not found? what does repo return if not found?
    }

    public List<String> createRoundUpJobs(RoundUpJobRequest jobRequest) {

        if(!validDate(jobRequest.getStartDate()) || !validDate(jobRequest.getEndDate())){
            throw new RuntimeException(""); //TODO double check exceptions
        }

        LocalDateTime startDateTime = LocalDateTime.of(LocalDate.parse(jobRequest.getStartDate()), LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(LocalDate.parse(jobRequest.getEndDate()), LocalTime.MAX);

        //split range into days of format yyyy-MM-dd
        Map<LocalDate, RoundUpJob> jobTransactionsMap = new HashMap<>();
        List<LocalDate> dates = generateDateList(LocalDate.parse(jobRequest.getStartDate()), LocalDate.parse(jobRequest.getEndDate()));
        dates.forEach(d -> jobTransactionsMap.put(d, new RoundUpJob().setJobId(d)));

        //perform transactions fetch for each day and create message
        FeedItems feedItems = transactionService.fetchTransactionsWithinRange(startDateTime, endDateTime);

        for (FeedItems.FeedItem transaction : feedItems.getFeedItems()) {
            LocalDate transactionDate = transaction.getTransactionTime().toLocalDate();
            long transactionSaving = roundUp(transaction.getAmount().getMinorUnits());
            jobTransactionsMap.get(transactionDate).addValueToRoundUp(transactionSaving);
        }

        return Collections.emptyList();
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
        repo.save(new RoundUpJob().setJobId(LocalDate.parse("2024-01-01")).setStatus(RoundUpJob.JobStatus.COMPLETE));
        return repo.findByJobId(LocalDate.parse("2024-01-01"));
    }

}
