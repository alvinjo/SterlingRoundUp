package com.alvin.roundup.service;


import com.alvin.roundup.domain.RoundUpJob;
import com.alvin.roundup.domain.RoundUpMessage;
import com.alvin.roundup.repo.RoundUpRepo;
import com.alvin.sterling.service.SavingsService;
import org.pmw.tinylog.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class RoundUpJobListener {

    public static final String ROUND_UP_JOB_QUEUE = "ROUND_UP_JOB_QUEUE";
    public static final String ROUND_UP_JOB_DLQ = "DLQ.ROUND_UP_JOB_QUEUE";

    private JmsMessagingTemplate jmsTemplate;
    private SavingsService savingsService;
    private RoundUpRepo roundUpRepo;

    @Autowired
    public RoundUpJobListener(JmsMessagingTemplate jmsTemplate, SavingsService savingsService, RoundUpRepo roundUpRepo) {
        this.savingsService = savingsService;
        this.jmsTemplate = jmsTemplate;
        this.roundUpRepo = roundUpRepo;
    }

    @JmsListener(destination = ROUND_UP_JOB_QUEUE)
    public void receiveRoundUpJob(RoundUpMessage message) {
        Logger.info("Picked up message with Id: {}", message.getRoundUpJob().getId());

        if (savingsService.performSavingsGoalTransfer(message.getRoundUpJob())) {
            var completedJob = message.getRoundUpJob().setStatus(RoundUpJob.JobStatus.COMPLETE);
            roundUpRepo.save(completedJob);
        } else{
            jmsTemplate.convertAndSend(ROUND_UP_JOB_DLQ, message);
        }
    }

    public void redriveRoundUpJobs() {
        var message = jmsTemplate.receiveAndConvert(ROUND_UP_JOB_DLQ, RoundUpMessage.class);
        if(message != null) {
            savingsService.performSavingsGoalTransfer(message.getRoundUpJob());
        }
    }

}
