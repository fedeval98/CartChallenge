package com.fedeval.cartservicechallenge.configs.security;

import com.fedeval.cartservicechallenge.models.Client;
import com.fedeval.cartservicechallenge.repositories.ClientRepository;
import lombok.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final ClientRepository clientRepository;

    public CustomUserDetailsService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Client client = clientRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Unknown user"));

        return User.builder()
                .username(client.getEmail())
                .password(client.getPassword())
                .roles(client.getRole().name())
                .build();
    }
}
