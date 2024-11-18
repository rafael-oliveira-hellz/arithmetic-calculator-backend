package org.exercise.core.services;

import org.exercise.core.dtos.Register;
import org.exercise.core.entities.Balance;
import org.exercise.core.entities.User;
import org.exercise.core.interfaces.UserService;
import org.exercise.infrastructure.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private static final String POOL_ID = System.getenv("COGNITO_USER_POOL_ID");
    private static final String INITIAL_AMOUNT = System.getenv("INITIAL_AMOUNT");


    private final UserRepository userRepository;
    private final CognitoIdentityProviderClient cognitoClient;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, CognitoIdentityProviderClient cognitoClient) {
        this.userRepository = userRepository;
        this.cognitoClient = cognitoClient;
    }

    @Transactional
    public void registerUser(Register register) {
        logger.info("Iniciando o registro do usuário. Username: {}, Email: {}", register.username(), register.email());

        logger.info("Criando usuário no Cognito...");
        AdminCreateUserRequest createUserRequest = getCreateUserRequest(register.username(), register.email());
        AdminCreateUserResponse createUserResponse = cognitoClient.adminCreateUser(createUserRequest);
        logger.info("Usuário criado no Cognito com sucesso. Cognito ID: {}", createUserResponse.user().username());

        logger.info("Definindo senha permanente no Cognito...");
        setPermanentPassword(register.username(), register.password());
        logger.info("Senha permanente definida com sucesso no Cognito para o usuário: {}", register.username());

        logger.info("Salvando usuário no banco de dados...");
        try {
            User user = getUser(register.username(), register.email(), register.password(), createUserResponse);

            userRepository.save(user);

            logger.info("Usuário salvo no banco de dados com sucesso. User ID: {}", user.getId());
        } catch (Exception e) {
            logger.error("Erro ao salvar usuário no banco de dados: ", e);
            throw e;
        }
    }

    private User getUser(String username, String email, String password, AdminCreateUserResponse createUserResponse) {
        logger.info("Criando entidade User para persistência...");
        String cognitoUserId = getCognitoSub(createUserResponse.user().username());
        Balance balance = new Balance(Integer.parseInt(INITIAL_AMOUNT));
        logger.info("Entidade User criada com sucesso. Cognito ID: {}", cognitoUserId);
        return new User(username, password, email, cognitoUserId, balance);
    }

    private AdminCreateUserRequest getCreateUserRequest(String username, String email) {
        logger.info("Construindo AdminCreateUserRequest para o usuário. Username: {}, Email: {}", username, email);
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

    private void setPermanentPassword(String username, String password) {
        logger.info("Construindo AdminSetUserPasswordRequest para o usuário. Username: {}", username);
        AdminSetUserPasswordRequest setPasswordRequest = AdminSetUserPasswordRequest.builder()
                .userPoolId(POOL_ID)
                .username(username)
                .password(password)
                .permanent(true)
                .build();
        cognitoClient.adminSetUserPassword(setPasswordRequest);
        logger.info("Senha permanente definida com sucesso para o usuário: {}", username);
    }

    private String getCognitoSub(String username) {
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
                    .orElseThrow(() -> new RuntimeException("Sub attribute not found for user: " + username));
        } catch (Exception e) {
            throw new RuntimeException("Error fetching sub attribute for user: " + username, e);
        }
    }

}