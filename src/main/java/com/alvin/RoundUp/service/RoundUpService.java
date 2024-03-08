package com.alvin.RoundUp.service;

import com.alvin.RoundUp.repo.RoundUpRepo;
import com.alvin.RoundUp.repo.domain.SavingsGoal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoundUpService {

    private RoundUpRepo repo;
    @Autowired
    public RoundUpService(RoundUpRepo repo) {
        this.repo = repo;
    }


    public List<SavingsGoal> getSavings() {
        var a = new SavingsGoal();
        List<SavingsGoal> savingsList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            savingsList.add(new SavingsGoal().setAmount(i));
        }
        return savingsList;
    }


}
