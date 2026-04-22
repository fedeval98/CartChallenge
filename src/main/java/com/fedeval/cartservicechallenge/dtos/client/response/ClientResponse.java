package com.fedeval.cartservicechallenge.dtos.client.response;

import lombok.Data;

@Data
public class ClientResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
}
