package com.machine.atm.service;

import com.machine.atm.exception.CustomerNotFoundException;
import com.machine.atm.exception.PreDispenseValidationException;
import com.machine.atm.model.Customer;
import com.machine.atm.model.Server;
import com.machine.atm.repository.CustomerRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class CustomerServiceTest {

    @Autowired
    CustomerService customerService;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    Server server;

    @BeforeEach
    void beginInit(){
        customerRepository.save(new Customer(123456789L, 1234, null, null));
        server.setAllVariables(1500.0, 10, 30, 30 , 20);
    }


    @Test
    void getUserFromAuthTestInvalidCustomer(){
        String auth = "12345678:1234";
        String encodedAuth = "Basic "+ Base64.getEncoder().withoutPadding().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        CustomerNotFoundException exception = Assertions.assertThrows(CustomerNotFoundException.class, () -> {
            customerService.getUserFromAuth(encodedAuth);
        });
        Assertions.assertEquals("Invalid Customer request", exception.getMessage());
        Assertions.assertEquals(auth.split(":")[0]+" : Account number or pin is incorrect, please verify!", exception.getDescription());
    }

    @Test
    void getUserFromAuthTestValidCustomer(){
        String auth = "123456789:1234";
        String encodedAuth = "Basic "+ Base64.getEncoder().withoutPadding().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        Customer customer = customerService.getUserFromAuth(encodedAuth);
        Assertions.assertEquals(customer.getAccountNumber().toString(), auth.substring(0,9));
    }

    @Test
    void preDispenseValidationNoCashException(){
        Customer customer = customerRepository.findById(123456789L).orElse(null);
        server.setTotalAvailableCurrencyForWithdrawal(0.0);
        PreDispenseValidationException exception = Assertions.assertThrows(PreDispenseValidationException.class, () -> {
            customerService.preDispenseValidation(customer, server, 100.0);
        });
        Assertions.assertEquals("Can not dispense the request amount", exception.getMessage());
        Assertions.assertEquals("No cash left in the machine to withdraw, try again later", exception.getDescription());
    }

    @Test
    void preDispenseValidationLessAccountBalanceException(){
        Customer customer = customerRepository.findById(123456789L).orElse(null);
        customer.setAccountBalance(100.0);
        customer.setOverDraft(10.0);
        PreDispenseValidationException exception = Assertions.assertThrows(PreDispenseValidationException.class, () -> {
            customerService.preDispenseValidation(customer, server, 150.0);
        });
        Assertions.assertEquals("Can not dispense the request amount", exception.getMessage());
        Assertions.assertTrue(exception.getDescription().contains("Insufficient fund: Exceed withdrawal limit"));
    }

    @Test
    void preDispenseValidationLowCash(){
        Customer customer = customerRepository.findById(123456789L).orElse(null);
        customer.setAccountBalance(1600.0);
        customer.setOverDraft(10.0);
        PreDispenseValidationException exception = Assertions.assertThrows(PreDispenseValidationException.class, () -> {
            customerService.preDispenseValidation(customer, server, 1600.0);
        });
        Assertions.assertEquals("Can not dispense the request amount", exception.getMessage());
        Assertions.assertTrue(exception.getDescription().contains("Low cash, requested amount is currently not available"));
    }

    @Test
    void preDispenseValidationIncorrectCashRequest(){
        Customer customer = customerRepository.findById(123456789L).orElse(null);
        customer.setAccountBalance(1000.0);
        customer.setOverDraft(10.0);
        PreDispenseValidationException exception = Assertions.assertThrows(PreDispenseValidationException.class, () -> {
            customerService.preDispenseValidation(customer, server, 163.0);
        });
        Assertions.assertEquals("Can not dispense the request amount", exception.getMessage());
        Assertions.assertEquals(exception.getDescription(),"Enter the amount in multiples of 5");
    }

    @Test
    void preDispenseValidationSuccessfulTransaction(){
        Customer customer = customerRepository.findById(123456789L).orElse(null);
        customer.setAccountBalance(1500.0);
        customer.setOverDraft(10.0);
        Map<Integer, Integer> currencyDispense = customerService.preDispenseValidation(customer, server, 1455.0);
        Assertions.assertEquals(currencyDispense, Map.of(50,10, 20, 30, 10, 30, 5, 11));
    }



}
