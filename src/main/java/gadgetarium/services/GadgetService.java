package gadgetarium.services;

import gadgetarium.dto.response.GadgetResponse;

public interface GadgetService {
    GadgetResponse getGadgetById(Long id);
}
