package gadgetarium.services.impl;

import gadgetarium.configs.jwt.JwtService;
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
import org.apache.commons.mail.EmailException;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
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

    @Transactional
    public void createPasswordResetToken(User user, String token) {
        LocalDateTime expiryDate = LocalDateTime.now().plus(Duration.ofMinutes(30));
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setUser(user);
        passwordResetToken.setToken(token);
        passwordResetToken.setExpiryDate(expiryDate);
        passwordResetTokenRepo.save(passwordResetToken);
        user.addPasswordResetToken(passwordResetToken);
        passwordResetToken.setUser(user);
        userRepo.save(user);
    }

    @Async
    public void sendEmail(String email, String resetLink) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String htmlMsg = String.format("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Reset Password</title>
                <style>
                    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@100..900&display=swap');
                    body {
                        font-family: 'Inter', sans-serif;
                        background-color: #f4f4f4;
                        margin: 0;
                        padding: 0;
                    }
                    .container {
                        width: 100%%;
                        max-width: 600px;
                        margin: 0 auto;
                        background-color: #ffffff;
                        box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1);
                        padding: 20px;
                        border-radius: 8px;
                        overflow: hidden;
                    }
                    .header {
                        text-align: center;
                        padding: 20px 0;
                        background-color: #007bff;
                        color: #ffffff;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                    }
                    .content {
                        padding: 20px;
                        text-align: center;
                    }
                    .content p {
                        font-size: 16px;
                        color: #333333;
                    }
                    .content a {
                        display: inline-block;
                        margin-top: 20px;
                        padding: 10px 20px;
                        font-size: 16px;
                        color: #ffffff;
                        background-color: #007bff;
                        text-decoration: none;
                        border-radius: 5px;
                        transition: background-color 0.3s ease;
                    }
                    .content a:hover {
                        background-color: #0056b3;
                    }
                    .footer {
                        text-align: center;
                        padding: 10px;
                        font-size: 12px;
                        color: #777777;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Сброс пароля</h1>
                    </div>
                    <div class="content">
                        <p>Здравствуйте,</p>
                        <p>Чтобы изменить пароль, нажмите на эту ссылку:</p>
                        <a href="%s">Изменить пароль</a>
                    </div>
                    <div class="footer">
                        <p>Если вы не запрашивали изменение пароля, пожалуйста, проигнорируйте это сообщение.</p>
                    </div>
                </div>
            </body>
            </html>
            """, resetLink);

            helper.setText(htmlMsg, true);
            helper.setTo(email);
            helper.setSubject("Забыли пароль!");
            helper.setFrom("GADGETARIUM <gadgetarium22@gmail.com>");
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email. Please try again later.", e);
        }
    }

    @Override
    @Transactional
    public SignResponse resetPassword(String token, String password, String confirmPassword) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepo.getByToken(token);
        if (!password.equals(confirmPassword)) {
            throw new BadRequestException("Пароли не совподают");
        }

        LocalDateTime now = LocalDateTime.now();
        if (passwordResetToken.getExpiryDate().isBefore(now)) {
            passwordResetTokenRepo.delete(passwordResetToken);
            throw new IllegalArgumentException("Время действия токена истекло");
        }

        User user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(password));
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
