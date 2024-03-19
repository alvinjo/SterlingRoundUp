package com.alvin.sterling.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SavingsGoalTransferResponseV2 {
    private String transferUid;
    private boolean success;
}
