package com.mycompany.customer.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
}
