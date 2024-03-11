package com.alvin.starling.service;

import com.alvin.starling.domain.CurrencyAndAmount;
import com.alvin.starling.domain.FeedItems;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    public FeedItems fetchTransactionsWithinRange(LocalDateTime start, LocalDateTime end) {

        return new FeedItems().setFeedItems(List.of(new FeedItems.FeedItem().setTransactionTime(LocalDateTime.of(2024, 12, 12, 0,0,0,0)).setAmount(new CurrencyAndAmount().setMinorUnits(1250))));
    }

}
