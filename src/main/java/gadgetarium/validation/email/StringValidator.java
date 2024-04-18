package gadgetarium.validation.email;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StringValidator implements ConstraintValidator<StringValidation, String> {

    @Override
    public boolean isValid(String soz, ConstraintValidatorContext constraintValidatorContext) {
        return !soz.matches("\\d+");
    }
}
