package com.spring.fit.backend.security.service;

public interface FirebaseService {

    FirebaseUserInfo verifyIdToken(String idToken) throws Exception;
    
    interface FirebaseUserInfo {
        String getUid();
        String getEmail();
        String getName();
        String getPicture();
        boolean isEmailVerified();
    }
}
