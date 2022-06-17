package com.machine.atm.controller;

import com.machine.atm.exception.CustomerNotFoundException;
import com.machine.atm.exception.PreDispenseValidationException;
import com.machine.atm.model.Customer;
import com.machine.atm.model.Error;
import com.machine.atm.model.Server;
import com.machine.atm.repository.CustomerRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class ApiControllerTest {

    @Autowired
    ApiController apiController;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    ApiControllerAdvice apiControllerAdvice;
    @Autowired
    Server server;

    @BeforeEach
    void beginInit(){
        customerRepository.save(new Customer(123456789L, 1234, 1600.0, 10.0));
        server.setAllVariables(1500.0, 10, 30, 30 , 20);
    }

    @Test
    void getAccountBalance(){
        String auth = "123456789:1234";
        String encodedAuth = "Basic "+ Base64.getEncoder().withoutPadding().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        BalanceDao balanceDao = (BalanceDao) apiController.getAccountBalance(encodedAuth).getBody();
        Assertions.assertEquals(balanceDao.getAccountBalance(),1600);
        Assertions.assertEquals(balanceDao.getMaxWithdrawalAmount(),server.getTotalAvailableCurrencyForWithdrawal());
    }

    @Test
    void withdrawMoney(){
        String auth = "123456789:1234";
        String encodedAuth = "Basic "+ Base64.getEncoder().withoutPadding().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        WithdrawDao withdrawDao = (WithdrawDao) apiController.withdrawMoney(encodedAuth, 1450.0).getBody();
        Assertions.assertEquals(withdrawDao.getRemainingAccountBalance(),150);
        Assertions.assertEquals(withdrawDao.getAmountWithdrawn(),1450);
        Assertions.assertEquals(withdrawDao.getFiftyMultiples(),10);
        Assertions.assertEquals(withdrawDao.getTwentyMultiples(),30);
        Assertions.assertEquals(withdrawDao.getTenMultiples(),30);
        Assertions.assertEquals(withdrawDao.getFiveMultiples(),10);
    }

    @Test
    void ExceptionResponse(){
        //Dummy validation to cover 100 percent testing
        Error error = apiControllerAdvice.resourceNotFoundException(new CustomerNotFoundException("Message", "Description"));
        Assertions.assertEquals(error.getTimestamp().getDate(), new Date().getDate());
        Assertions.assertEquals(error.getMessage(), "Message");
        Assertions.assertEquals(error.getDescription(), "Description");
        error = apiControllerAdvice.globalExceptionHandler(new Exception("Message"));
        Assertions.assertEquals(error.getTimestamp().getDate(), new Date().getDate());
        Assertions.assertEquals(error.getMessage(), "Message");
        error = apiControllerAdvice.preDispenseValidation(new PreDispenseValidationException("Description"));
        Assertions.assertEquals(error.getTimestamp().getDate(), new Date().getDate());
        Assertions.assertEquals(error.getMessage(), "Can not dispense the request amount");
        Assertions.assertEquals(error.getDescription(), "Description");

    }



}
