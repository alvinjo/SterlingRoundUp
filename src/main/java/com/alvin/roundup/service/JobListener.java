package com.alvin.roundup.service;


import com.alvin.roundup.repo.domain.RoundUpMessage;
import com.alvin.starling.service.SavingsService;
import org.pmw.tinylog.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class JobListener {

    public static final String ROUND_UP_JOB_QUEUE = "ROUND_UP_JOB_QUEUE";
    public static final String ROUND_UP_JOB_DLQ = "DLQ.ROUND_UP_JOB_QUEUE";

    private SavingsService savingsService;

    @Autowired
    public JobListener(SavingsService savingsService) {
        this.savingsService = savingsService;
    }

    @JmsListener(destination = ROUND_UP_JOB_QUEUE)
    public void recieveRoundUpJob(RoundUpMessage message) {
        Logger.info("Picked up message with Id:", message.getRoundUpJob().getJobId());

        savingsService.performSavingsGoalTransfer(message.getRoundUpJob());
    }

}
