package gadgetarium.validation.card.cvc;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CvcValidator implements ConstraintValidator<CvcValidation, String> {
    @Override
    public boolean isValid(String cvc, ConstraintValidatorContext constraintValidatorContext) {

        return cvc != null && cvc.matches("\\d{3,4}");
    }
}
