package coffe.baseproject.services;

import coffe.baseproject.data.dto.security.AccountCredentialsDTO;
import coffe.baseproject.data.dto.security.TokenDTO;
import coffe.baseproject.data.dto.security.UserRegistrationResponseDTO;
import coffe.baseproject.model.Profile;
import coffe.baseproject.exception.RequiredObjectIsNullException;
import coffe.baseproject.exception.UserAlreadyExistsException;
import coffe.baseproject.model.Permission;
import coffe.baseproject.model.User;
import coffe.baseproject.repository.PermissionRepository;
import coffe.baseproject.repository.UserRepository;
import coffe.baseproject.security.jwt.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthService {

    Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserRepository repository;

    @Autowired
    private PermissionRepository permissionRepository;

    public ResponseEntity<TokenDTO> signIn(AccountCredentialsDTO credentials) {
        String login = credentials.getUsername() != null && !credentials.getUsername().isBlank()
                ? credentials.getUsername()
                : credentials.getEmail();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        login,
                        credentials.getPassword()
                )
        );

        var user = repository.findByUsernameOrEmail(login);
        if (user == null) {
            throw new UsernameNotFoundException("Username or email " + login + " not found!");
        }

        var token = tokenProvider.createAccessToken(
                user.getUsername(),
                user.getRoles()
        );
        return ResponseEntity.ok(token);
    }

    public ResponseEntity<TokenDTO> refreshToken(String username, String refreshToken) {
        var user = repository.findByUsernameOrEmail(username);
        TokenDTO token;
        if (user != null) {
            token = tokenProvider.refreshToken(refreshToken);
        } else {
            throw new UsernameNotFoundException("Username or email " + username + " not found!");
        }
        return ResponseEntity.ok(token);
    }

    @Transactional
    public UserRegistrationResponseDTO create(AccountCredentialsDTO user) {

        if (user == null) throw new RequiredObjectIsNullException();

        if (repository.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistsException("Username is already registered");
        }
        if (repository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("Email is already registered");
        }

        logger.info("Creating one new User!");
        var entity = new User();
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setPassword(generateHashedPassword(user.getPassword()));
        entity.setAccountNonExpired(true);
        entity.setAccountNonLocked(true);
        entity.setCredentialsNonExpired(true);
        entity.setEnabled(true);
        entity.setProfile(new Profile());
        Permission userPermission = permissionRepository.findFirstByDescription("usuario")
                .orElseThrow(() -> new IllegalStateException(
                        "Required permission 'usuario' was not found"
                ));
        entity.setPermissions(List.of(userPermission));

        var dto = repository.save(entity);
        return new UserRegistrationResponseDTO(dto.getId(), dto.getUsername(), dto.getEmail());
    }

    private String generateHashedPassword(String password) {

        PasswordEncoder pbkdf2Encoder = new Pbkdf2PasswordEncoder(
                "", 8, 185000,
                Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);

        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("pbkdf2", pbkdf2Encoder);
        DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder("pbkdf2", encoders);

        passwordEncoder.setDefaultPasswordEncoderForMatches(pbkdf2Encoder);
        return passwordEncoder.encode(password);
    }
}
