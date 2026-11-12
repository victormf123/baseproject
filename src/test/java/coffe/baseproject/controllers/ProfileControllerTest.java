package coffe.baseproject.controllers;

import coffe.baseproject.config.SecurityConfig;
import coffe.baseproject.data.dto.profile.ProfileResponseDTO;
import coffe.baseproject.data.dto.profile.ProfileUpdateRequestDTO;
import coffe.baseproject.exception.ResourceNotFoundException;
import coffe.baseproject.security.jwt.JwtTokenProvider;
import coffe.baseproject.services.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
@Import(SecurityConfig.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private JwtTokenProvider tokenProvider;

    private ProfileResponseDTO profileResponse;

    @BeforeEach
    void setUp() {
        when(tokenProvider.resolveToken(any(HttpServletRequest.class))).thenAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            String header = request.getHeader("Authorization");
            return header != null && header.startsWith("Bearer ") ? header.substring(7) : null;
        });
        when(tokenProvider.validateToken("valid-token")).thenReturn(true);
        when(tokenProvider.getAuthentication("valid-token")).thenReturn(
                new UsernamePasswordAuthenticationToken(
                        "profile-owner", null, List.of(new SimpleGrantedAuthority("usuario"))));

        profileResponse = new ProfileResponseDTO(
                15L, "Jane Doe", "+5511999999999", "12345678901", null,
                "Main Street, 10", "01000000", false);
    }

    @Test
    void getProfile_shouldReturnProfile() throws Exception {
        when(profileService.findByUserId(7L)).thenReturn(profileResponse);

        mockMvc.perform(get("/profiles/user/7").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(15))
                .andExpect(jsonPath("$.fullName").value("Jane Doe"))
                .andExpect(jsonPath("$.phone").value("+5511999999999"))
                .andExpect(jsonPath("$.company").value(false))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    void getProfile_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/profiles/user/7"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getProfile_shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        when(profileService.findByUserId(7L)).thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/profiles/user/7").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void getProfile_shouldReturnNotFoundWhenProfileDoesNotExist() throws Exception {
        when(profileService.findByUserId(7L)).thenThrow(new ResourceNotFoundException("Profile not found"));

        mockMvc.perform(get("/profiles/user/7").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Profile not found"));
    }

    @Test
    void updateProfile_shouldReturnUpdatedProfile() throws Exception {
        when(profileService.updateByUserId(any(Long.class), any(ProfileUpdateRequestDTO.class)))
                .thenReturn(profileResponse);

        mockMvc.perform(put("/profiles/user/7")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Jane Doe","phone":"+5511999999999","company":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(15))
                .andExpect(jsonPath("$.fullName").value("Jane Doe"));
    }

    @Test
    void updateProfile_shouldReturnBadRequestWhenDataIsInvalid() throws Exception {
        String tooLongValue = "x".repeat(256);

        mockMvc.perform(put("/profiles/user/7")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"" + tooLongValue + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProfile_shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        when(profileService.updateByUserId(any(Long.class), any(ProfileUpdateRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(put("/profiles/user/7")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Jane Doe\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void updateProfile_shouldReturnNotFoundWhenProfileDoesNotExist() throws Exception {
        when(profileService.updateByUserId(any(Long.class), any(ProfileUpdateRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Profile not found"));

        mockMvc.perform(put("/profiles/user/7")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Jane Doe\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Profile not found"));
    }
}
