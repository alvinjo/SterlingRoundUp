package com.alvin.starling.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class FeedItems implements Serializable {

    private List<FeedItem> feedItems;

    @Getter
    @Setter
    @Entity
    public class FeedItem {
        @Id
        private String feedItemUid;
        private String categoryUid;
        private CurrencyAndAmount amount;
        private CurrencyAndAmount sourceAmount;
        private String direction;
        private String updatedAt;
        private LocalDateTime transactionTime;
        private LocalDateTime settlementTime;
        private String retryAllocationUntilTime;
        private String source;
        private String sourceSubType;
        private String status;
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
        //    private AssociatedFeedRoundUp roundUp;
        private boolean hasAttachment;
        private boolean hasReceipt;
//    private BatchPaymentDetails batchPaymentDetails;
    }
}
