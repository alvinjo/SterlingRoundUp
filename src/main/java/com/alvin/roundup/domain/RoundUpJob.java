package com.alvin.roundup.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "ROUND_UP_JOB")
public class RoundUpJob {

    public enum JobStatus {
        COMPLETE,
    }

    public RoundUpJob(String id, String accountId, String categoryId) {
        this.id = id;
        this.accountId = accountId;
        this.categoryId = categoryId;
    }

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "ACCOUNT_ID")
    private String accountId;

    @Column(name = "CATEGORY_ID")
    private String categoryId;

    @Column(name = "TRANSFER_ID")
    private String transferId;

//    private FeedItems transactions;

    @Column(name = "STATUS")
    private JobStatus status;

    @Transient
    private long transferValue;

    @Transient
    private String currency;

    @Transient
    private boolean hasUnsettledTransactions;

    public void addValueToRoundUp(long value) {
        transferValue += value;
    }
}
