package gadgetarium.validation.nounEmail;

import gadgetarium.entities.ZeroBouncerVerifier;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RealEmailValidator implements ConstraintValidator<RealEmailValidation, String> {

    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        return ZeroBouncerVerifier.verifyEmail(email);
    }
}
