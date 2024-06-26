package gadgetarium.validations.feedack;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class ImageValidator implements ConstraintValidator<ImageValidation, List<String>> {
    @Override
    public boolean isValid(List<String> images, ConstraintValidatorContext constraintValidatorContext) {
        return images.size() <= 6;
    }
}
