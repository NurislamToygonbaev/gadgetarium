package gadgetarium.api;

import gadgetarium.dto.request.ChangePasswordRequest;
import gadgetarium.dto.request.CurrentUserProfileRequest;
import gadgetarium.dto.request.UserImageRequest;
import gadgetarium.dto.response.*;
import gadgetarium.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/personal")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PersonalAreaApi {

    private final OrderService orderService;

    @PreAuthorize("hasAnyAuthority({'USER'})")
    @Operation(summary = "Посмотреть весь истории заказов.", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @GetMapping
    public List<AllOrderHistoryResponse> getAllOrdersHistory(){
        return orderService.getAllOrdersHistory();
    }

    @PreAuthorize("hasAnyAuthority({'USER'})")
    @Operation(summary = "Посмотреть один историю заказа.", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @GetMapping("/by-id/{orderId}")
    public OrderHistoryResponse getOrderHistoryById(@PathVariable Long orderId){
        return orderService.getOrderHistoryById(orderId);
    }

    @PreAuthorize("hasAnyAuthority({'USER'})")
    @Operation(summary = "Изменить профиль.", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @PostMapping("/edit-profile")
    public CurrentUserProfileResponse editProfile(@RequestBody @Valid CurrentUserProfileRequest currentUserProfileRequest){
        return orderService.editProfile(currentUserProfileRequest);
    }

    @PreAuthorize("hasAnyAuthority({'USER'})")
    @Operation(summary = "Изменить фото профиля.", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @PatchMapping("/add-image")
    public UserImageResponse addPhotoAndEdit(@RequestBody @Valid UserImageRequest userImageRequest ){
        return orderService.addPhotoAndEdit(userImageRequest);
    }

    @PreAuthorize("hasAnyAuthority({'USER'})")
    @Operation(summary = "Сменить пароль.", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @PatchMapping("/change-password")
    public HttpResponse changePassword(@RequestBody @Valid ChangePasswordRequest changePasswordRequest){
        return orderService.changePassword(changePasswordRequest);
    }

}
