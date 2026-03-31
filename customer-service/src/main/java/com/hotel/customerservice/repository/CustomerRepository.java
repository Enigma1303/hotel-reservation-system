package com.hotel.customerservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotel.customerservice.entity.Customer;



public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer>findByEmail(String email);
    


}
