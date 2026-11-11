package coffe.baseproject.services;

import coffe.baseproject.data.dto.security.AccountCredentialsDTO;
import coffe.baseproject.data.dto.security.TokenDTO;
import coffe.baseproject.exception.RequiredObjectIsNullException;
import coffe.baseproject.model.User;
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
import java.util.Date;
import java.util.List;

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

    @InjectMocks
    private AuthService authService;

    private AccountCredentialsDTO credentials;

    @BeforeEach
    void setUp() {
        credentials = new AccountCredentialsDTO("john", "123456", "John Doe");
    }

    @Test
    void signIn_shouldAuthenticateAndReturnAccessToken() {
        User user = new User();
        user.setUsername("john");
        user.setPermissions(new ArrayList<>());

        TokenDTO token = new TokenDTO("john", true, new Date(), new Date(), "access-token", "refresh-token");

        when(repository.findByUsername("john")).thenReturn(user);
        when(tokenProvider.createAccessToken("john", List.of())).thenReturn(token);

        ResponseEntity<TokenDTO> response = authService.signIn(credentials);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(repository).findByUsername("john");
        verify(tokenProvider).createAccessToken("john", List.of());
        assertEquals(ResponseEntity.ok(token), response);
    }

    @Test
    void signIn_shouldThrowWhenUserDoesNotExist() {
        when(repository.findByUsername("john")).thenReturn(null);

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> authService.signIn(credentials)
        );

        assertTrue(exception.getMessage().contains("Username john not found"));
    }

    @Test
    void refreshToken_shouldReturnTokenWhenUserExists() {
        User user = new User();
        user.setUsername("john");
        TokenDTO token = new TokenDTO("john", true, new Date(), new Date(), "new-access", "new-refresh");

        when(repository.findByUsername("john")).thenReturn(user);
        when(tokenProvider.refreshToken("refresh-token")).thenReturn(token);

        ResponseEntity<TokenDTO> response = authService.refreshToken("john", "refresh-token");

        assertEquals(ResponseEntity.ok(token), response);
        verify(tokenProvider).refreshToken("refresh-token");
    }

    @Test
    void refreshToken_shouldThrowWhenUserDoesNotExist() {
        when(repository.findByUsername("invalid-user")).thenReturn(null);

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> authService.refreshToken("invalid-user", "refresh-token")
        );

        assertTrue(exception.getMessage().contains("Username invalid-user not found"));
    }

    @Test
    void create_shouldPersistUserAndEncodePassword() {
        User savedUser = new User();
        savedUser.setFullname("John Doe");
        savedUser.setUsername("john");
        savedUser.setPassword("{pbkdf2}encodedPassword");

        when(repository.save(any(User.class))).thenReturn(savedUser);

        AccountCredentialsDTO result = authService.create(credentials);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals("john", capturedUser.getUsername());
        assertEquals("John Doe", capturedUser.getFullname());
        assertNotNull(capturedUser.getPassword());
        assertNotEquals("123456", capturedUser.getPassword());
        assertTrue(capturedUser.getAccountNonExpired());
        assertTrue(capturedUser.getAccountNonLocked());
        assertTrue(capturedUser.getCredentialsNonExpired());
        assertTrue(capturedUser.getEnabled());

        assertEquals("john", result.getUsername());
        assertEquals("John Doe", result.getFullname());
    }

    @Test
    void create_shouldThrowWhenUserIsNull() {
        RequiredObjectIsNullException exception = assertThrows(
                RequiredObjectIsNullException.class,
                () -> authService.create(null)
        );

        assertNotNull(exception);
    }
}
