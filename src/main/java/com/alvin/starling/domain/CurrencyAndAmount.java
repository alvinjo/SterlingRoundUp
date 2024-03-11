package com.alvin.starling.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class CurrencyAndAmount implements Serializable {
    private String currency;
    private long minorUnits; //TODO big decimal?
}
