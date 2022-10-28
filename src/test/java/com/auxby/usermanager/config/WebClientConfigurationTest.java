package com.auxby.usermanager.config;

import com.auxby.usermanager.config.properties.KeycloakProps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebClientConfigurationTest {

    private WebClientConfiguration webClientConfiguration;
    @Mock
    private KeycloakProps keycloakProps;

    @BeforeEach
    void setup() {
        webClientConfiguration = new WebClientConfiguration(keycloakProps);
    }

    @Test
    void webClient() {
        when(keycloakProps.getUrl())
                .thenReturn("Test");
        var result = webClientConfiguration.webClient();
        assertNotNull(result);
    }
}