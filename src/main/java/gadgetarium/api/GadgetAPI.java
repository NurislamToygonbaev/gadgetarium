package gadgetarium.api;

import gadgetarium.dto.request.PaginationRequest;
import gadgetarium.dto.response.GadgetResponse;
import gadgetarium.dto.response.ResultPaginationGadget;
import gadgetarium.services.GadgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gadget")
@Slf4j
public class GadgetAPI {
    private GadgetService gadgetService;

    @Secured("ADMIN")
    @GetMapping("/get-all")
    public ResultPaginationGadget allGadgets(PaginationRequest request){
        return gadgetService.getAll(request);
    }

    @Secured("ADMIN")
    @GetMapping("/getGadget/{id}")
    public GadgetResponse getGadget(@PathVariable Long id){
        return gadgetService.getGadgetById(id);
    }
}
