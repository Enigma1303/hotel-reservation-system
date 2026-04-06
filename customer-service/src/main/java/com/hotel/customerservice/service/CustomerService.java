package com.hotel.customerservice.service;

import com.hotel.customerservice.dto.CustomerRequest;
import com.hotel.customerservice.dto.CustomerResponse;
import com.hotel.customerservice.entity.Customer;
import com.hotel.customerservice.exception.CustomerNotFoundException;
import com.hotel.customerservice.exception.EmailAlreadyExistsException;
import com.hotel.customerservice.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerResponse createCustomer(CustomerRequest request) {
        log.info("Creating customer with email: {}", request.getEmail());

        if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
            log.error("Email already exists: {}", request.getEmail());
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());

        Customer saved = customerRepository.save(customer);

        log.info("Customer created with id: {}", saved.getId());

        return new CustomerResponse(saved.getId(), saved.getName(), saved.getEmail());
    }

    public CustomerResponse getCustomerById(Long id) {
        log.info("Fetching customer with id: {}", id);
        
        if(id == null) {
            log.error("Customer id cannot be null");
            throw new IllegalArgumentException("Customer id cannot be null");
        }
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Customer not found with id: {}", id);
                    return new CustomerNotFoundException(id);
                });

        log.info("Customer found with id: {}", id);

        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail());
    }
}