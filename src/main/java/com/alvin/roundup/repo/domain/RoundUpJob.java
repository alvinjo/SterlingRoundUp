package com.alvin.roundup.repo.domain;

import com.alvin.starling.domain.FeedItems;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@Accessors(chain = true)
public class RoundUpJob {

    public enum JobStatus {
        COMPLETE,
    }

    @Id
    private LocalDate jobId;

    private FeedItems transactions;

    private JobStatus status;

    private long roundUpValue; //not persisted

    private boolean hasUnsettledTransactions; //not persisted

    public void addValueToRoundUp(long value) {
        roundUpValue += value;
    }
}
