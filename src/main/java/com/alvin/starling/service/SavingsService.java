package com.alvin.starling.service;

import com.alvin.roundup.repo.domain.RoundUpJob;
import com.alvin.starling.domain.SavingsGoalTransferResponseV2;
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

@Service
public class SavingsService {

    @Value("${starling.baseurl}")
    private String starlingBaseUrl;

    @Value("${starling.account.url}")
    private String accountUrl;

    private HttpClient httpClient;

    @Autowired
    public SavingsService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void performSavingsGoalTransfer(RoundUpJob roundUpJob) {
        var jsonMapper = new Gson();
        try {
            String transferId = "";
            var transferUri = UriComponentsBuilder.fromPath(starlingBaseUrl).path(accountUrl)
                    .pathSegment(roundUpJob.getAccountId(), "savings-goals", roundUpJob.getCategoryId(), "add-money", transferId).build().toUri();

            var body = new TopUpRequestV2(roundUpJob.getCurrency(), roundUpJob.getTransferValue());

            var request = HttpRequest.newBuilder(transferUri)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonMapper.toJson(body))).build();

            var clientResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            var transferResponse = jsonMapper.fromJson(clientResponse.body(), SavingsGoalTransferResponseV2.class);

            if(!transferResponse.isSuccess()){
                //TODO
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e); //TODO
        }
    }

}
