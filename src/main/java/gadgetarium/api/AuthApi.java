package gadgetarium.api;

import gadgetarium.dto.request.SignInRequest;
import gadgetarium.dto.request.SignUpRequest;
import gadgetarium.dto.response.SignResponse;
import gadgetarium.services.FirebaseAuthenticationService;
import gadgetarium.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthApi {

    private final UserService userService;
    private final FirebaseAuthenticationService firebaseAuthenticationService;

    @Operation(summary = "Метод  для регистрации", description = "Авторизация: Все")
    @PostMapping("/sign-up")
    public SignResponse signUp(@RequestBody @Valid SignUpRequest signUpRequest) {
        return userService.signUp(signUpRequest);
    }

    @Operation(summary = "Вход пользователя", description = "Авторизация: Все")
    @PostMapping("/sign-in")
    public SignResponse signIn(@RequestBody @Valid SignInRequest signInRequest) {
        return userService.signIn(signInRequest);
    }

    @Operation(summary = "Вход пользователя через Google", description = "авторизация: Все")
    @PostMapping("/google")
    public SignResponse authenticateUser(@RequestBody String idToken) {
        return firebaseAuthenticationService.authenticateUser(idToken);
    }

}


