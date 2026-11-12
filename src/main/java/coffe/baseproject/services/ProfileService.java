package coffe.baseproject.services;

import coffe.baseproject.data.dto.profile.ProfileResponseDTO;
import coffe.baseproject.data.dto.profile.ProfileUpdateRequestDTO;
import coffe.baseproject.exception.ResourceNotFoundException;
import coffe.baseproject.model.Profile;
import coffe.baseproject.repository.ProfileRepository;
import coffe.baseproject.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    public ProfileService(UserRepository userRepository, ProfileRepository profileRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public ProfileResponseDTO findByUserId(Long userId) {
        ensureUserExists(userId);
        return new ProfileResponseDTO(findProfileByUserId(userId));
    }

    @Transactional
    public ProfileResponseDTO updateByUserId(Long userId, ProfileUpdateRequestDTO request) {
        ensureUserExists(userId);
        Profile profile = findProfileByUserId(userId);

        updateIfPresent(request.getFullName(), profile::setFullName);
        updateIfPresent(request.getPhone(), profile::setPhone);
        updateIfPresent(request.getCpf(), profile::setCpf);
        updateIfPresent(request.getCnpj(), profile::setCnpj);
        updateIfPresent(request.getAddress(), profile::setAddress);
        updateIfPresent(request.getZipCode(), profile::setZipCode);
        if (request.getCompany() != null) {
            profile.setCompany(request.getCompany());
        }

        return new ProfileResponseDTO(profileRepository.save(profile));
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User with id " + userId + " not found");
        }
    }

    private Profile findProfileByUserId(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profile for user with id " + userId + " not found"));
    }

    private void updateIfPresent(String value, java.util.function.Consumer<String> updater) {
        if (value != null) {
            updater.accept(value);
        }
    }
}
