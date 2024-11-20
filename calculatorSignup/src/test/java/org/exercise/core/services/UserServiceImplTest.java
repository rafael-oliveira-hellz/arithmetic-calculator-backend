package org.exercise.core.services;

import org.exercise.core.dtos.Register;
import org.exercise.core.entities.Balance;
import org.exercise.core.entities.User;
import org.exercise.core.exceptions.BadGatewayException;
import org.exercise.infrastructure.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CognitoIdentityProviderClient cognitoClient;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        System.setProperty("INITIAL_AMOUNT", "1000");
        System.setProperty("COGNITO_USER_POOL_ID", "testPoolId");
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        Register register = new Register("testUser", "SecurePass1!", "test@example.com");

        AdminCreateUserResponse createUserResponse = mock(AdminCreateUserResponse.class);
        UserType userType = mock(UserType.class);

        when(createUserResponse.user()).thenReturn(userType);
        when(userType.username()).thenReturn("testCognitoId");

        // Mock the behavior of adminCreateUser
        when(cognitoClient.adminCreateUser(any(AdminCreateUserRequest.class))).thenReturn(createUserResponse);

        // Mock the behavior of adminGetUser to return the 'sub' attribute
        when(cognitoClient.adminGetUser((AdminGetUserRequest) any())).thenReturn(
                AdminGetUserResponse.builder()
                        .userAttributes(AttributeType.builder().name("sub").value(UUID.randomUUID().toString()).build())
                        .build()
        );

        // Mock the behavior of passwordEncoder
        when(passwordEncoder.encode(anyString())).thenReturn("encryptedPassword");

        // Act
        userService.registerUser(register);

        // Assert
        verify(userRepository, times(1)).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(register.username(), savedUser.getUsername());
        assertEquals(register.email(), savedUser.getEmail());
        assertEquals("encryptedPassword", savedUser.getPassword());
        assertNotNull(savedUser.getBalance());
        assertEquals(1000, savedUser.getBalance().getAmount());
    }


    @Test
    void shouldThrowBadGatewayErrorExceptionWhenSubAttributeNotFound() {
        when(cognitoClient.adminGetUser((AdminGetUserRequest) any())).thenReturn(
                AdminGetUserResponse.builder().userAttributes(anyCollection()).build()
        );

        assertThrows(BadGatewayException.class, () -> userService.getCognitoSub("testUser"));
    }

    @Test
    void shouldThrowBadGatewayExceptionWhenCognitoClientFails() {
        // Arrange
        when(cognitoClient.adminGetUser((AdminGetUserRequest) any())).thenThrow(RuntimeException.class);

        // Act & Assert
        assertThrows(BadGatewayException.class, () -> userService.getCognitoSub("testUser"));
    }

    @Test
    void shouldEncryptPasswordSuccessfully() {
        // Arrange
        String rawPassword = "SecurePass1!";
        when(passwordEncoder.encode(rawPassword)).thenReturn("encryptedPassword");

        // Act
        String encryptedPassword = userService.encryptPassword(rawPassword);

        // Assert
        assertEquals("encryptedPassword", encryptedPassword);
    }

    @Test
    void shouldBuildAdminCreateUserRequest() {
        AdminCreateUserRequest request = userService.getCreateUserRequest("testUser", "test@example.com");

        assertNotNull(request);
        assertEquals("testUser", request.username());
        assertEquals(2, request.userAttributes().size());
        assertEquals("email", request.userAttributes().getFirst().name());
        assertEquals("test@example.com", request.userAttributes().getFirst().value());
    }

    @Test
    void shouldSetPermanentPasswordSuccessfully() {
        String username = "testUser";
        String password = "SecurePass1!";

        when(cognitoClient.adminSetUserPassword(any(AdminSetUserPasswordRequest.class))).thenReturn(null); // Mock the actual return
        userService.setPermanentPassword(username, password);

        verify(cognitoClient).adminSetUserPassword(argThat((AdminSetUserPasswordRequest actualRequest) -> {
            return actualRequest.username().equals(username) &&
                    actualRequest.password().equals(password) &&
                    actualRequest.permanent();
        }));
    }


}
