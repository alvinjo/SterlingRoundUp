package com.alvin.sterling.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopUpRequestV2 {
    private CurrencyAndAmount amount;

    public TopUpRequestV2(String currency, long minorUnits) {
        this.amount = new CurrencyAndAmount().setCurrency(currency).setMinorUnits(minorUnits);
    }
}
