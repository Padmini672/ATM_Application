package com.machine.atm.model;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;

@Setter
@Getter
public class Server {

    @Value("${withdrawal.currency.available}")
    private Double totalAvailableCurrencyForWithdrawal;

    @Value("${multiples.fifty}")
    private Integer fiftyMultiples;

    @Value("${multiples.twenty}")
    private Integer twentyMultiples;

    @Value("${multiples.ten}")
    private Integer tenMultiples;

    @Value("${multiples.five}")
    private Integer fiveMultiples;

    public void setAllVariables(Double totalAvailableCurrencyForWithdrawal,
                                Integer fiftyMultiples, Integer twentyMultiples, Integer tenMultiples, Integer fiveMultiples) {
        this.totalAvailableCurrencyForWithdrawal = totalAvailableCurrencyForWithdrawal;
        this.fiftyMultiples = fiftyMultiples;
        this.twentyMultiples = twentyMultiples;
        this.tenMultiples = tenMultiples;
        this.fiveMultiples = fiveMultiples;
    }
}
