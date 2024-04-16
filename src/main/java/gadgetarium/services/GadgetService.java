package gadgetarium.services;

import gadgetarium.dto.request.PaginationRequest;
import gadgetarium.dto.response.GadgetResponse;
import gadgetarium.dto.response.ResultPaginationGadget;

public interface GadgetService {
    GadgetResponse getGadgetById(Long gadgetId);

    GadgetResponse getGadgetSelectColour(String colour, String nameOfGadget);

    ResultPaginationGadget getAll(PaginationRequest request);
}
