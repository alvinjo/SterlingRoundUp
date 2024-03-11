package com.alvin.starling.service;

import com.alvin.starling.domain.CurrencyAndAmount;
import com.alvin.starling.domain.FeedItems;
import com.alvin.starling.domain.TopUpRequestV2;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionFeedService {

    @Value("${starling.base.url}")
    private String starlingBaseUrl;

    @Value("${starling.feed.path}")
    private String feedPath;

    private HttpClient httpClient;

    @Autowired
    public TransactionFeedService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public FeedItems fetchTransactionsWithinRange(LocalDateTime start, LocalDateTime end, String accountId) throws IOException, InterruptedException {
        var jsonMapper = new Gson();

        var transactionsFeedUri = UriComponentsBuilder.fromPath(starlingBaseUrl).path(feedPath)
                .pathSegment("account", accountId, "settled-transactions-between")
                .queryParam("minTransactionTimestamp ", start)
                .queryParam("maxTransactionTimestamp ", end).build().toUri();

        var request = HttpRequest.newBuilder(transactionsFeedUri).GET().build();

        var clientResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return jsonMapper.fromJson(clientResponse.body(), FeedItems.class);
    }

}
