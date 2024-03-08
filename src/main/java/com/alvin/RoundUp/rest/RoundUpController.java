package com.alvin.RoundUp.rest;

import com.alvin.RoundUp.repo.domain.SavingsGoal;
import com.alvin.RoundUp.service.RoundUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller("/api/v1/roundup")
public class RoundUpController {

    private RoundUpService roundUpService;

    @Autowired
    public RoundUpController(RoundUpService roundUpService) {
        this.roundUpService = roundUpService;
    }


    @GetMapping("/savings")
    public HttpEntity<List<SavingsGoal>> getSavingsGoal() {
        return new HttpEntity<>(roundUpService.getSavings());
    }


}
