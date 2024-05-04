package gadgetarium.validation.string;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StringValidator implements ConstraintValidator<StringValidation, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) {
            return false;
        }

        return !value.trim().isEmpty() && value.length() < 500;
    }
}
