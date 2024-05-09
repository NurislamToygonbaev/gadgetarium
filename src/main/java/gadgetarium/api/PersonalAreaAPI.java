package gadgetarium.api;

import gadgetarium.dto.request.ChangePasswordRequest;
import gadgetarium.dto.request.CurrentUserProfileRequest;
import gadgetarium.dto.request.UserImageRequest;
import gadgetarium.dto.response.*;
import gadgetarium.services.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/personal-area")
public class PersonalAreaAPI {

    private final OrderService orderService;

    @GetMapping("/view-all-history")
    public List<AllOrderHistoryResponse> getAllOrdersHistory(){
        return orderService.getAllOrdersHistory();
    }

    @GetMapping("/view-order/{orderId}")
    public OrderHistoryResponse getOrderHistoryById(@PathVariable Long orderId){
        return orderService.getOrderHistoryById(orderId);
    }

    @PostMapping("/edit-profile")
    public CurrentUserProfileResponse editProfile(@RequestBody @Valid CurrentUserProfileRequest currentUserProfileRequest){
        return orderService.editProfile(currentUserProfileRequest);
    }

    @PutMapping("/add-image")
    public UserImageResponse addPhotoAndEdit(@RequestBody @Valid UserImageRequest userImageRequest ){
        return orderService.addPhotoAndEdit(userImageRequest);
    }

    @PostMapping("/change-password")
    public HttpResponse changePassword(@RequestBody @Valid ChangePasswordRequest changePasswordRequest){
        return orderService.changePassword(changePasswordRequest);
    }

}
