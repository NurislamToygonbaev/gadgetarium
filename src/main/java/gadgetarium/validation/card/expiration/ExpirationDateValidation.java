package gadgetarium.validation.card.expiration;

import gadgetarium.validation.card.number.CardNumberValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {ExpirationDateValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExpirationDateValidation {

    String message() default "{date and year must be before current date}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
