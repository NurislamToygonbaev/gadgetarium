package gadgetarium.validation.string;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StringValidator implements ConstraintValidator<StringValidation, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) {
            return false; // Возвращаем false, если значение равно null
        }
        // Убираем начальные и конечные пробелы, и проверяем, пустая ли строка
        return !value.trim().isEmpty();
    }
}
