package gadgetarium.api;

import gadgetarium.dto.request.PasswordRequest;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.SignResponse;
import gadgetarium.services.PasswordResetTokenService;
import gadgetarium.validation.password.PasswordValidation;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reset")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ResetPasswordApi {

    private final PasswordResetTokenService passwordResetService;

    @Operation(summary = "Метод для того чтоб отправить ссылка на email", description = "Авторизация: ВСЕ")
    @PostMapping
    public HttpResponse forgotPassword(@RequestParam("email") String email) {
        return passwordResetService.sendResetEmail(email);
    }

    @Operation(summary = "Метод  для изменение пароля", description = "Авторизация: ВСЕ")
    @PatchMapping
    public SignResponse resetPassword(@RequestParam("token") String token,
                                      @RequestParam @PasswordValidation String password,
                                      @RequestParam @PasswordValidation String confirmPassword) {
        return passwordResetService.resetPassword(token, password, confirmPassword);
    }
}
