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
@RequestMapping("/api/reset")
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
                                      @RequestBody @Valid PasswordRequest request) {
        return passwordResetService.resetPassword(token, request);
    }
}
