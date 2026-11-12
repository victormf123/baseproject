package coffe.baseproject.services;

import coffe.baseproject.data.dto.security.AccountCredentialsDTO;
import coffe.baseproject.data.dto.security.TokenDTO;
import coffe.baseproject.data.dto.security.UserRegistrationResponseDTO;
import coffe.baseproject.exception.RequiredObjectIsNullException;
import coffe.baseproject.exception.UserAlreadyExistsException;
import coffe.baseproject.model.Permission;
import coffe.baseproject.model.User;
import coffe.baseproject.repository.PermissionRepository;
import coffe.baseproject.repository.UserRepository;
import coffe.baseproject.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserRepository repository;

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private AuthService authService;

    private AccountCredentialsDTO credentials;

    @BeforeEach
    void setUp() {
        credentials = new AccountCredentialsDTO("john", "123456", "john@email.com");
    }

    @Test
    void signIn_shouldAuthenticateAndReturnAccessToken() {
        User user = new User();
        user.setUsername("john");
        user.setEmail("john@email.com");
        user.setPermissions(new ArrayList<>());

        TokenDTO token = new TokenDTO("john", true, new Date(), new Date(), "access-token", "refresh-token");

        when(repository.findByUsernameOrEmail("john")).thenReturn(user);
        when(tokenProvider.createAccessToken("john", List.of())).thenReturn(token);

        ResponseEntity<TokenDTO> response = authService.signIn(credentials);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(repository).findByUsernameOrEmail("john");
        verify(tokenProvider).createAccessToken("john", List.of());
        assertEquals(ResponseEntity.ok(token), response);
    }

    @Test
    void signIn_shouldAuthenticateWithEmail() {
        User user = new User();
        user.setUsername("john");
        user.setEmail("john@email.com");
        user.setPermissions(new ArrayList<>());

        TokenDTO token = new TokenDTO("john", true, new Date(), new Date(), "access-token", "refresh-token");

        credentials.setUsername(null);
        credentials.setEmail("john@email.com");

        when(repository.findByUsernameOrEmail("john@email.com")).thenReturn(user);
        when(tokenProvider.createAccessToken("john", List.of())).thenReturn(token);

        ResponseEntity<TokenDTO> response = authService.signIn(credentials);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(repository).findByUsernameOrEmail("john@email.com");
        verify(tokenProvider).createAccessToken("john", List.of());
        assertEquals(ResponseEntity.ok(token), response);
    }

    @Test
    void signIn_shouldThrowWhenUserDoesNotExist() {
        when(repository.findByUsernameOrEmail("john")).thenReturn(null);

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> authService.signIn(credentials)
        );

        assertTrue(exception.getMessage().contains("Username or email john not found"));
    }

    @Test
    void refreshToken_shouldReturnTokenWhenUserExists() {
        User user = new User();
        user.setUsername("john");
        TokenDTO token = new TokenDTO("john", true, new Date(), new Date(), "new-access", "new-refresh");

        when(repository.findByUsernameOrEmail("john")).thenReturn(user);
        when(tokenProvider.refreshToken("refresh-token")).thenReturn(token);

        ResponseEntity<TokenDTO> response = authService.refreshToken("john", "refresh-token");

        assertEquals(ResponseEntity.ok(token), response);
        verify(tokenProvider).refreshToken("refresh-token");
    }

    @Test
    void refreshToken_shouldThrowWhenUserDoesNotExist() {
        when(repository.findByUsernameOrEmail("invalid-user")).thenReturn(null);

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> authService.refreshToken("invalid-user", "refresh-token")
        );

        assertTrue(exception.getMessage().contains("Username or email invalid-user not found"));
    }

    @Test
    void create_shouldPersistUserAndEncodePassword() {
        Permission userPermission = new Permission();
        userPermission.setId(1L);
        userPermission.setDescription("usuario");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("john");
        savedUser.setEmail("john@email.com");
        savedUser.setPassword("{pbkdf2}encodedPassword");

        when(permissionRepository.findFirstByDescription("usuario")).thenReturn(Optional.of(userPermission));
        when(repository.save(any(User.class))).thenReturn(savedUser);

        UserRegistrationResponseDTO result = authService.create(credentials);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals("john", capturedUser.getUsername());
        assertEquals("john@email.com", capturedUser.getEmail());
        assertNotNull(capturedUser.getPassword());
        assertNotEquals("123456", capturedUser.getPassword());
        assertTrue(capturedUser.getAccountNonExpired());
        assertTrue(capturedUser.getAccountNonLocked());
        assertTrue(capturedUser.getCredentialsNonExpired());
        assertTrue(capturedUser.getEnabled());
        assertEquals(List.of(userPermission), capturedUser.getPermissions());
        assertNotNull(capturedUser.getProfile());
        assertNull(capturedUser.getProfile().getFullName());
        assertNull(capturedUser.getProfile().getPhone());
        assertNull(capturedUser.getProfile().getCpf());
        assertNull(capturedUser.getProfile().getCnpj());
        assertNull(capturedUser.getProfile().getAddress());
        assertNull(capturedUser.getProfile().getZipCode());
        assertNull(capturedUser.getProfile().getCompany());
        assertSame(capturedUser, capturedUser.getProfile().getUser());

        assertEquals(1L, result.getId());
        assertEquals("john", result.getUsername());
        assertEquals("john@email.com", result.getEmail());
        assertFalse(resultJsonContainsPassword(result));
    }

    @Test
    void create_shouldRejectExistingUsername() {
        when(repository.existsByUsername("john")).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> authService.create(credentials)
        );

        assertEquals("Username is already registered", exception.getMessage());
        verify(repository, never()).save(any(User.class));
        verifyNoInteractions(permissionRepository);
    }

    @Test
    void create_shouldRejectExistingEmail() {
        when(repository.existsByEmail("john@email.com")).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> authService.create(credentials)
        );

        assertEquals("Email is already registered", exception.getMessage());
        verify(repository, never()).save(any(User.class));
        verifyNoInteractions(permissionRepository);
    }

    @Test
    void create_shouldFailWhenDefaultUserPermissionDoesNotExist() {
        when(permissionRepository.findFirstByDescription("usuario")).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> authService.create(credentials)
        );

        assertTrue(exception.getMessage().contains("permission 'usuario'"));
        verify(repository, never()).save(any(User.class));
    }

    @Test
    void create_shouldRunWithinTransaction() throws NoSuchMethodException {
        Method createMethod = AuthService.class.getMethod("create", AccountCredentialsDTO.class);

        assertTrue(createMethod.isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class));
    }

    @Test
    void create_shouldThrowWhenUserIsNull() {
        RequiredObjectIsNullException exception = assertThrows(
                RequiredObjectIsNullException.class,
                () -> authService.create(null)
        );

        assertNotNull(exception);
    }

    private boolean resultJsonContainsPassword(UserRegistrationResponseDTO result) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(result)
                    .contains("password");
        } catch (com.fasterxml.jackson.core.JsonProcessingException exception) {
            throw new AssertionError("Could not serialize registration response", exception);
        }
    }
}
