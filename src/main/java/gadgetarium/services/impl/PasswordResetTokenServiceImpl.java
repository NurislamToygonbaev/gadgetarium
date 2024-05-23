package gadgetarium.services.impl;

import gadgetarium.config.jwt.JwtService;
import gadgetarium.dto.request.PasswordRequest;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.SignResponse;
import gadgetarium.entities.PasswordResetToken;
import gadgetarium.entities.User;
import gadgetarium.exceptions.BadRequestException;
import gadgetarium.exceptions.IllegalArgumentException;
import gadgetarium.repositories.PasswordResetTokenRepository;
import gadgetarium.repositories.UserRepository;
import gadgetarium.services.PasswordResetTokenService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

    private final UserRepository userRepo;
    private final PasswordResetTokenRepository passwordResetTokenRepo;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional
    public HttpResponse sendResetEmail(String email) {
        User user = userRepo.getByEmail(email);
        String token = UUID.randomUUID().toString();
        createPasswordResetToken(user, token);

        String resetPasswordUrl = "http://localhost:5173/auth/newForgotPassword";
        String resetLink = String.format("%s?token=%s", resetPasswordUrl, token);
        sendEmail(email, resetLink);
        log.info("success send to email");
        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("ссылка на ваш email успешно отправлено, если код не пришло то посмотрите СПАМ")
                .build();
    }

    private void createPasswordResetToken(User user, String token) {
        LocalDateTime expiryDate = LocalDateTime.now().plus(Duration.ofMinutes(30));
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setUser(user);
        passwordResetToken.setToken(token);
        passwordResetToken.setExpiryDate(expiryDate);
        passwordResetTokenRepo.save(passwordResetToken);
        user.addPasswordResetToken(passwordResetToken);
        passwordResetToken.setUser(user);
    }

    private void sendEmail(String email, String resetLink) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(mimeMessage, true);
            String htmlMsg = "<p style=\"font-size: 20px;\">Чтоб изменить пароль, нажмите на эту ссылку:</p> " +
                             "<a href=\"" + resetLink + "\" style=\"font-size: 15px;\">Изменить пароль</a>";
            helper.setText(htmlMsg, true);
            helper.setTo(email);
            helper.setSubject("Забыли пароль!");
            helper.setFrom("GADGETARIUM");
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Override
    @Transactional
    public SignResponse resetPassword(String token, PasswordRequest request) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepo.getByToken(token);
        if (!request.password().equals(request.confirmPassword())) {
            throw new BadRequestException("Пароли не совподают");
        }

        LocalDateTime now = LocalDateTime.now();
        if (passwordResetToken.getExpiryDate().isBefore(now)) {
            passwordResetTokenRepo.delete(passwordResetToken);
            throw new IllegalArgumentException("Время действия токена истекло");
        }

        User user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.password()));
        userRepo.save(user);
        passwordResetTokenRepo.delete(passwordResetToken);
        log.info("success changed password");
        return SignResponse.builder()
                .id(user.getId())
                .role(user.getRole())
                .phoneNumber(user.getPhoneNumber())
                .token(jwtService.createToken(user))
                .email(user.getEmail())
                .response(HttpResponse.builder()
                        .status(HttpStatus.OK)
                        .message("Пароль успешно изменено")
                        .build())
                .build();
    }
}
