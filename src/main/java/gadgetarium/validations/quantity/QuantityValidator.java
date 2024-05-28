package gadgetarium.validations.quantity;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class QuantityValidator implements ConstraintValidator<QuantityValidation, Integer> {

    @Override
    public boolean isValid(Integer quantity, ConstraintValidatorContext constraintValidatorContext) {
        return quantity > 0 ;
    }
}
