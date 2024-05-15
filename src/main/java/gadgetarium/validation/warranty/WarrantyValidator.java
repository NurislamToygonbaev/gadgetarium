package gadgetarium.validation.warranty;

import gadgetarium.validation.string.StringValidation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class WarrantyValidator implements ConstraintValidator<WarrantyValidation, Integer> {
    @Override
    public boolean isValid(Integer i, ConstraintValidatorContext constraintValidatorContext) {
        if (i <= 0){
            return false;
        }
        return true;
    }
}
