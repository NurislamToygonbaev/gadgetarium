package gadgetarium.dto.response;

import gadgetarium.enums.Memory;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompareFieldResponse {
    private Long id;
    private String image;
    private String brandName;
    private String nameOfGadget;
    private Memory memory;
    private String mainColour;
    private BigDecimal price;
}

