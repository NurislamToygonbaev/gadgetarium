package gadgetarium.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrivateGadgetResponse{
    private Long id;
    private String gadgetImage;
    private String nameOfGadget;
    private String subCategoryName;
    private double rating;
    private int countRating;
    private BigDecimal currentPrice;

}
