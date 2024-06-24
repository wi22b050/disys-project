package com.example.Customer;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Charge {

    @Id
    private int id;

    private Double kwh;

    private Integer customerId;
}
