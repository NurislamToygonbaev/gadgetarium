package gadgetarium.services;

import gadgetarium.dto.request.PaginationRequest;
import gadgetarium.dto.response.GadgetResponse;
import gadgetarium.dto.response.ResultPaginationGadget;

public interface GadgetService {
    GadgetResponse getGadgetById(Long id);

    ResultPaginationGadget getAll(PaginationRequest request);
}
