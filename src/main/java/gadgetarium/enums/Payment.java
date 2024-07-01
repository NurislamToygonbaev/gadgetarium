package gadgetarium.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum Payment {
    PAYMENT_BY_CARD("Оплата картой"),
    UPON_RECEIPT_CARD("При получении картой"),
    UPON_RECEIPT_CASH("При получении наличными");

    private final String russian;

    private static final Map<String, Payment> russianToPaymentMap = new HashMap<>();
    private static final Map<String, String> englishToRussianMap = new HashMap<>();

    static {
        for (Payment payment : values()) {
            russianToPaymentMap.put(payment.russian, payment);
            englishToRussianMap.put(payment.name(), payment.russian);
        }
    }

    Payment(String russian) {
        this.russian = russian;
    }

    public static Payment fromRussian(String russian) {
        return russianToPaymentMap.get(russian);
    }

    public static String toRussian(String english) {
        return englishToRussianMap.get(english);
    }
}
