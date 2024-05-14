package gadgetarium.validation.card.cvc;

import gadgetarium.validation.card.expiration.ExpirationDateValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {CvcValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CvcValidation {

    String message() default "{cvc not be empty}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
