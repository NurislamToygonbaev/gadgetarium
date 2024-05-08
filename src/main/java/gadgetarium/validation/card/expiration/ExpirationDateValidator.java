package gadgetarium.validation.card.expiration;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class ExpirationDateValidator implements ConstraintValidator<ExpirationDateValidation, LocalDate> {
    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext constraintValidatorContext) {
        LocalDate currentDate = LocalDate.now();

        int month = Integer.parseInt(String.valueOf(date.getMonth()));
        int year = Integer.parseInt(String.valueOf(date.getYear()));

        return year > currentDate.getYear() || (year == currentDate.getYear() && month >= currentDate.getMonthValue());
    }
}
