package com.alvin.starling.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SavingsGoalsV2 {

    private List<SavingsGoalV2> savingsGoalsList;

    @Getter
    @Setter
    public static class SavingsGoalV2 {

        private String savingsGoalUid;
        private String description;
        private String name;
        private CurrencyAndAmount target;
        private CurrencyAndAmount totalSaved;
        private int savedPercentage;
        private SavingsGoalState state;

        public enum SavingsGoalState {
            CREATING, ACTIVE, ARCHIVING, ARCHIVED, RESTORING, PENDING
        }

    }

}
