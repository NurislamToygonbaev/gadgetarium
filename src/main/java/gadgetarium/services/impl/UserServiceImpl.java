package gadgetarium.services.impl;

import gadgetarium.config.jwt.JwtService;
import gadgetarium.dto.request.PasswordRequest;
import gadgetarium.dto.request.SignInRequest;
import gadgetarium.dto.request.SignUpRequest;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.SignResponse;
import gadgetarium.entities.User;
import gadgetarium.enums.Role;
import gadgetarium.exceptions.AlreadyExistsException;
import gadgetarium.exceptions.AuthenticationException;
import gadgetarium.exceptions.BadRequestException;
import gadgetarium.repositories.UserRepository;
import gadgetarium.services.UserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JavaMailSender javaMailSender;

    private int code;
    private String userName;

    private void checkEmail(String email){
        boolean existsByEmail = userRepo.existsByEmail(email);
        if (existsByEmail) throw new AlreadyExistsException("User with email " + email + " already exists.");
    }

    @Override
    public SignResponse signUp(SignUpRequest signUpRequest) {
        checkEmail(signUpRequest.getEmail());
        User buildedUser = User.builder()
                .firstName(signUpRequest.getFirstName())
                .lastName(signUpRequest.getLastName())
                .image(signUpRequest.getImage())
                .phoneNumber(signUpRequest.getPhoneNumber())
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .address(signUpRequest.getAddress())
                .role(Role.USER)
                .build();
        userRepo.save(buildedUser);
        return SignResponse.builder()
                .id(buildedUser.getId())
                .role(buildedUser.getRole())
                .phoneNumber(buildedUser.getPhoneNumber())
                .token(jwtService.createToken(buildedUser))
                .email(buildedUser.getEmail())
                .response(HttpResponse.builder()
                        .status(HttpStatus.OK)
                        .message("Sign in was successful!")
                        .build())
                .build();
    }

    @Override
    public SignResponse signIn(SignInRequest signInRequest) {
        User userByEmail = userRepo.getByEmail(signInRequest.email());
        String password = userByEmail.getPassword();
        String decodedPassword = signInRequest.password();
        if(!passwordEncoder.matches(decodedPassword, password)){
            throw new AuthenticationException("Incorrect email and/or password.");
        }
        return SignResponse.builder()
                .id(userByEmail.getId())
                .role(userByEmail.getRole())
                .phoneNumber(userByEmail.getPhoneNumber())
                .token(jwtService.createToken(userByEmail))
                .email(userByEmail.getEmail())
                .response(HttpResponse.builder()
                        .status(HttpStatus.OK)
                        .message("Sign in was successful!")
                        .build())
                .build();
    }

    @Override
    public HttpResponse oneTimePassword(String email) throws MessagingException {
        User user = userRepo.getByEmail(email);
        userName = user.getEmail();
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        mimeMessageHelper.setFrom("ntoygonbaev098@gmail.com");
        mimeMessageHelper.setTo(email);
        Random random = new Random();
        code = random.nextInt(9000) + 1000;
        String text = "<p style=font-size:20px; color: black>Ваш одноразовый код: <span style=font-size:30px;>" + code + "</span></p>.<br>"
                + "<p style=font-size:20px; color: black>Никому не передайте этот код.</p>";
        mimeMessageHelper.setText(text, true);
        mimeMessageHelper.setSubject("FORGOT PASSWORD");
        javaMailSender.send(mimeMessage);
        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("Если код не пришел. Смотрите СПАМ")
                .build();
    }

    @Override
    public HttpResponse checkingCode(int codeRequest) {
        if (code != codeRequest){
            throw new BadRequestException("Invalid code");
        }
        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("correct")
                .build();
    }

    @Override @Transactional
    public SignResponse changePassword(PasswordRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new BadRequestException("Invalid password");
        }
        User user = userRepo.getByEmail(userName);
        user.setPassword(passwordEncoder.encode(request.password()));
        return SignResponse.builder()
                .id(user.getId())
                .role(user.getRole())
                .phoneNumber(user.getPhoneNumber())
                .token(jwtService.createToken(user))
                .email(user.getEmail())
                .response(HttpResponse.builder()
                        .status(HttpStatus.OK)
                        .message("Successfully changed")
                        .build())
                .build();
    }

}
