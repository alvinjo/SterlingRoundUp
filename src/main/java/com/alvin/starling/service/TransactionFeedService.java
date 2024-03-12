package com.alvin.starling.service;

import com.alvin.common.DateUtils;
import com.alvin.common.SecurityUtils;
import com.alvin.starling.domain.FeedItems;
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

@Service
public class TransactionFeedService {

    @Value("${starling.base.url}")
    private String starlingBaseUrl;

    @Value("${starling.feed.path}")
    private String feedPath;

    private SecurityUtils securityUtils;

    private HttpClient httpClient;

    @Autowired
    public TransactionFeedService(HttpClient httpClient, SecurityUtils securityUtils) {
        this.httpClient = httpClient;
        this.securityUtils = securityUtils;
    }

    public FeedItems fetchTransactionsWithinRange(LocalDateTime start, LocalDateTime end, String accountId) throws IOException, InterruptedException {
        var jsonMapper = new Gson();

        var transactionsFeedUri = UriComponentsBuilder.fromHttpUrl(starlingBaseUrl).path(feedPath)
                .pathSegment("account", accountId, "settled-transactions-between")
                .queryParam("minTransactionTimestamp", DateUtils.dateTimeToFormattedString(start))
                .queryParam("maxTransactionTimestamp", DateUtils.dateTimeToFormattedString(end)).build().toUri();

        var request = HttpRequest.newBuilder(transactionsFeedUri)
                .header("Authorization", "Bearer " + securityUtils.getUserAccessToken())
                .GET().build();

        var clientResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return jsonMapper.fromJson(clientResponse.body(), FeedItems.class);
    }

}
