package gadgetarium.dto.response;

import lombok.*;

@Builder
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class ColorResponseWithCount {

    private String colorName;
    private String colorQuantity;
}
