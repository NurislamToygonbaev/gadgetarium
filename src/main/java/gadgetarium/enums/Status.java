package gadgetarium.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum Status {
    PENDING("Ожидание"),
    READY("Готово"),
    COURIER_ON_THE_WAY("Курьер в пути"),
    DELIVERED("Доставлено"),
    RECEIVED("Получено"),
    CANCELLED("Отменено");

    private final String russian;

    private static final Map<String, Status> russianToStatusMap = new HashMap<>();
    private static final Map<String, String> englishToRussianMap = new HashMap<>();

    static {
        for (Status status : values()) {
            russianToStatusMap.put(status.russian, status);
            englishToRussianMap.put(status.name(), status.russian);
        }
    }

    Status(String russian) {
        this.russian = russian;
    }

    public static Status fromRussian(String russian) {
        return russianToStatusMap.get(russian);
    }

    public static String toRussian(String english) {
        return englishToRussianMap.get(english);
    }
}
