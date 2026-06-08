package com.mycompany.customer.service.impl;

import com.mycompany.customer.dto.request.CreateCustomerRequest;
import com.mycompany.customer.dto.response.CustomerResponse;
import com.mycompany.customer.exception.ResourceNotFoundException;
import com.mycompany.customer.mapper.CustomerMapper;
import com.mycompany.customer.repository.CustomerRepository;
import com.mycompany.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    @Override
    public CustomerResponse create(CreateCustomerRequest request) {
        var entity = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse findById(Long id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> findAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public CustomerResponse update(Long id, CreateCustomerRequest request) {
        var customer = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        return mapper.toResponse(repository.save(customer));
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Customer", id);
        }
        repository.deleteById(id);
    }
}
