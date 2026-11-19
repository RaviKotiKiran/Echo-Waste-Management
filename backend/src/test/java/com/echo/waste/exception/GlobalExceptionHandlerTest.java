package com.echo.waste.exception;

import com.echo.waste.dto.ApiDtos;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsAccessDeniedToForbidden() {
        ResponseEntity<ApiDtos.Message> response = handler.accessDenied(new AccessDeniedException("denied"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isEqualTo(
                new ApiDtos.Message("You do not have permission to perform this action."));
    }

    @Test
    void mapsAuthenticationFailureToUnauthorized() {
        ResponseEntity<ApiDtos.Message> response =
                handler.authentication(new InsufficientAuthenticationException("missing authentication"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo(new ApiDtos.Message("Authentication required."));
    }
}
