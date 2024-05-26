package gadgetarium.dto.request;

import gadgetarium.validations.card.cvc.CvcValidation;
import gadgetarium.validations.card.expiration.ExpirationDateValidation;
import gadgetarium.validations.card.number.CardNumberValidation;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class CarDetails {
    @CardNumberValidation
    private String cardNumber;
    private String expirationMonth;
    @ExpirationDateValidation
    private String expirationYear;
    @CvcValidation
    private String cvc;
    private String fullName;


    public Map<String, Object> toMap() {
        Map<String, Object> cardParams = new HashMap<>();
        cardParams.put("number", this.cardNumber);
        cardParams.put("exp_month", this.expirationMonth);
        cardParams.put("exp_year", this.expirationYear);
        cardParams.put("cvc", this.cvc);
        cardParams.put("name", this.fullName);
        return cardParams;
    }
}
