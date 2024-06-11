package gadgetarium.validations.issueDate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class IssueDateValidator implements ConstraintValidator<IssueDateValidation, String> {

    @Override
    public boolean isValid(String dateStr, ConstraintValidatorContext constraintValidatorContext) {
        if (dateStr == null || dateStr.isEmpty()) {
            return false;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            LocalDate date = LocalDate.parse(dateStr, formatter);
            LocalDate today = LocalDate.now();
            return date.isBefore(today);
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
