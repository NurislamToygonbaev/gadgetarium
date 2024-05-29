package gadgetarium.services.impl;

import gadgetarium.configs.jwt.JwtService;
import gadgetarium.dto.request.CategoryNameRequest;
import gadgetarium.dto.request.SignInRequest;
import gadgetarium.dto.request.SignUpRequest;
import gadgetarium.dto.response.*;
import gadgetarium.entities.*;
import gadgetarium.enums.Role;
import gadgetarium.exceptions.AlreadyExistsException;
import gadgetarium.exceptions.AuthenticationException;
import gadgetarium.repositories.SubGadgetRepository;
import gadgetarium.repositories.UserRepository;
import gadgetarium.repositories.jdbcTemplate.impl.GadgetJDBCTemplateRepositoryImpl;
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
        User buildedUser = User.builder().firstName(signUpRequest.getFirstName())
                .lastName(signUpRequest.getLastName()).image(signUpRequest.getImage())
                .phoneNumber(signUpRequest.getPhoneNumber()).email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .address(signUpRequest.getAddress()).role(Role.USER).build();
        userRepo.save(buildedUser);
        return SignResponse.builder().id(buildedUser.getId())
                .role(buildedUser.getRole()).phoneNumber(buildedUser.getPhoneNumber())
                .token(jwtService.createToken(buildedUser)).email(buildedUser.getEmail())
                .response(HttpResponse.builder().status(HttpStatus.OK).message("Sign in was successful!").build()).build();
    }

    @Override
    public SignResponse signIn(SignInRequest signInRequest) {
        User userByEmail = userRepo.getByEmail(signInRequest.email());
        String password = userByEmail.getPassword();
        String decodedPassword = signInRequest.password();
        if (!passwordEncoder.matches(decodedPassword, password)) {
            throw new AuthenticationException("Incorrect email and/or password.");
        }
        return SignResponse.builder().id(userByEmail.getId()).role(userByEmail.getRole())
                .phoneNumber(userByEmail.getPhoneNumber()).token(jwtService.createToken(userByEmail))
                .email(userByEmail.getEmail()).response(HttpResponse.builder().status(HttpStatus.OK).message("Sign in was successful!").build()).build();
    }

    @Override
    @Transactional
    public HttpResponse addCompare(Long subGadgetsId) {
        User user = currentUser.get();
        SubGadget subGadget = subGadgetRepository.getByID(subGadgetsId);
        if (user.getComparison().contains(subGadget)) {
            user.getComparison().remove(subGadget);
            return HttpResponse.builder().status(HttpStatus.OK).message("Gadget removed!").build();
        } else {
            user.addComparison(subGadget);
            log.info("Gadget successfully added in comparison or deleted!");
        }
        return HttpResponse.builder().status(HttpStatus.OK).message("Gadget added successfully!").build();
    }

    @Override
    public List<ListComparisonResponse> seeComparison() {
        List<ListComparisonResponse> listComparisonResponses = new ArrayList<>();
        for (SubGadget subGadget : currentUser.get().getComparison()) {
            listComparisonResponses.add(convert(subGadget));
        }
        return listComparisonResponses;
    }

    public ComparedGadgetsResponse compare(CategoryNameRequest selectCategory, boolean differences) {
        User user = currentUser.get();
        List<SubGadget> comparison = user.getComparison();
        Map<String, Integer> categoryCounts = new HashMap<>();
        String select = "phone";
        String s = selectCategory.categoryName().isEmpty() ? select : selectCategory.categoryName();

        List<SampleResponse> responses = comparison.stream().filter(subGadget -> subGadget.getGadget().getSubCategory().getCategory().getCategoryName().equalsIgnoreCase(s)).map(subGadget -> {
            String categoryName = subGadget.getGadget().getSubCategory().getCategory().getCategoryName();
            categoryCounts.put(categoryName + " quantity", categoryCounts.getOrDefault(categoryName + " quantity", 0) + 1);
            return convertToSubGadget(subGadget, comparison, differences);
        }).collect(Collectors.toList());

        return ComparedGadgetsResponse.builder().categoryCounts(categoryCounts).subGadgetResponses(responses).build();
    }


    private SampleResponse convertToSubGadget(SubGadget subGadget, List<SubGadget> comparison, boolean differences) {
        Map<String, String> uniqueCharacteristics = new HashMap<>();
        CompareFieldResponse uniqueFields = new CompareFieldResponse();

        for (SubGadget other : comparison) {
            if (other.equals(subGadget)) {
                continue;
            }

            if (differences) {
                populateUniqueFields(subGadget, other, uniqueFields);
                populateUniqueCharacteristics(subGadget, other, uniqueCharacteristics);
                break;
            }
        }

        if (differences) {
            return new UniqueFieldResponse(uniqueFields, uniqueCharacteristics);
        } else {
            return new SubGadgetResponse(subGadget.getId(), Collections.singletonList(subGadget.getImages().getFirst()), subGadget.getGadget().getNameOfGadget(), subGadget.getPrice(), subGadget.getMainColour(),
                    subGadget.getGadget().getBrand().getBrandName(), subGadget.getMemory(), subGadget.getGadget().getCharName());
        }
    }

    private void populateUniqueFields(SubGadget subGadget, SubGadget other, CompareFieldResponse uniqueFields) {
        uniqueFields.setId(subGadget.getId());
        uniqueFields.setImages(Collections.singletonList(other.getImages().getFirst()));

        uniqueFields.setNameOfGadget(!subGadget.getGadget().getNameOfGadget().equals(other.getGadget().getNameOfGadget()) ? subGadget.getGadget().getNameOfGadget() : null);
        uniqueFields.setPrice(!subGadget.getPrice().equals(other.getPrice()) ? subGadget.getPrice() : null);
        uniqueFields.setMainColour(!subGadget.getMainColour().equals(other.getMainColour()) ? subGadget.getMainColour() : null);
        uniqueFields.setBrandName(!subGadget.getGadget().getBrand().getBrandName().equals(other.getGadget().getBrand().getBrandName()) ? subGadget.getGadget().getBrand().getBrandName() : null);
        uniqueFields.setMemory(!subGadget.getMemory().equals(other.getMemory()) ? subGadget.getMemory() : null);
    }


    private void populateUniqueCharacteristics(SubGadget subGadget, SubGadget other, Map<String, String> uniqueCharacteristics) {
        Map<CharValue, String> currentCharacteristics = subGadget.getGadget().getCharName();
        Map<CharValue, String> otherCharacteristics = other.getGadget().getCharName();

        for (Map.Entry<CharValue, String> entry : currentCharacteristics.entrySet()) {
            CharValue currentCharValue = entry.getKey();
            String currentCharacteristicName = entry.getValue();
            CharValue otherCharValue = findMatchingCharValue(otherCharacteristics, currentCharacteristicName);

            if (otherCharValue == null) {
                uniqueCharacteristics.put(currentCharacteristicName, currentCharValue.getValues().toString());
                continue;
            }

            Map<String, String> currentValues = currentCharValue.getValues();
            Map<String, String> otherValues = otherCharValue.getValues();

            for (Map.Entry<String, String> charEntry : currentValues.entrySet()) {
                String characteristic = charEntry.getKey();
                String currentValue = charEntry.getValue();
                String otherValue = otherValues.get(characteristic);

                if (!Objects.equals(currentValue, otherValue)) {
                    uniqueCharacteristics.put(currentCharacteristicName + " - " + characteristic, currentValue);
                }
            }
        }
    }

    private CharValue findMatchingCharValue(Map<CharValue, String> characteristics, String characteristicName) {
        for (Map.Entry<CharValue, String> entry : characteristics.entrySet()) {
            if (entry.getValue().equals(characteristicName)) {
                return entry.getKey();
            }
        }
        return null;
    }


    @Override
    @Transactional
    public HttpResponse deleteSubGadget(Long subGadgetId) {
        User user = currentUser.get();
        SubGadget subGadget = subGadgetRepository.getByID(subGadgetId);
        if (user.getComparison().contains(subGadget)) {
            user.getComparison().remove(subGadget);
            log.info("Gadget successfully removed from comparison!");
            return HttpResponse.builder().status(HttpStatus.OK).message("Gadget successfully removed from comparison!").build();
        }

        return HttpResponse.builder().status(HttpStatus.NOT_FOUND).message("There is no such gadget in comparison").build();
    }

    @Override
    @Transactional
    public HttpResponse deleteAllGadgets() {
        User user = currentUser.get();
        user.getComparison().clear();
        log.info("Comparison cleared");
        return HttpResponse.builder().status(HttpStatus.OK).message("Comparison cleared").build();
    }

    @Override
    @Transactional
    public HttpResponse addToFavorites(Long subGadgetId) {
        User user = currentUser.get();
        if (user == null) {
            return HttpResponse.builder().status(HttpStatus.UNAUTHORIZED).message("User not authenticated!").build();
        }

        SubGadget subGadget = subGadgetRepository.getByID(subGadgetId);

        synchronized (user) {
            if (user.getLikes().contains(subGadget)) {
                user.getLikes().remove(subGadget);
                log.info("SubGadget '{}' successfully removed from favorites for User '{}'.", subGadget.getGadget().getNameOfGadget(), user.getUsername());
                return HttpResponse.builder().status(HttpStatus.OK).message("SubGadget removed from favorites!").build();
            }
            user.addLikes(subGadget);
            log.info("SubGadget '{}' successfully added to favorites for User '{}'.", subGadget.getGadget().getNameOfGadget(), user.getUsername());
            return HttpResponse.builder().status(HttpStatus.OK).message("SubGadget added to favorites successfully!").build();

        }
    }

    @Override
    @Transactional
    public HttpResponse addAllGadgetsToFavorites(List<Long> subGadgetId) {
        User user = currentUser.get();
        if (user == null) {
            return HttpResponse.builder().status(HttpStatus.UNAUTHORIZED).message("User not authenticated!").build();
        }
        List<SubGadget> subGadgetsToAdd = new ArrayList<>();

        for (Long subGadgets : subGadgetId) {
            SubGadget subGadget = subGadgetRepository.getByID(subGadgets);
            if (!user.getLikes().contains(subGadget) && !subGadgetsToAdd.contains(subGadget)) {
                subGadgetsToAdd.add(subGadget);
            }
        }

        synchronized (user) {
            user.getLikes().addAll(subGadgetsToAdd);
        }

        return HttpResponse.builder().status(HttpStatus.OK).message("Gadgets successfully added to favorites!").build();

    }

    @Override
    public List<ListComparisonResponse> seeFavorites() {
        List<ListComparisonResponse> listComparisonResponses = new ArrayList<>();
        for (SubGadget like : currentUser.get().getLikes()) {
            listComparisonResponses.add(convert(like));
        }
        return listComparisonResponses;
    }

    @Override
    public List<AllFavoritesResponse> getAllFavorites() {
        List<AllFavoritesResponse> allFavoritesResponses = new ArrayList<>();
        for (SubGadget like : currentUser.get().getLikes()) {
            AllFavoritesResponse response = convertToResponse(like);
            if (response != null) {
                allFavoritesResponses.add(response);
            }
        }
        return allFavoritesResponses;
    }

    @Override
    @Transactional
    public HttpResponse deleteById(Long subGadgetId) {
        User user = currentUser.get();
        SubGadget subGadget = subGadgetRepository.getByID(subGadgetId);
        if (user.getLikes().contains(subGadget)) {
            user.getLikes().remove(subGadget);
            log.info("Gadget successfully removed from favorites!");
            return HttpResponse.builder().status(HttpStatus.OK).message("Gadget successfully removed from favorites!").build();
        }

        return HttpResponse.builder().status(HttpStatus.NOT_FOUND).message("There is no such gadget in favorites").build();
    }

    @Override
    @Transactional
    public HttpResponse clearFavorites() {
        currentUser.get().getLikes().clear();
        return HttpResponse.builder().status(HttpStatus.OK).message("Favorites successfully cleared!").build();

    }

    private AllFavoritesResponse convertToResponse(SubGadget subGadget) {
        if (subGadget == null || subGadget.getGadget() == null || subGadget.getImages() == null ||
            subGadget.getGadget().getSubCategory() == null || subGadget.getGadget().getBrand() == null) {
            return null;
        }

        User user = currentUser.get();
        boolean likes = GadgetJDBCTemplateRepositoryImpl.checkLikes(subGadget, user);
        boolean comparison = GadgetJDBCTemplateRepositoryImpl.checkComparison(subGadget, user);
        boolean basket = GadgetJDBCTemplateRepositoryImpl.checkBasket(subGadget, user);

        Gadget gadget = subGadget.getGadget();
        List<String> images = subGadget.getImages();
        Category category = gadget.getSubCategory().getCategory();
        Brand brand = gadget.getBrand();
        int percent = 0;

        if (gadget.getDiscount() != null){
            percent = gadget.getDiscount().getPercent();
        }

        return new AllFavoritesResponse(
                subGadget.getId(),
                images.isEmpty() ? null : images.getFirst(),
                category.getCategoryName(),
                brand.getBrandName(),
                gadget.getNameOfGadget(),
                subGadget.getMemory(),
                subGadget.getMainColour(),
                gadget.getRating(),
                percent,
                gadget.isNew(),
                GadgetJDBCTemplateRepositoryImpl.isRecommended(gadget),
                subGadget.getPrice(),
                GadgetServiceImpl.calculatePrice(subGadget),
                likes,
                comparison,
                basket
        );
    }

    private ListComparisonResponse convert(SubGadget subGadget) {
        User user = currentUser.get();
        List<String> images = subGadget.getImages();
        boolean basket = GadgetJDBCTemplateRepositoryImpl.checkBasket(subGadget, user);
        return new ListComparisonResponse(subGadget.getId(),
                images.isEmpty() ? null : Collections.singletonList(images.getFirst()),
                subGadget.getGadget().getNameOfGadget(), subGadget.getMainColour(),
                subGadget.getMemory(), subGadget.getPrice(), basket);
    }
}
