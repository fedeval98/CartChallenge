package com.fedeval.cartservicechallenge.configs.security;

import com.fedeval.cartservicechallenge.models.Client;
import com.fedeval.cartservicechallenge.models.enums.RoleType;
import com.fedeval.cartservicechallenge.repositories.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void should_load_user_by_username() {
        Client client = new Client();
        client.setEmail("test@mail.com");
        client.setPassword("1234");
        client.setRole(RoleType.CLIENT);

        when(clientRepository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(client));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@mail.com");

        assertNotNull(userDetails);
        assertEquals("test@mail.com", userDetails.getUsername());
        assertEquals("1234", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT")));
    }

    @Test
    void should_throw_exception_when_user_not_found() {
        when(clientRepository.findByEmail("unknown@mail.com"))
                .thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("unknown@mail.com")
        );

        assertEquals("Unknown user", ex.getMessage());
    }
}