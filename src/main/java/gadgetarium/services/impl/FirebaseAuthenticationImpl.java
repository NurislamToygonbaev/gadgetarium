package gadgetarium.services.impl;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import gadgetarium.configs.jwt.JwtService;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.SignResponse;
import gadgetarium.entities.User;
import gadgetarium.enums.Role;
import gadgetarium.exceptions.AuthenticationException;
import gadgetarium.exceptions.BadRequestException;
import gadgetarium.exceptions.IllegalArgumentException;
import gadgetarium.repositories.UserRepository;
import gadgetarium.services.FirebaseAuthenticationService;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class FirebaseAuthenticationImpl implements FirebaseAuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JavaMailSender javaMailSender;

    @PostConstruct
    void init() throws IOException {
        try {
            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(new ClassPathResource("serviceAccountKey.json").getInputStream());
            FirebaseOptions firebaseOptions = FirebaseOptions.builder().setCredentials(googleCredentials).build();
            FirebaseApp.initializeApp(firebaseOptions);
            log.info("FirebaseApp успешно инициализирован");
        } catch (IOException e) {
            log.error("Ошибка при инициализации FirebaseApp: {}", e.getMessage(), e);
            throw e;
        }
    }

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
                User newUser = createUserFromDecodedToken(decodedToken);
                return createSignResponse(newUser);
            } else {
                User foundUser = userRepository.getByEmail(email);
                return createSignResponse(foundUser);
            }
        } catch (FirebaseAuthException e) {
            throw new AuthenticationException("Ошибка аутентификации: " + e.getMessage());
        }
    }

    private User createUserFromDecodedToken(FirebaseToken decodedToken) {
        User newUser = new User();
        String[] nameParts = decodedToken.getName().split(" ");
        String firstName = (nameParts.length > 0) ? nameParts[0] : "Unknown";
        String lastName = (nameParts.length > 1) ? nameParts[1] : "Unknown";
        String pictureUrl = decodedToken.getPicture();
        if (pictureUrl != null && !pictureUrl.isEmpty()) {
            newUser.setImage(pictureUrl);
        }
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setEmail(decodedToken.getEmail());
        newUser.setRole(Role.USER);
        String password = generateRandomPassword();
        sendEmail(newUser.getEmail(), password);
        newUser.setPassword(passwordEncoder.encode(password));
        userRepository.save(newUser);
        return newUser;
    }

    private String generateRandomPassword() {
        return UUID.randomUUID().toString();
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


    @Async
    public void sendEmail(String email, String password) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            String htmlMsg = "<p style=\"font-size: 16px;\">Ваш новый временный пароль:</p> " +
                             "<p style=\"font-size: 16px; font-weight: bold;\">" + password + "</p>";
            helper.setText(htmlMsg, true);
            helper.setTo(email);
            helper.setSubject("Ваш новый временный пароль!");
            helper.setFrom("GADGETARIUM <gadgetarium22@gmail.com>");
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new BadRequestException("Не удалось отправить письмо.");
        }
    }
}

