package gadgetarium.validations.number;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {NumberValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NumberValidation {
    String message() default "{Email must's contain @ symbol and ends with .com}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
