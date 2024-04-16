package gadgetarium.validation.banner;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

public class BannerValidator implements ConstraintValidator<BannerValidation, List<String>> {

    @Override
    public boolean isValid(List<String> banners, ConstraintValidatorContext constraintValidatorContext) {
        return banners.size() <= 6 && !banners.isEmpty();
    }
}
