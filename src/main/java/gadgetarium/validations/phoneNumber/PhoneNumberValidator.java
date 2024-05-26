package gadgetarium.validations.phoneNumber;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumberValidation, String> {

    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^\\+?[0-9. ()-]{7,25}$");

    @Override
    public void initialize(PhoneNumberValidation constraintAnnotation) {
    }

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        if (phoneNumber == null) {
            return false;
        }
        return PHONE_NUMBER_PATTERN.matcher(phoneNumber).matches();
    }
}
