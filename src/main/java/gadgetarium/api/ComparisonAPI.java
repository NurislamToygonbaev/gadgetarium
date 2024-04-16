package gadgetarium.api;

import gadgetarium.dto.request.SelectCategoryRequest;
import gadgetarium.dto.response.ComparedGadgetsResponse;
import gadgetarium.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comparison")
public class ComparisonAPI {

    private final UserService userService;

    @GetMapping("/compare")
    public ComparedGadgetsResponse compare(@RequestParam String diffClear,
                                           @RequestBody SelectCategoryRequest selectCategory){
        return userService.compare(diffClear, selectCategory);
    }
}
