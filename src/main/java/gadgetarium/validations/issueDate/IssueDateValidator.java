package gadgetarium.validations.issueDate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class IssueDateValidator implements ConstraintValidator<IssueDateValidation, LocalDate> {
    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext constraintValidatorContext) {
        LocalDate today = LocalDate.now();

        return date != null && date.isBefore(today);
    }
}

