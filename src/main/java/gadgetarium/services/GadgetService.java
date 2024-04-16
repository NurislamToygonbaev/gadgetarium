package gadgetarium.services;

import gadgetarium.dto.response.GadgetResponse;

public interface GadgetService {
    GadgetResponse getGadgetById(Long gadgetId);

    GadgetResponse getGadgetSelectColour(String colour, String nameOfGadget);
}
