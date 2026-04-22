package com.fedeval.cartservicechallenge.dtos.client.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ClientResponse {
    Long id;
    String firstName;
    String lastName;
    String email;
    String role;
}
