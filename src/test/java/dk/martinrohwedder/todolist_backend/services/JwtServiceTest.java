package dk.martinrohwedder.todolist_backend.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
    @Mock
    private JwtEncoder jwtEncoder;

    @InjectMocks
    private JwtService jwtService;

    // *********************************************************
    // Helper Method
    // *********************************************************

    private Jwt createMockJwt() {
        return Jwt.withTokenValue("test-jwt-token")
                .header("alg", "RS256")
                .claim("sub", "testuser")
                .build();
    }

    // *********************************************************
    // generateToken()
    // *********************************************************

    @Test
    void generateToken_shouldReturnEncodedToken() {
        UserDetails user = User.withUsername("testuser")
                .password("password")
                .roles("USER")
                .build();

        Jwt jwt = createMockJwt();

        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(jwt);

        String result = jwtService.generateToken(user);

        assertThat(result).isEqualTo("test-jwt-token");

        verify(jwtEncoder).encode(any(JwtEncoderParameters.class));
    }

    @Test
    void generateToken_shouldCreateCorrectClaims() {
        UserDetails user = User.withUsername("testuser")
                .password("password")
                .roles("USER")
                .build();

        Jwt jwt = createMockJwt();

        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(jwt);

        Instant before = Instant.now();

        jwtService.generateToken(user);

        Instant after = Instant.now();

        ArgumentCaptor<JwtEncoderParameters> captor =
                ArgumentCaptor.forClass(JwtEncoderParameters.class);

        verify(jwtEncoder).encode(captor.capture());

        JwtClaimsSet claims = captor.getValue().getClaims();

        assertThat(claims.getClaimAsString("iss"))
                .isEqualTo("todolist-backend");

        assertThat(claims.getClaimAsString("sub"))
                .isEqualTo("testuser");

        assertThat(claims.getClaimAsString("scope"))
                .isEqualTo("ROLE_USER");

        assertThat(claims.getIssuedAt())
                .isBetween(before, after);

        assertThat(claims.getExpiresAt())
                .isBetween(
                        before.plus(1, ChronoUnit.HOURS),
                        after.plus(1, ChronoUnit.HOURS)
                );
    }

    @Test
    void generateToken_shouldIncludeAllUserAuthoritiesInScope() {
        UserDetails user = User.withUsername("testuser")
                .password("password")
                .authorities("ROLE_USER", "ROLE_ADMIN")
                .build();

        Jwt jwt = createMockJwt();

        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(jwt);

        jwtService.generateToken(user);

        ArgumentCaptor<JwtEncoderParameters> captor =
                ArgumentCaptor.forClass(JwtEncoderParameters.class);

        verify(jwtEncoder).encode(captor.capture());

        JwtClaimsSet claims = captor.getValue().getClaims();

        assertThat(claims.getClaimAsString("scope"))
                .contains("ROLE_USER", "ROLE_ADMIN");
    }
}