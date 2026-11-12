package coffe.baseproject.controllers.docs;

import coffe.baseproject.data.dto.profile.ProfileResponseDTO;
import coffe.baseproject.data.dto.profile.ProfileUpdateRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.http.ResponseEntity;

public interface ProfileControllerDocs {

    @Operation(
            summary = "Get a user's profile",
            description = "Returns the profile associated with the specified user ID.",
            tags = {"Profile Management"},
            responses = {
                    @ApiResponse(
                            description = "Profile found",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ProfileResponseDTO.class),
                                    examples = @ExampleObject(value = """
                                            {"id":1,"fullName":"Jane Doe","phone":"+5511999999999","cpf":"12345678901","cnpj":null,"address":"Main Street, 10","zipCode":"01000000","company":false}
                                            """))),
                    @ApiResponse(description = "User or profile not found", responseCode = "404", content = @Content)
            }
    )
    ResponseEntity<ProfileResponseDTO> findByUserId(
            @Parameter(description = "ID of the user whose profile is requested", example = "1", required = true)
            Long userId);

    @Operation(
            summary = "Update a user's profile",
            description = "Updates only the supplied profile fields. Null or omitted fields retain their current values.",
            tags = {"Profile Management"},
            responses = {
                    @ApiResponse(
                            description = "Profile updated",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ProfileResponseDTO.class),
                                    examples = @ExampleObject(value = """
                                            {"id":1,"fullName":"Jane Doe","phone":"+5511999999999","cpf":"12345678901","cnpj":null,"address":"Main Street, 10","zipCode":"01000000","company":false}
                                            """))),
                    @ApiResponse(description = "Invalid request data", responseCode = "400", content = @Content),
                    @ApiResponse(description = "User or profile not found", responseCode = "404", content = @Content)
            }
    )
    ResponseEntity<ProfileResponseDTO> updateByUserId(
            @Parameter(description = "ID of the user whose profile is updated", example = "1", required = true)
            Long userId,
            @RequestBody(
                    description = "Profile fields to update; omitted or null fields are preserved",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProfileUpdateRequestDTO.class),
                            examples = @ExampleObject(value = """
                                    {"fullName":"Jane Doe","phone":"+5511999999999","cpf":"12345678901","address":"Main Street, 10","zipCode":"01000000","company":false}
                                    """)))
            ProfileUpdateRequestDTO request);
}
