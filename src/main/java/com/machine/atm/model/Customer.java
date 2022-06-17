package com.machine.atm.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table
public class Customer {

    @Id
    private Long accountNumber;
    private Integer pinNumber;
    private Double accountBalance;
    private Double overDraft;

}
