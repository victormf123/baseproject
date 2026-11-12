package coffe.baseproject.services;

import coffe.baseproject.model.User;
import coffe.baseproject.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserService userService;

    @Test
    void loadUserByUsername_shouldReturnUserWhenUsernameExists() {
        User user = new User();
        user.setUsername("john");

        when(repository.findByUsernameOrEmail("john")).thenReturn(user);

        UserDetails result = userService.loadUserByUsername("john");

        assertNotNull(result);
        assertEquals("john", result.getUsername());
    }

    @Test
    void loadUserByUsername_shouldReturnUserWhenEmailExists() {
        User user = new User();
        user.setUsername("john");
        user.setEmail("john@email.com");

        when(repository.findByUsernameOrEmail("john@email.com")).thenReturn(user);

        UserDetails result = userService.loadUserByUsername("john@email.com");

        assertNotNull(result);
        assertEquals("john", result.getUsername());
    }

    @Test
    void loadUserByUsername_shouldThrowWhenUserDoesNotExist() {
        when(repository.findByUsernameOrEmail("missing-user")).thenReturn(null);

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("missing-user")
        );

        assertTrue(exception.getMessage().contains("missing-user"));
    }
}
