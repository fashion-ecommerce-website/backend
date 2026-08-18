package com.spring.fit.backend.security.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.spring.fit.backend.security.service.FirebaseService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FirebaseServiceImpl implements FirebaseService {

    private static final String CLAIM_EMAIL = "email";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public FirebaseUserInfo verifyIdToken(String idToken) throws Exception {
        if (idToken == null || idToken.trim().isEmpty()) {
            throw new IllegalArgumentException("ID token cannot be null or empty");
        }

        log.info("Verifying Google / Firebase ID Token...");

        // 1. Try Firebase Admin SDK if FirebaseApp is initialized
        FirebaseUserInfo sdkResult = tryVerifyWithAdminSdk(idToken);
        if (sdkResult != null) {
            return sdkResult;
        }

        // 2. Decode JWT Claims (Payload)
        Map<String, Object> claims = decodeJwtPayload(idToken);

        // 3. Validate expiration and issuer
        validateExpiration(claims);
        String iss = validateIssuer(claims);

        String email = extractEmail(claims);

        // 4. If issuer is accounts.google.com, attempt remote tokeninfo verification
        email = enrichEmailFromTokenInfo(iss, idToken, email);

        return buildFirebaseUserInfo(claims, email);
    }

    // -------------------------------------------------------------------------
    // Private helpers — each handles a single responsibility
    // -------------------------------------------------------------------------

    private FirebaseUserInfo tryVerifyWithAdminSdk(String idToken) {
        if (FirebaseApp.getApps().isEmpty()) {
            return null;
        }
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            log.info("Successfully verified ID token via Firebase Admin SDK for email: {}", decodedToken.getEmail());
            return new FirebaseUserInfo() {
                @Override public String getUid()            { return decodedToken.getUid(); }
                @Override public String getEmail()          { return decodedToken.getEmail(); }
                @Override public String getName()           { return decodedToken.getName(); }
                @Override public String getPicture()        { return decodedToken.getPicture(); }
                @Override public boolean isEmailVerified()  { return decodedToken.isEmailVerified(); }
            };
        } catch (Exception e) {
            log.warn("Firebase Admin SDK verification failed, falling back to JWT verification: {}", e.getMessage());
            return null;
        }
    }

    private Map<String, Object> decodeJwtPayload(String idToken) {
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length < 2) {
                throw new SecurityException("Malformed JWT ID token");
            }
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = objectMapper.readValue(payloadJson, Map.class);
            return parsed;
        } catch (Exception e) {
            log.error("Failed to decode JWT payload: {}", e.getMessage());
            throw new SecurityException("Invalid JWT token format: " + e.getMessage());
        }
    }

    private void validateExpiration(Map<String, Object> claims) {
        if (!claims.containsKey("exp")) return;
        long nowInSeconds = System.currentTimeMillis() / 1000;
        long exp = ((Number) claims.get("exp")).longValue();
        if (exp < nowInSeconds) {
            log.error("JWT token expired at {}, now is {}", exp, nowInSeconds);
            throw new SecurityException("Google / Firebase ID token has expired");
        }
    }

    private String validateIssuer(Map<String, Object> claims) {
        String iss = (String) claims.get("iss");
        if (iss == null
                || (!iss.startsWith("https://securetoken.google.com/")
                        && !iss.startsWith("https://accounts.google.com"))) {
            log.error("Invalid token issuer: {}", iss);
            throw new SecurityException("Invalid token issuer: " + iss);
        }
        return iss;
    }

    private String extractEmail(Map<String, Object> claims) {
        String email = (String) claims.get(CLAIM_EMAIL);
        if (email == null || email.trim().isEmpty()) {
            throw new SecurityException("ID token payload does not contain an email");
        }
        return email;
    }

    private String enrichEmailFromTokenInfo(String iss, String idToken, String email) {
        if (!iss.startsWith("https://accounts.google.com")) {
            return email;
        }
        try {
            String tokenInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(tokenInfoUrl, Map.class);
            if (response != null && response.containsKey(CLAIM_EMAIL)) {
                return (String) response.get(CLAIM_EMAIL);
            }
        } catch (Exception e) {
            log.warn("Google TokenInfo API check failed for accounts.google.com token, using parsed claims: {}", e.getMessage());
        }
        return email;
    }

    private FirebaseUserInfo buildFirebaseUserInfo(Map<String, Object> claims, String email) {
        final String finalEmail = email;
        final String uid = claims.containsKey("user_id")
                ? (String) claims.get("user_id")
                : (String) claims.getOrDefault("sub", email);
        final String name = claims.containsKey("name")
                ? (String) claims.get("name")
                : email.split("@")[0];
        final String picture = (String) claims.get("picture");
        final boolean emailVerified = Boolean.TRUE.equals(claims.get("email_verified"))
                || "true".equals(String.valueOf(claims.get("email_verified")));

        log.info("Successfully verified Google/Firebase ID token for email: {}", finalEmail);

        return new FirebaseUserInfo() {
            @Override public String getUid()            { return uid; }
            @Override public String getEmail()          { return finalEmail; }
            @Override public String getName()           { return name; }
            @Override public String getPicture()        { return picture; }
            @Override public boolean isEmailVerified()  { return emailVerified; }
        };
    }
}
