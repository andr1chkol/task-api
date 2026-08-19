package com.andr1chkol.taskapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {
    private static final long EXPIRATION_MS = 900_000;

    private static final String TEST_SECRET = Encoders.BASE64.encode(
            "01234567890123456789012345678901"
                    .getBytes(StandardCharsets.UTF_8)
    );

    private static final String OTHER_TEST_SECRET = Encoders.BASE64.encode(
            "abcdefghijklmnopqrstuvwxyz123456"
                    .getBytes(StandardCharsets.UTF_8)
    );

    @Test
    public void generateToken_createsSignedTokenWithExpectedClaims() {
        JwtService jwtService = new JwtService(TEST_SECRET, EXPIRATION_MS);

        String token = jwtService.generateToken("test@example.com");

        SecretKey signingKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(TEST_SECRET)
        );

        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertNotNull(token);
        assertEquals("test@example.com", claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertEquals(
                EXPIRATION_MS,
                claims.getExpiration().getTime()
                        - claims.getIssuedAt().getTime()
        );
    }

    @Test
    public void extractSubject_whenTokenIsValid_returnsSubject() {
        JwtService jwtService = new JwtService(TEST_SECRET, EXPIRATION_MS);

        String token = jwtService.generateToken("test@example.com");

        String result = jwtService.extractSubject(token);

        assertEquals("test@example.com", result);
    }

    @Test
    public void isTokenValid_whenTokenIsValidAndSubjectMatches_returnsTrue() {
        JwtService jwtService = new JwtService(TEST_SECRET, EXPIRATION_MS);

        String subject = "test@example.com";

        String token = jwtService.generateToken(subject);

        assertTrue(jwtService.isTokenValid(token, subject));
    }

    @Test
    public void isTokenValid_whenSubjectDoesNotMatch_returnsFalse() {
        JwtService jwtService = new JwtService(TEST_SECRET, EXPIRATION_MS);

        String subject = "test@example.com";

        String token = jwtService.generateToken(subject);

        assertFalse(jwtService.isTokenValid(token, "invalid@example.com"));
    }

    @Test
    public void isTokenValid_whenTokenWasSignedWithAnotherKey_returnsFalse() {
        JwtService jwtService = new JwtService(TEST_SECRET, EXPIRATION_MS);
        JwtService otherJwtService = new JwtService(OTHER_TEST_SECRET, EXPIRATION_MS);

        String token = otherJwtService.generateToken("test@example.com");

        assertFalse(jwtService.isTokenValid(token, "test@example.com"));
    }

    @Test
    public void isTokenValid_whenTokenIsExpired_returnsFalse() {
        JwtService jwtService = new JwtService(TEST_SECRET, -60_000);

        String token = jwtService.generateToken("test@example.com");

        assertFalse(jwtService.isTokenValid(token, "test@example.com"));
    }

    @Test
    public void isTokenValid_whenTokenIsMalformed_returnsFalse() {
        JwtService jwtService = new JwtService(TEST_SECRET, EXPIRATION_MS);

        assertFalse(jwtService.isTokenValid("this-is-not-correct", "test@example.com"));
    }
}
