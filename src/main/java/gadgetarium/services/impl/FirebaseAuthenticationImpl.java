package gadgetarium.services.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import gadgetarium.config.jwt.JwtService;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.SignResponse;
import gadgetarium.entities.User;
import gadgetarium.enums.Role;
import gadgetarium.exceptions.AuthenticationException;
import gadgetarium.exceptions.IllegalArgumentException;
import gadgetarium.repositories.UserRepository;
import gadgetarium.services.FirebaseAuthenticationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class FirebaseAuthenticationImpl implements FirebaseAuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JavaMailSender javaMailSender;

    @Override
    @Transactional
    public SignResponse authenticateUser(String idToken) {
        try {
            if (idToken == null || idToken.isEmpty()) {
                throw new IllegalArgumentException("Токен аутентификации не может быть пустым.");
            }

            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String email = decodedToken.getEmail();

            if (email == null || email.isEmpty()) {
                throw new IllegalArgumentException("Неверный формат токена аутентификации.");
            }

            boolean userExists = userRepository.existsByEmail(email);

            if (!userExists) {
                User newUser = new User();
                String[] nameParts = decodedToken.getName().split(" ");
                String firstName = (nameParts.length > 0) ? nameParts[0] : "Unknown";
                String lastName = (nameParts.length > 1) ? nameParts[1] : "Unknown";
                newUser.setFirstName(firstName);
                newUser.setLastName(lastName);
                newUser.setEmail(email);
                newUser.setRole(Role.USER);
                String password = UUID.randomUUID().toString();
                newUser.setPassword(passwordEncoder.encode(password));
                userRepository.save(newUser);
                sendEmail(email, password);
                return createSignResponse(newUser);
            } else {
                User foundUser = userRepository.getByEmail(email);
                return createSignResponse(foundUser);
            }
        } catch (FirebaseAuthException e) {
            throw new AuthenticationException("Ошибка аутентификации: " + e.getMessage());
        }
    }

    private SignResponse createSignResponse(User user) {
        return SignResponse.builder()
                .id(user.getId())
                .role(user.getRole())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .token(jwtService.createToken(user))
                .response(HttpResponse.builder()
                        .status(HttpStatus.OK)
                        .message("Успешная аутентификация")
                        .build())
                .build();
    }


    private void sendEmail(String email, String password) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom("GADGETARIUM");
        mailMessage.setTo(email);
        mailMessage.setSubject("Ваш новый временный пароль!");
        mailMessage.setText(password);
        javaMailSender.send(mailMessage);
    }
}
