package com.alvin.starling.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class FeedItems implements Serializable {

    private List<FeedItem> feedItems;

    @Getter
    @Setter
    @Entity
    @Accessors(chain = true)
    public static class FeedItem {
        @Id
        private String feedItemUid;
        private String categoryUid;
        private CurrencyAndAmount amount;
        private CurrencyAndAmount sourceAmount;
        private Direction direction;
        private String updatedAt;
        private String transactionTime;
        private String settlementTime;
        private String retryAllocationUntilTime;
        private String source;
        private String sourceSubType;
        private Status status;
        private String transactingApplicationUserUid;
        private String counterPartyType;
        private String counterPartyUid;
        private String counterPartyName;
        private String counterPartySubEntityUid;
        private String counterPartySubEntityName;
        private String counterPartySubEntityIdentifier;
        private String counterPartySubEntitySubIdentifier;
        private double exchangeRate;
        private double totalFees;
        private CurrencyAndAmount totalFeeAmount;
        private String reference;
        private String country;
        private String spendingCategory;
        private String userNote;
        private boolean hasAttachment;
        private boolean hasReceipt;

        public enum Status {
            UPCOMING,
            UPCOMING_CANCELLED,
            PENDING,
            REVERSED,
            SETTLED,
            DECLINED,
            REFUNDED,
            RETRYING,
            ACCOUNT_CHECK
        }

        public enum Direction {
            IN, OUT
        }
    }
}
