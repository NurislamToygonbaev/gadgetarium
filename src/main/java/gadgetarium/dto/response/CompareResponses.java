package gadgetarium.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CompareResponses {
    private Long gadgetId;
    private Long subGadgetId;
    private String image;
    private String nameOfGadget;
    private String memory;
    private String color;
    private BigDecimal price;
    private String nameOfGadgetCompare;
    private String colorCompare;
    private String brandCompare;
    private String memoryCompare;
    private String ramCompare;
    private boolean inBasket;
    private int simCompare;
    private String warrantyCompare;
    private int phoneCount;
    private int laptopCount;
    private int watchCount;
}
