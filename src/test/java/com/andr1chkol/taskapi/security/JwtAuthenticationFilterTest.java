package com.andr1chkol.taskapi.security;

import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void clearContextBeforeTest() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void clearContextAfterTest() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_whenAuthorizationHeaderIsMissing_doesNotAuthenticate() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest();
        MockHttpServletResponse response =
                new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verifyNoInteractions(jwtService, userDetailsService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_whenAuthorizationHeaderIsValid_setsAuthentication() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest();
        MockHttpServletResponse response =
                new MockHttpServletResponse();

        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer valid-token");

        UserDetails userDetails = User.withUsername("test@example.com")
                .password("bcrypt-hash").authorities("ROLE_USER").build();

        when(jwtService.extractSubject("valid-token")).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("valid-token", "test@example.com")).thenReturn(true);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        assertTrue(authentication.isAuthenticated());
        assertSame(userDetails, authentication.getPrincipal());
        assertNull(authentication.getCredentials());
        assertEquals(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                authentication.getAuthorities().stream().toList()
        );

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_whenTokenIsMalformed_doesNotAuthenticate() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest();
        MockHttpServletResponse response =
                new MockHttpServletResponse();

        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer malformed-token");

        when(jwtService.extractSubject("malformed-token")).thenThrow(new MalformedJwtException("Malformed token"));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verifyNoInteractions(userDetailsService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_whenAuthenticationAlreadyExists_doesNotReplaceIt() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest();
        MockHttpServletResponse response =
                new MockHttpServletResponse();

        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer valid-token");

        UsernamePasswordAuthenticationToken existingAuthentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        "existing-user", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

        SecurityContextHolder.getContext().setAuthentication(existingAuthentication);

        when(jwtService.extractSubject("valid-token")).thenReturn("test@example.com");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        Authentication result = SecurityContextHolder.getContext().getAuthentication();

        assertSame(existingAuthentication, result);

        verifyNoInteractions(userDetailsService);
        verify(filterChain).doFilter(request, response);
    }
}