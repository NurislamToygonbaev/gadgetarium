package gadgetarium.dto.request;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class CarDetails {
    private String cardNumber;
    private String expirationMonth;
    private String expirationYear;
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
