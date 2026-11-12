package coffe.baseproject;

import coffe.baseproject.data.dto.security.AccountCredentialsDTO;
import coffe.baseproject.data.dto.security.UserRegistrationResponseDTO;
import coffe.baseproject.exception.UserAlreadyExistsException;
import coffe.baseproject.services.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@AutoConfigureMockMvc
class BaseprojectApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

	@Test
	void contextLoads() {
	}

    @Test
    void createUserPreflight_shouldAllowConfiguredFrontend() throws Exception {
        mockMvc.perform(options("/api/auth/createUser")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "authorization,content-type,accept"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Methods",
                        org.hamcrest.Matchers.containsString("POST")))
                .andExpect(header().string("Access-Control-Allow-Headers",
                        org.hamcrest.Matchers.containsString("authorization")));
    }

    @Test
    void createUser_shouldReachControllerFromConfiguredFrontend() throws Exception {
        when(authService.create(any(AccountCredentialsDTO.class)))
                .thenReturn(new UserRegistrationResponseDTO(10L, "new-user", "new-user@example.com"));

        mockMvc.perform(post("/api/auth/createUser")
                        .header("Origin", "http://localhost:3000")
                        .header("Authorization", "Bearer invalid-stale-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"new-user","password":"secret","email":"new-user@example.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));

        verify(authService).create(any(AccountCredentialsDTO.class));
    }

    @Test
    void createUser_shouldReturnConflictWhenUsernameOrEmailAlreadyExists() throws Exception {
        when(authService.create(any(AccountCredentialsDTO.class)))
                .thenThrow(new UserAlreadyExistsException("Username is already registered"));

        mockMvc.perform(post("/api/auth/createUser")
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"existing-user","password":"secret","email":"existing@example.com"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username is already registered"));
    }

    @Test
    void profileEndpoints_shouldBePresentInOpenApiDocument() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/profiles/user/{userId}'].get").exists())
                .andExpect(jsonPath("$.paths['/profiles/user/{userId}'].put").exists());
    }

}
