package gadgetarium.validations.countSim;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CountSimValidator implements ConstraintValidator<CountSimValidation, Integer> {
    @Override
    public boolean isValid(Integer countSim, ConstraintValidatorContext constraintValidatorContext) {
        return countSim > 0 && 2 >= countSim;
    }
}
