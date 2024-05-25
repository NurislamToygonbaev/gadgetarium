package gadgetarium.dto.request;

import gadgetarium.enums.Memory;
import gadgetarium.enums.Ram;
import gadgetarium.validation.countSim.CountSimValidation;
import gadgetarium.validation.feedack.ImageValidation;
import gadgetarium.validation.issueDate.IssueDateValidation;
import gadgetarium.validation.warranty.WarrantyValidation;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record GadgetNewDataRequest(
        String colour,
        @CountSimValidation
        int countSim,
        Memory memory,
        Ram ram,
        @ImageValidation
        List<String> images,
        String materialBracelet,
        String materialBody,
        String sizeWatch,
        String dumas,
        String genderWatch,
        String waterproof,
        String wireless,
        String shapeBody

) {
}
