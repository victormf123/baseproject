package coffe.baseproject.controllers;

import coffe.baseproject.controllers.docs.ProfileControllerDocs;
import coffe.baseproject.data.dto.profile.ProfileResponseDTO;
import coffe.baseproject.data.dto.profile.ProfileUpdateRequestDTO;
import coffe.baseproject.services.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profiles")
public class ProfileController implements ProfileControllerDocs {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/user/{userId}")
    @Override
    public ResponseEntity<ProfileResponseDTO> findByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.findByUserId(userId));
    }

    @PutMapping("/user/{userId}")
    @Override
    public ResponseEntity<ProfileResponseDTO> updateByUserId(
            @PathVariable Long userId,
            @Valid @RequestBody ProfileUpdateRequestDTO request) {
        return ResponseEntity.ok(profileService.updateByUserId(userId, request));
    }
}
