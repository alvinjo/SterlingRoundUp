package com.alvin.common.utils;

import com.alvin.common.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class is a placeholder for a security context implementation.
 * Ideally this is a static class with a util method for accessing the User in the security context and returning the access token
 */
@Component
public class SecurityUtils {

    @Autowired
    private UserRepo userRepo;

    public String getUserAccessToken() {
        return userRepo.findById("123").get().getEncryptedAccessToken();
    }

}
