package com.alvin.starling.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Getter
@Setter
@Accessors(chain = true)
public class CurrencyAndAmount implements Serializable {
    private String currency;
    private long minorUnits; //TODO big decimal?
}
