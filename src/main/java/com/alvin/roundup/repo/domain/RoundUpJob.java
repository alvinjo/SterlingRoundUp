package com.alvin.roundup.repo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class RoundUpJob {

    public enum JobStatus {
        COMPLETE,
    }

    public RoundUpJob(String jobId, String accountId, String categoryId) {
        this.jobId = jobId;
        this.accountId = accountId;
        this.categoryId = categoryId;
    }

    @Id
    private String jobId;

    private String accountId;

    private String categoryId;

//    private FeedItems transactions;

    private JobStatus status;

    private long transferValue; //not persisted

    private String currency; //not persisted

    private boolean hasUnsettledTransactions; //not persisted

    public void addValueToRoundUp(long value) {
        transferValue += value;
    }
}
