package coffe.baseproject.services;

import coffe.baseproject.data.dto.profile.ProfileResponseDTO;
import coffe.baseproject.data.dto.profile.ProfileUpdateRequestDTO;
import coffe.baseproject.exception.ResourceNotFoundException;
import coffe.baseproject.model.Profile;
import coffe.baseproject.model.User;
import coffe.baseproject.repository.ProfileRepository;
import coffe.baseproject.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private ProfileService profileService;

    private Profile profile;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(7L);
        user.setUsername("profile-owner");
        user.setEmail("owner@example.com");
        user.setPassword("encoded-password");
        user.setEnabled(true);

        profile = new Profile();
        profile.setUser(user);
        profile.setFullName("Original Name");
        profile.setPhone("111111111");
        profile.setCpf("11111111111");
        profile.setCnpj("11111111000111");
        profile.setAddress("Original address");
        profile.setZipCode("02000000");
        profile.setCompany(false);
    }

    @Test
    void findByUserId_shouldReturnProfileResponse() {
        when(userRepository.existsById(7L)).thenReturn(true);
        when(profileRepository.findByUserId(7L)).thenReturn(Optional.of(profile));

        ProfileResponseDTO result = profileService.findByUserId(7L);

        assertThat(result.getId()).isNull();
        assertThat(result.getFullName()).isEqualTo("Original Name");
        assertThat(result.getPhone()).isEqualTo("111111111");
        assertThat(result.getCompany()).isFalse();
        verify(profileRepository).findByUserId(7L);
    }

    @Test
    void findByUserId_shouldThrowWhenUserDoesNotExist() {
        when(userRepository.existsById(7L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> profileService.findByUserId(7L));

        verify(profileRepository, never()).findByUserId(7L);
    }

    @Test
    void findByUserId_shouldThrowWhenProfileDoesNotExist() {
        when(userRepository.existsById(7L)).thenReturn(true);
        when(profileRepository.findByUserId(7L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> profileService.findByUserId(7L));
    }

    @Test
    void updateByUserId_shouldUpdateAndPersistOnlyProfileFields() {
        ProfileUpdateRequestDTO request = new ProfileUpdateRequestDTO();
        request.setFullName("Updated Name");
        request.setPhone("222222222");
        request.setCpf("12345678901");
        request.setAddress("Updated address");
        request.setZipCode("01000000");
        request.setCompany(true);

        when(userRepository.existsById(7L)).thenReturn(true);
        when(profileRepository.findByUserId(7L)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProfileResponseDTO result = profileService.updateByUserId(7L, request);

        ArgumentCaptor<Profile> savedProfile = ArgumentCaptor.forClass(Profile.class);
        verify(profileRepository).save(savedProfile.capture());
        assertThat(savedProfile.getValue().getFullName()).isEqualTo("Updated Name");
        assertThat(savedProfile.getValue().getPhone()).isEqualTo("222222222");
        assertThat(savedProfile.getValue().getCpf()).isEqualTo("12345678901");
        assertThat(savedProfile.getValue().getAddress()).isEqualTo("Updated address");
        assertThat(savedProfile.getValue().getZipCode()).isEqualTo("01000000");
        assertThat(savedProfile.getValue().getCompany()).isTrue();
        assertThat(savedProfile.getValue().getUser()).isSameAs(user);
        assertThat(result.getId()).isNull();
        assertThat(result.getFullName()).isEqualTo("Updated Name");
        verify(userRepository, never()).save(any(User.class));
        assertThat(user.getUsername()).isEqualTo("profile-owner");
        assertThat(user.getEmail()).isEqualTo("owner@example.com");
        assertThat(user.getPassword()).isEqualTo("encoded-password");
        assertThat(user.getEnabled()).isTrue();
    }

    @Test
    void updateByUserId_shouldPreserveFieldsNotProvided() {
        ProfileUpdateRequestDTO request = new ProfileUpdateRequestDTO();
        request.setFullName("Updated Name");

        when(userRepository.existsById(7L)).thenReturn(true);
        when(profileRepository.findByUserId(7L)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProfileResponseDTO result = profileService.updateByUserId(7L, request);

        assertThat(result.getFullName()).isEqualTo("Updated Name");
        assertThat(result.getPhone()).isEqualTo("111111111");
        assertThat(result.getCpf()).isEqualTo("11111111111");
        assertThat(result.getCnpj()).isEqualTo("11111111000111");
        assertThat(result.getAddress()).isEqualTo("Original address");
        assertThat(result.getZipCode()).isEqualTo("02000000");
        assertThat(result.getCompany()).isFalse();
    }

    @Test
    void updateByUserId_shouldNotCreateProfileWhenMissing() {
        when(userRepository.existsById(7L)).thenReturn(true);
        when(profileRepository.findByUserId(7L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> profileService.updateByUserId(7L, new ProfileUpdateRequestDTO()));

        verify(profileRepository, never()).save(any(Profile.class));
    }
}
