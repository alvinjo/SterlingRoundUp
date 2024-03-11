package com.alvin.roundup.rest;

import com.alvin.roundup.repo.domain.RoundUpJob;
import com.alvin.roundup.repo.domain.RoundUpJobRequest;
import com.alvin.roundup.service.RoundUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/roundup")
public class RoundUpController {

    private RoundUpService roundUpService;

    @Autowired
    public RoundUpController(RoundUpService roundUpService) {
        this.roundUpService = roundUpService;
    }

    @GetMapping("/{jobId}")
    public HttpEntity<RoundUpJob> getRoundUpJob(@PathVariable("jobId") String jobId) {
        return new HttpEntity<>(roundUpService.getRoundUpJob(LocalDate.parse(jobId)));
    }

    @GetMapping("/test")
    public HttpEntity<RoundUpJob> test(){
        return new HttpEntity<>(roundUpService.test());
    }

    @PostMapping
    public HttpEntity<List<String>> createRoundUpJobs(@RequestBody RoundUpJobRequest jobRequest) {
        return new HttpEntity<>(roundUpService.createRoundUpJobs(jobRequest));
    }


//    @PatchMapping
//    public HttpEntity<List<SavingsGoal>> rerunRoundUpJobs() {
//        return new HttpEntity<>(roundUpService.getSavings());
//    }

}