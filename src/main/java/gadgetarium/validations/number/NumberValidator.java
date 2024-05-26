package gadgetarium.validations.number;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NumberValidator implements ConstraintValidator<NumberValidation, Integer> {

    @Override
    public boolean isValid(Integer integer, ConstraintValidatorContext constraintValidatorContext) {
        return integer > 0;
    }
}
