package gadgetarium.api;

import gadgetarium.services.FirebaseAuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/google")
@CrossOrigin(origins = "*", maxAge = 100000L)
public class AuthGoogleApi {
    private final FirebaseAuthenticationService firebaseAuthenticationService;

    @Operation(summary = "Вход пользователя через Google", description = "авторизация: Все")
    @PostMapping
    public ResponseEntity<?> authenticateUser(@RequestBody String idToken) {
        return firebaseAuthenticationService.authenticateUser(idToken);
    }
}
