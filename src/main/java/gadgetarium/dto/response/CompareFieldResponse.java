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
    private List<String> images;
    private String nameOfGadget;
    private BigDecimal price;
    private String mainColour;
    private String brandName;
    private Memory memory;
    private Map<String, String> characteristics;
}

