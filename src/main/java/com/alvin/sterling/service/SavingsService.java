package com.alvin.sterling.service;

import com.alvin.common.utils.SecurityUtils;
import com.alvin.roundup.domain.RoundUpJob;
import com.alvin.sterling.domain.SavingsGoalTransferResponseV2;
import com.alvin.sterling.domain.Spaces;
import com.alvin.sterling.domain.TopUpRequestV2;
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

    @Value("${sterling.base.url}")
    private String sterlingBaseUrl;

    @Value("${sterling.account.path}")
    private String accountPath;

    private SecurityUtils securityUtils;

    private HttpClient httpClient;

    @Autowired
    public SavingsService(HttpClient httpClient, SecurityUtils securityUtils) {
        this.httpClient = httpClient;
        this.securityUtils = securityUtils;
    }

    //TODO retryable https://www.baeldung.com/spring-retry (retry in netw failure scenarios as an example)
    public boolean performSavingsGoalTransfer(RoundUpJob roundUpJob) {
        var jsonMapper = new Gson();
        try {
            roundUpJob.setTransferId(UUID.randomUUID().toString());

            var savingsTransferUri = UriComponentsBuilder.fromHttpUrl(sterlingBaseUrl).path(accountPath)
                    .pathSegment(roundUpJob.getAccountId(), "savings-goals", roundUpJob.getSavingsGoalId(), "add-money", roundUpJob.getTransferId()).build().toUri();

            var body = new TopUpRequestV2(roundUpJob.getCurrency(), roundUpJob.getTransferValue());

            var request = HttpRequest.newBuilder(savingsTransferUri)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + securityUtils.getUserAccessToken())
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonMapper.toJson(body))).build();

            var clientResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            var transferResponse = jsonMapper.fromJson(clientResponse.body(), SavingsGoalTransferResponseV2.class);

            Logger.info("Transfer success: {}", transferResponse);
            return transferResponse.isSuccess();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public Spaces getSavingsGoalsList(String accountId) {
        var jsonMapper = new Gson();
        try{
            var getSavingsGoalsUri = UriComponentsBuilder.fromHttpUrl(sterlingBaseUrl).path(accountPath)
                    .pathSegment(accountId, "spaces").build().toUri();

            var request = HttpRequest.newBuilder(getSavingsGoalsUri)
                    .header("Authorization", "Bearer " + securityUtils.getUserAccessToken())
                    .GET().build();

            var clientResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return jsonMapper.fromJson(clientResponse.body(), Spaces.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
