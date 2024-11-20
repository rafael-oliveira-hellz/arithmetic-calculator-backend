package org.exercise.core.services;

import org.exercise.core.dtos.Register;
import org.exercise.core.entities.Balance;
import org.exercise.core.entities.User;
import org.exercise.core.exceptions.BadGatewayException;
import org.exercise.core.exceptions.InternalErrorException;
import org.exercise.core.interfaces.UserService;
import org.exercise.infrastructure.persistence.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final PasswordEncoder passwordEncoder;

    private static final String POOL_ID = System.getenv("COGNITO_USER_POOL_ID");

    private final UserRepository userRepository;
    private final CognitoIdentityProviderClient cognitoClient;

    @Autowired
    public UserServiceImpl(PasswordEncoder passwordEncoder, UserRepository userRepository, CognitoIdentityProviderClient cognitoClient) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.cognitoClient = cognitoClient;
    }

    @Transactional
    public void registerUser(Register register) {
        logger.info("Starting user registration. Username: {}, Email: {}", register.username(), register.email());

        logger.info("Creating user at Cognito...");
        AdminCreateUserRequest createUserRequest = getCreateUserRequest(register.username(), register.email());
        AdminCreateUserResponse createUserResponse = cognitoClient.adminCreateUser(createUserRequest);
        logger.info("User created successfully. Cognito ID: {}", createUserResponse.user().username());

        logger.info("Defining password at Cognito...");
        setPermanentPassword(register.username(), register.password());
        logger.info("Password successfully defined at Cognito for the user: {}", register.username());

        logger.info("Saving user in the database...");
        User user = getUser(register.username(), register.email(), register.password(), createUserResponse);
        userRepository.save(user);
        logger.info("User successfully saved in the database. User ID: {}", user.getId());
    }

    private User getUser(String username, String email, String password, AdminCreateUserResponse createUserResponse) {
        logger.info("Creating user entity for persistence...");
        String cognitoUserId = getCognitoSub(createUserResponse.user().username());
        Balance balance = new Balance(1000);
        logger.info("User entity successfully created. Cognito ID: {}", cognitoUserId);
        logger.info("Encrypting user password...");
        String encryptedPassword = encryptPassword(password);
        logger.info("Password encrypted");
        return new User(username, encryptedPassword, email, UUID.fromString(cognitoUserId), balance);
    }

    String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }

    AdminCreateUserRequest getCreateUserRequest(String username, String email) {
        logger.info("Building AdminCreateUserRequest for user. Username: {}, Email: {}", username, email);
        return AdminCreateUserRequest.builder()
                .userPoolId(POOL_ID)
                .username(username)
                .userAttributes(
                        AttributeType.builder()
                                .name("email")
                                .value(email)
                                .build(),
                        AttributeType.builder()
                                .name("email_verified")
                                .value("true")
                                .build()
                )
                .build();
    }

    void setPermanentPassword(String username, String password) {
        logger.info("Building AdminSetUserPasswordRequest for user. Username: {}", username);
        AdminSetUserPasswordRequest setPasswordRequest = AdminSetUserPasswordRequest.builder()
                .userPoolId(POOL_ID)
                .username(username)
                .password(password)
                .permanent(true)
                .build();
        cognitoClient.adminSetUserPassword(setPasswordRequest);
        logger.info("Password successfully defined for user: {}", username);
    }

    String getCognitoSub(String username) {
        try {
            AdminGetUserRequest getUserRequest = AdminGetUserRequest.builder()
                    .userPoolId(POOL_ID)
                    .username(username)
                    .build();

            AdminGetUserResponse getUserResponse = cognitoClient.adminGetUser(getUserRequest);
            return getUserResponse.userAttributes().stream()
                    .filter(attr -> "sub".equals(attr.name()))
                    .map(AttributeType::value)
                    .findFirst()
                    .orElseThrow(() -> new InternalErrorException("Sub attribute not found for user: " + username));
        } catch (Exception e) {
            throw new BadGatewayException("Error fetching sub attribute for user: " + username +
                    ". Error on the origin: " + e);
        }
    }

}