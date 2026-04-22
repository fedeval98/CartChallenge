package com.fedeval.cartservicechallenge.services.impl;

import com.fedeval.cartservicechallenge.dtos.client.request.CreateClientRequest;
import com.fedeval.cartservicechallenge.dtos.client.response.ClientResponse;
import com.fedeval.cartservicechallenge.exceptions.ConflictException;
import com.fedeval.cartservicechallenge.models.Client;
import com.fedeval.cartservicechallenge.models.enums.RoleType;
import com.fedeval.cartservicechallenge.repositories.ClientRepository;
import com.fedeval.cartservicechallenge.services.ClientService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public ClientServiceImpl(ClientRepository clientRepository, PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ClientResponse createClient(CreateClientRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        String normalizedFirstName = request.getFirstName().trim();
        String normalizedLastName = request.getLastName().trim();

        if (clientRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new ConflictException("Email already registered");
        }

        Client client = Client.builder()
                .firstName(normalizedFirstName)
                .lastName(normalizedLastName)
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(RoleType.CLIENT)
                .build();

        Client savedClient = clientRepository.save(client);

        ClientResponse response = new ClientResponse();
        response.setId(savedClient.getId());
        response.setFirstName(savedClient.getFirstName());
        response.setLastName(savedClient.getLastName());
        response.setEmail(savedClient.getEmail());
        response.setRole(savedClient.getRole().name());

        return response;
    }
}
