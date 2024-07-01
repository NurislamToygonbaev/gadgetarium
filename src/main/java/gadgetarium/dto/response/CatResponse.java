package gadgetarium.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Builder
public class CatResponse {
    private Long id;
    private String categoryName;

    public String getCategoryName() {
        return switch (this.categoryName) {
            case "phone" -> "Телефоны";
            case "laptop" -> "Ноутбуки и планшеты";
            case "watch" -> "Смарт-часы и браслеты";
            case "Accessories" -> "Аксессуары";
            default -> this.categoryName;
        };
    }
}

