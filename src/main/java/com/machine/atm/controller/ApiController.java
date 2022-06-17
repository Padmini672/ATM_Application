package com.machine.atm.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.machine.atm.model.Customer;
import com.machine.atm.model.Server;
import com.machine.atm.service.CustomerService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Slf4j
public class ApiController {

    private final Server server;
    private final CustomerService customerService;

    public ApiController(Server server, CustomerService customerService) {
        this.server = server;
        this.customerService = customerService;
    }


    @GetMapping("/balance")
    public ResponseEntity<?> getAccountBalance(@RequestHeader("Authorization") String authorization){
        Customer customer = customerService.getUserFromAuth(authorization);
        Double accountBalance = customer.getAccountBalance();
        Double maxWithdrawalAmount = accountBalance > server.getTotalAvailableCurrencyForWithdrawal()
                ?  server.getTotalAvailableCurrencyForWithdrawal() : null;
        log.info("Customer: " +customer.getAccountNumber() + " tried to check his balance");
        return new ResponseEntity<>(new BalanceDao(accountBalance,maxWithdrawalAmount), HttpStatus.OK);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdrawMoney(@RequestHeader("Authorization") String authorization, @RequestBody Double withdrawAmount){
        Customer customer = customerService.getUserFromAuth(authorization);
        Map<Integer, Integer> integerIntegerMap = customerService.preDispenseValidation(customer, server, withdrawAmount);
        integerIntegerMap.values().removeIf(val-> val == 0);
        log.info("Customer: " +customer.getAccountNumber() + " tried to withdraw money");
        return new ResponseEntity<>(new WithdrawDao(customer.getAccountBalance(), withdrawAmount,
                integerIntegerMap.get(50), integerIntegerMap.get(20), integerIntegerMap.get(10),
                integerIntegerMap.get(5)),HttpStatus.OK);
    }
}

@AllArgsConstructor
@Getter
@JsonInclude(Include.NON_NULL)
class BalanceDao {
    private Double accountBalance;
    private Double maxWithdrawalAmount;
}

@Getter
@JsonInclude(Include.NON_NULL)
@AllArgsConstructor
class WithdrawDao {
    private Double remainingAccountBalance;
    private Double amountWithdrawn;
    private Integer fiftyMultiples;
    private Integer twentyMultiples;
    private Integer tenMultiples;
    private Integer fiveMultiples;
}
