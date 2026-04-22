package com.fedeval.cartservicechallenge.services;

import com.fedeval.cartservicechallenge.dtos.client.request.CreateClientRequest;
import com.fedeval.cartservicechallenge.dtos.client.response.ClientResponse;

public interface ClientService {
    ClientResponse createClient (CreateClientRequest request);
}
