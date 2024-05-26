package gadgetarium.validations.rating;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RatingValidator implements ConstraintValidator<RatingValidation, Integer> {

    @Override
    public boolean isValid(Integer rating, ConstraintValidatorContext constraintValidatorContext) {
        return rating > 0 && rating < 6;
    }
}
