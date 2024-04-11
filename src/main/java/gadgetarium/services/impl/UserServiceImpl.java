package gadgetarium.services.impl;

import gadgetarium.config.jwt.JwtService;
import gadgetarium.dto.request.SignInRequest;
import gadgetarium.dto.request.SignUpRequest;
import gadgetarium.dto.response.SignResponse;
import gadgetarium.entities.User;
import gadgetarium.enums.Role;
import gadgetarium.exceptions.AlreadyExistsException;
import gadgetarium.exceptions.AuthenticationException;
import gadgetarium.repositories.UserRepository;
import gadgetarium.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public SignResponse signUp(SignUpRequest signUpRequest) {
        boolean existsByEmail = userRepo.existsByEmail(signUpRequest.getEmail());
        if (existsByEmail) throw new AlreadyExistsException("User with email " + signUpRequest.getEmail() + " already exists.");
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
        String token = jwtService.createToken(buildedUser);
        return SignResponse.builder()
                .httpStatus(HttpStatus.OK)
                .token(token)
                .message("Sign up was successful!")
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
        String token  = jwtService.createToken(userByEmail);
        return SignResponse.builder()
                .httpStatus(HttpStatus.OK)
                .token(token)
                .message("Sign in was successful!")
                .build();
    }
}
