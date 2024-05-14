package gadgetarium.api;

import gadgetarium.dto.request.ChangePasswordRequest;
import gadgetarium.dto.request.CurrentUserProfileRequest;
import gadgetarium.dto.request.UserImageRequest;
import gadgetarium.dto.response.*;
import gadgetarium.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/personal-area")
public class PersonalAreaAPI {

    private final OrderService orderService;

    @PreAuthorize("hasAnyAuthority({'USER'})")
    @Operation(summary = "Посмотреть весь истории заказов.", description = "авторизация: ПОЛЬЗОВАТЕЛЬ")
    @GetMapping("/view-all-history")
    public List<AllOrderHistoryResponse> getAllOrdersHistory(){
        return orderService.getAllOrdersHistory();
    }

    @PreAuthorize("hasAnyAuthority({'USER'})")
    @Operation(summary = "Посмотреть один историю заказа.", description = "авторизация: ПОЛЬЗОВАТЕЛЬ")
    @GetMapping("/view-order/{orderId}")
    public OrderHistoryResponse getOrderHistoryById(@PathVariable Long orderId){
        return orderService.getOrderHistoryById(orderId);
    }

    @PreAuthorize("hasAnyAuthority({'USER'})")
    @Operation(summary = "Изменить профиль.", description = "авторизация: ПОЛЬЗОВАТЕЛЬ")
    @PostMapping("/edit-profile")
    public CurrentUserProfileResponse editProfile(@RequestBody @Valid CurrentUserProfileRequest currentUserProfileRequest){
        return orderService.editProfile(currentUserProfileRequest);
    }

    @PreAuthorize("hasAnyAuthority({'USER'})")
    @Operation(summary = "Изменить фото профиля.", description = "авторизация: ПОЛЬЗОВАТЕЛЬ")
    @PutMapping("/add-image")
    public UserImageResponse addPhotoAndEdit(@RequestBody @Valid UserImageRequest userImageRequest ){
        return orderService.addPhotoAndEdit(userImageRequest);
    }

    @PreAuthorize("hasAnyAuthority({'USER'})")
    @Operation(summary = "Сменить пароль.", description = "авторизация: ПОЛЬЗОВАТЕЛЬ")
    @PostMapping("/change-password")
    public HttpResponse changePassword(@RequestBody @Valid ChangePasswordRequest changePasswordRequest){
        return orderService.changePassword(changePasswordRequest);
    }

}
