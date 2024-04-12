package gadgetarium.api;

import gadgetarium.dto.request.PasswordRequest;
import gadgetarium.dto.request.SignInRequest;
import gadgetarium.dto.request.SignUpRequest;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.SignResponse;
import gadgetarium.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.mail.MessagingException;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Slf4j
public class AuthApi {

    private final UserService userService;

    @Operation(description = "Метод  для регистрации")
    @PostMapping("/sign-up")
    public SignResponse signUp(@RequestBody @Valid SignUpRequest signUpRequest){
        return userService.signUp(signUpRequest);
    }

    @Operation(description = "Вход пользователя")
    @PutMapping("sign-in")
    public SignResponse signIn(@RequestBody @Valid SignInRequest signInRequest){
        return userService.signIn(signInRequest);
    }

    @Operation(description = "Метод  чтоб ввести email")
    @PostMapping("/email")
    public HttpResponse oneTimePassword(@RequestParam String email) throws MessagingException {
        return userService.oneTimePassword(email);
    }

    @Operation(description = "Метод чтоб ввести code")
    @PostMapping("/code")
    public HttpResponse checkingCOde(@RequestParam int code){
        return userService.checkingCode(code);
    }

    @Operation(description = "Метод  для изменение пароля")
    @PutMapping("/change")
    public SignResponse changePassword(@RequestBody @Valid PasswordRequest request) {
        return userService.changePassword(request);
    }


}
