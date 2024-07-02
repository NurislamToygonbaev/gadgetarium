package gadgetarium.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class AllOrderHistoryResponse {
    private Long id;
    private String createdAt;
    private Long number;
    private String status;
    private BigDecimal deliveryPrice;
}
