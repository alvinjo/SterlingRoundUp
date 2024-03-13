package com.alvin.roundup.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoundUpJobRequest {
    private String accountId;
    private String categoryId;
    private String startDate;
    private String endDate;
}
