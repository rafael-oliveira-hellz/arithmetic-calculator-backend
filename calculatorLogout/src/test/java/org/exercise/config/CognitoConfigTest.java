package org.exercise.config;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CognitoConfigTest {

    @Test
    void testCognitoClient() throws Exception {
        CognitoConfig cognitoConfig = new CognitoConfig();
        String testRegion = "us-west-2";

        Field regionField = CognitoConfig.class.getDeclaredField("region");
        regionField.setAccessible(true);
        regionField.set(cognitoConfig, testRegion);

        CognitoIdentityProviderClient cognitoClient = cognitoConfig.cognitoClient();

        assertNotNull(cognitoClient, "CognitoIdentityProviderClient should not be null");
        Region configuredRegion = cognitoClient.serviceClientConfiguration().region();
        assertNotNull(configuredRegion, "Region should be configured");
        assert (configuredRegion.toString().equals(testRegion));
    }
}