package com.mycompany.customer.service;

import com.mycompany.customer.dto.request.CreateCustomerRequest;
import com.mycompany.customer.dto.response.CustomerResponse;

import java.util.List;

public interface CustomerService {

    CustomerResponse create(CreateCustomerRequest request);
    CustomerResponse findById(Long id);
    List<CustomerResponse> findAll();
    CustomerResponse update(Long id, CreateCustomerRequest request);
    void delete(Long id);
}
