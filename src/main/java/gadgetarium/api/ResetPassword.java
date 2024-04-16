package gadgetarium.api;

import gadgetarium.dto.request.PasswordRequest;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.SignResponse;
import gadgetarium.services.PasswordResetTokenService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reset-password")
public class ResetPassword {

    private final PasswordResetTokenService passwordResetService;

    @Operation(description = "Метод для того чтоб отправить ссылка на email")
    @PostMapping("/forgot")
    public HttpResponse forgotPassword(@RequestParam("email") String email) {
        return passwordResetService.sendResetEmail(email);
    }

    @Operation(description = "Метод  для изменение пароля")
    @PostMapping("/reset")
    public SignResponse resetPassword(@RequestParam("token") String token,
                                      @RequestBody @Valid PasswordRequest request) {
        return passwordResetService.resetPassword(token, request);
    }
}
