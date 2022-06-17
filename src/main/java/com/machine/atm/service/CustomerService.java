package com.machine.atm.service;

import com.machine.atm.exception.CustomerNotFoundException;
import com.machine.atm.exception.PreDispenseValidationException;
import com.machine.atm.model.Customer;
import com.machine.atm.model.Server;
import com.machine.atm.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;


@Service
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    private final Server server;

    public CustomerService(CustomerRepository customerRepository, Server server) {
        this.customerRepository = customerRepository;
        this.server = server;
    }

    public Customer getUserFromAuth(String authorization) throws CustomerNotFoundException{
        authorization = new String(Base64.getDecoder().decode(authorization.substring(6)));
        Long username = Long.parseLong(authorization.split(":")[0]);
        Integer password = Integer.parseInt(authorization.split(":")[1]);
        Customer customer = customerRepository.findById(username).orElse(null);
        if(customer!=null && customer.getPinNumber().equals(password)){
            log.info("Login successful for customer: " + customer.getAccountNumber());
            return customer;
        }
        log.info("customer authentication failure : " + username);
        throw new CustomerNotFoundException("Invalid Customer request", username +" : Account number or pin is incorrect, please verify!");
    }

    public Map<Integer, Integer> preDispenseValidation(Customer customer, Server server, Double withdrawAmount){
        if(server.getTotalAvailableCurrencyForWithdrawal() == 0){
            log.error("ATM is empty when customer " + customer.getAccountNumber() + " tried to withdraw" );
            throw new PreDispenseValidationException("No cash left in the machine to withdraw, " +
                    "try again later");
        }
        else if(customer.getAccountBalance() <= 0 || withdrawAmount > customer.getAccountBalance() + customer.getOverDraft()){
            log.info("customer " + customer.getAccountNumber() + " tried to withdraw with account balance €" + customer.getAccountBalance());
            throw new PreDispenseValidationException("Insufficient fund: Exceed withdrawal limit, " +
                    "current available balance in your account : €" + customer.getAccountBalance());
        }
        else if(withdrawAmount > server.getTotalAvailableCurrencyForWithdrawal()){
            log.warn("Low cash warning: customer" + customer.getAccountNumber() + " tried to withdraw €" + customer.getAccountBalance());
            throw new PreDispenseValidationException("Low cash, requested amount is currently not available, " +
                    "try less than: €"+ server.getTotalAvailableCurrencyForWithdrawal());
        }
        else{
            Map<Integer, Integer> currentCurrencyCount = new LinkedHashMap<>();
            Map<Integer, Integer> dispenseCurrencyCount = new LinkedHashMap<>();
            if(dispenseCalculation(withdrawAmount, currentCurrencyCount, dispenseCurrencyCount)==0){
                server.setAllVariables(server.getTotalAvailableCurrencyForWithdrawal()-withdrawAmount,
                        currentCurrencyCount.get(50), currentCurrencyCount.get(20), currentCurrencyCount.get(10), currentCurrencyCount.get(5));
                customer.setAccountBalance(customer.getAccountBalance()-withdrawAmount);
                customerRepository.save(customer);
                log.info("Customer " + customer.getAccountNumber() +" transaction was successful, withdraw amount: €" +withdrawAmount);
                return dispenseCurrencyCount;
            }
            else {
                log.info("Customer " + customer.getAccountNumber() +" tried to withdraw €" +withdrawAmount + " but unsuccessful");
                throw new PreDispenseValidationException("Enter the amount in multiples of 5");
            }

         }
    }

    private Double dispenseCalculation(Double withdrawAmount, Map<Integer, Integer> currentCurrencyCount, Map<Integer, Integer> dispenseCurrencyCount) {
        currentCurrencyCount.put(50,server.getFiftyMultiples());
        currentCurrencyCount.put(20,server.getTwentyMultiples());
        currentCurrencyCount.put(10,server.getTenMultiples());
        currentCurrencyCount.put(5,server.getFiveMultiples());
        for(Integer currency : currentCurrencyCount.keySet()){
            if(withdrawAmount.intValue()/currency > currentCurrencyCount.get(currency)){
            dispenseCurrencyCount.put(currency, currentCurrencyCount.get(currency));
                withdrawAmount -= currency * currentCurrencyCount.get(currency);
            currentCurrencyCount.put(currency, 0);
            } else {
                dispenseCurrencyCount.put(currency, dispenseCurrencyCount.getOrDefault(currency, 0)+ withdrawAmount.intValue() / currency);
                currentCurrencyCount.put(currency, currentCurrencyCount.getOrDefault(currency,0) - withdrawAmount.intValue()/currency);
                withdrawAmount %= currency;
             }
        }
        log.debug("currentCurrencyCount" + currentCurrencyCount + " dispenseCurrencyCount" + dispenseCurrencyCount);
        return withdrawAmount;
    }

}

