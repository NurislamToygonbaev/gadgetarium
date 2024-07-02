package gadgetarium.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderHistoryResponse {
    private Long number;
    private List<PrivateGadgetResponse> privateGadgetResponse = new ArrayList<>();
    private String status;
    private String clientFullName;
    private String userName;
    private String address;
    private String phoneNumber;
    private String email;
    private BigDecimal discount;
    private BigDecimal currentPrice;
    private String createdAt;
    private String payment;
    private String lastName;
}
