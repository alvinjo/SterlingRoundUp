package com.alvin.starling.service;

import com.alvin.common.SecurityUtils;
import com.alvin.roundup.repo.domain.RoundUpJob;
import com.alvin.starling.domain.SavingsGoalTransferResponseV2;
import com.alvin.starling.domain.TopUpRequestV2;
import com.google.gson.Gson;
import org.pmw.tinylog.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@Service
public class SavingsService {

    @Value("${starling.base.url}")
    private String starlingBaseUrl;

    @Value("${starling.account.path}")
    private String accountPath;

    private SecurityUtils securityUtils;

    private HttpClient httpClient;

    @Autowired
    public SavingsService(HttpClient httpClient, SecurityUtils securityUtils) {
        this.httpClient = httpClient;
        this.securityUtils = securityUtils;
    }

    //TODO retryable https://www.baeldung.com/spring-retry
    public boolean performSavingsGoalTransfer(RoundUpJob roundUpJob) {
        var jsonMapper = new Gson();
        try {
            String transferId = String.valueOf(UUID.randomUUID().toString()).replace("-", "");
            roundUpJob.setTransferId(transferId);

            var savingsTransferUri = UriComponentsBuilder.fromHttpUrl(starlingBaseUrl).path(accountPath)
                    .pathSegment(roundUpJob.getAccountId(), "savings-goals", roundUpJob.getCategoryId(), "add-money", transferId).build().toUri();

            var body = new TopUpRequestV2(roundUpJob.getCurrency(), roundUpJob.getTransferValue());

            var request = HttpRequest.newBuilder(savingsTransferUri)
                    .header("Authorization", "Bearer " + securityUtils.getUserAccessToken())
                    .POST(HttpRequest.BodyPublishers.ofString(jsonMapper.toJson(body))).build();

            var clientResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            var transferResponse = jsonMapper.fromJson(clientResponse.body(), SavingsGoalTransferResponseV2.class);

            Logger.info("Transfer success: {}", transferResponse);
            return transferResponse.isSuccess();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e); //TODO
        }
    }

}
