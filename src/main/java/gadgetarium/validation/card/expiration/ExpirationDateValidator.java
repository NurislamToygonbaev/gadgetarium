package gadgetarium.validation.card.expiration;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class ExpirationDateValidator implements ConstraintValidator<ExpirationDateValidation, String> {
    @Override
    public boolean isValid(String date, ConstraintValidatorContext constraintValidatorContext) {
        if (!date.matches("\\d+")) {
            return false;
        }

        LocalDate currentDate = LocalDate.now();
        int year = Integer.parseInt(date);

        return year > currentDate.getYear() || (year == currentDate.getYear());
    }
}
