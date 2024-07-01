package gadgetarium.dto.response;

import gadgetarium.enums.Payment;
import gadgetarium.enums.Status;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record OrderHistoryResponse(
        Long number,
        List<PrivateGadgetResponse> privateGadgetResponse,
        String status,
        String clientFullName,
        String userName,
        String address,
        String phoneNumber,
        String email,
        BigDecimal discount,
        BigDecimal currentPrice,
        String createdAt,
        String payment,
        String lastName


){
}
