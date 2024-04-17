package gadgetarium.validation.discount;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DiscountValidator implements ConstraintValidator<DiscountValidation, Integer> {

    @Override
    public boolean isValid(Integer percent , ConstraintValidatorContext constraintValidatorContext) {
        return percent > 0;
    }
}