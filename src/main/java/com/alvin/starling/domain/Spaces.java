package com.alvin.starling.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Spaces {

    private List<SavingsGoalOrdered> savingsGoals;
    private List<SpendingSpace> spendingSpaces;

    @Getter
    @Setter
    public static class SavingsGoalOrdered {
        private String savingsGoalUid;
        private String name;
        private CurrencyAndAmount target;
        private CurrencyAndAmount totalSaved;
        private int savedPercentage;
        private int sortOrder;
        private String state;
    }

    @Getter
    @Setter
    public static class SpendingSpace {
        private String uid;
        private String name;
        private CurrencyAndAmount balance;
        private String cardAssociationUid;
        private int sortOrder;
        private String spendingSpaceType;
        private String state;
        private String spaceUid;
    }

}