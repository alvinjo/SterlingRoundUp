package com.alvin.roundup.repo.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class SavingsGoal {

    private String title;

    private String currency;

    private double amount;

}
