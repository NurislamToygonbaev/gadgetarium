package gadgetarium.services.impl;

import gadgetarium.config.jwt.JwtService;
import gadgetarium.dto.request.SignInRequest;
import gadgetarium.dto.request.SignUpRequest;
import gadgetarium.dto.response.*;
import gadgetarium.entities.SubGadget;
import gadgetarium.entities.User;
import gadgetarium.enums.Role;
import gadgetarium.exceptions.AlreadyExistsException;
import gadgetarium.exceptions.AuthenticationException;
import gadgetarium.repositories.SubGadgetRepository;
import gadgetarium.repositories.UserRepository;
import gadgetarium.services.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SubGadgetRepository subGadgetRepository;
    private final CurrentUser currentUser;

    private void checkEmail(String email) {
        boolean existsByEmail = userRepo.existsByEmail(email);
        if (existsByEmail) throw new AlreadyExistsException("User with email " + email + " already exists.");
    }

    @Override
    public SignResponse signUp(SignUpRequest signUpRequest) {
        boolean existsByEmail = userRepo.existsByEmail(signUpRequest.getEmail());
        if (existsByEmail)
            throw new AlreadyExistsException("User with email " + signUpRequest.getEmail() + " already exists.");
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
        if (!passwordEncoder.matches(decodedPassword, password)) {
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
    @Transactional
    public HttpResponse addCompare(Long subGadgetsId) {
        User user = currentUser.get();
        SubGadget subGadget = subGadgetRepository.getByID(subGadgetsId);
        if (user.getComparison().contains(subGadget)) {
            user.getComparison().remove(subGadget);
            return HttpResponse.builder()
                    .status(HttpStatus.OK)
                    .message("Gadget removed!")
                    .build();
        } else {
            user.addComparison(subGadget);
            log.info("Gadget successfully added in comparison or deleted!");
        }
        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("Gadget added successfully!")
                .build();
    }

    @Override
    public List<ListComparisonResponse> seeComparison() {
        List<ListComparisonResponse> listComparisonResponses = new ArrayList<>();
        for (SubGadget subGadget : currentUser.get().getComparison()) {
            listComparisonResponses.add(convert(subGadget));
        }
        return listComparisonResponses;
    }

    public ComparedGadgetsResponse compare(String selectCategory, boolean differences) {
        User user = currentUser.get();
        List<SubGadget> comparison = user.getComparison();
        Map<String, Integer> categoryCounts = new HashMap<>();

        List<SubGadgetResponse> responses = comparison.stream()
                .filter(subGadget -> subGadget.getGadget().getSubCategory().getCategory().getCategoryName().equalsIgnoreCase(selectCategory))
                .map(subGadget -> {
                    String categoryName = subGadget.getGadget().getSubCategory().getCategory().getCategoryName();
                    categoryCounts.put(categoryName, categoryCounts.getOrDefault(categoryName, 0) + 1);
                    return convertToSubGadget(subGadget, comparison);
                })
                .collect(Collectors.toList());

        return ComparedGadgetsResponse.builder()
                .categoryCounts(categoryCounts)
                .subGadgetResponses(responses)
                .build();
    }

    private List<String> getDifferences(SubGadget subGadget, SubGadget previousGadget) {
        List<String> differencesList = new ArrayList<>();

        if (previousGadget != null) {
            if (!Objects.equals(previousGadget.getPrice(), subGadget.getPrice())) {
                differencesList.add("price");
            }
            if (!Objects.equals(previousGadget.getCharacteristics(), subGadget.getCharacteristics())) {
                differencesList.add("characteristics");
            }
            if (!Objects.equals(previousGadget.getNameOfGadget(), subGadget.getNameOfGadget())) {
                differencesList.add("nameOfGadget");
            }
            if (!Objects.equals(previousGadget.getGadget().getBrand().getBrandName(), subGadget.getGadget().getBrand().getBrandName())) {
                differencesList.add("brandName");
            }
            if (!Objects.equals(previousGadget.getMainColour(), subGadget.getMainColour())) {
                differencesList.add("mainColour");
            }
            if (!Objects.equals(previousGadget.getGadget().getMemory(), subGadget.getGadget().getMemory())) {
                differencesList.add("memory");
            }
        }

        return differencesList;
    }

    private SubGadgetResponse convertToSubGadget(SubGadget subGadget, List<SubGadget> comparison) {
        SubGadget previousGadget = getPreviousGadget(subGadget, comparison);

        List<String> differencesList = getDifferences(subGadget, previousGadget);

        return new SubGadgetResponse(
                subGadget.getId(),
                subGadget.getNameOfGadget(),
                subGadget.getPrice(),
                subGadget.getMainColour(),
                subGadget.getGadget().getBrand().getBrandName(),
                subGadget.getGadget().getMemory(),
                subGadget.getCharacteristics(),
                differencesList
        );
    }

    private SubGadget getPreviousGadget(SubGadget subGadget, List<SubGadget> comparison) {
        int currentIndex = comparison.indexOf(subGadget);

        if (currentIndex > 0) {
            return comparison.get(currentIndex - 1);
        } else {
            return null;
        }
    }

    @Override
    @Transactional
    public HttpResponse deleteSubGadget(Long subGadgetId) {
        User user = currentUser.get();
        SubGadget subGadget = subGadgetRepository.getByID(subGadgetId);
        if (user.getComparison().contains(subGadget)) {
            user.getComparison().remove(subGadget);
            log.info("Gadget successfully removed from comparison!");
            return HttpResponse.builder()
                    .status(HttpStatus.OK)
                    .message("Gadget successfully removed from comparison!")
                    .build();
        }

        return HttpResponse.builder()
                .status(HttpStatus.NOT_FOUND)
                .message("There is no such gadget in comparison")
                .build();
    }

    @Override
    @Transactional
    public HttpResponse deleteAllGadgets() {
        User user = currentUser.get();
        user.getComparison().clear();
        log.info("Comparison cleared");
        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("Comparison cleared")
                .build();
    }

    private ListComparisonResponse convert(SubGadget subGadget) {
        return new ListComparisonResponse(
                subGadget.getId(),
                subGadget.getImages(),
                subGadget.getNameOfGadget(),
                subGadget.getMainColour(),
                subGadget.getGadget().getMemory(),
                subGadget.getPrice()
        );
    }
}
