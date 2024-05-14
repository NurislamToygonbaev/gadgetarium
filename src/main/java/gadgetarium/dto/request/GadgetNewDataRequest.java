package gadgetarium.dto.request;

import gadgetarium.validation.countSim.CountSimValidation;
import gadgetarium.validation.feedack.ImageValidation;
import gadgetarium.validation.issueDate.IssueDateValidation;
import gadgetarium.validation.warranty.WarrantyValidation;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record GadgetNewDataRequest(
        @WarrantyValidation
        int warranty,
        String nameOfGadget,
        @IssueDateValidation
        LocalDate issueDate,
        String colour,
        @CountSimValidation
        int countSim,
        @ImageValidation
        List<String> images
) {
}
