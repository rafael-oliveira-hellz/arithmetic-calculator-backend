package org.exercise.core.services;

import org.exercise.core.interfaces.CognitoAuthService;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class CognitoAuthServiceImpl implements CognitoAuthService {
    /**
     * @param clientId
     * @param clientSecret
     * @param username
     * @return
     */
    @Override
    public String calculateSecretHash(String clientId, String clientSecret, String username) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(clientSecret.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal((username + clientId).getBytes());
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Error while calculating SECRET_HASH", e);
        }
    }
}
