package com.mycompany.customer.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCustomerRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    private String phone;
}
