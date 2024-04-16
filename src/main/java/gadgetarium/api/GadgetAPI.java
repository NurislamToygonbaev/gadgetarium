package gadgetarium.api;

import gadgetarium.dto.request.PaginationRequest;
import gadgetarium.dto.response.GadgetResponse;
import gadgetarium.dto.response.ResultPaginationGadget;
import gadgetarium.services.GadgetService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gadget")
@Slf4j
public class GadgetAPI {

    private final GadgetService gadgetService;

    @Secured("ADMIN")
    @Operation(description = "Получение всех гаджетов!")
    @GetMapping("/get-all")
    public ResultPaginationGadget allGadgets(PaginationRequest request){
        return gadgetService.getAll(request);
    }

    @Secured("ADMIN")
    @Operation(description = "Получение гаджета по ID")
    @GetMapping("/get-gadget/{gadgetId}")
    public GadgetResponse getGadget(@PathVariable Long gadgetId){
        return gadgetService.getGadgetById(gadgetId);
    }

    @Secured("ADMIN")
    @Operation(description = "Полученный гаджет, выбор по цвету")
    @GetMapping("/select-colour")
    public GadgetResponse getGadgetByColour(@RequestParam String colour,
                                            @RequestParam String nameOfGadget){
        return gadgetService.getGadgetSelectColour(colour, nameOfGadget);
    }
}
