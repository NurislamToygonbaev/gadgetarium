package gadgetarium.validation.card.number;

import gadgetarium.validation.discount.DiscountValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {CardNumberValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CardNumberValidation {

    String message() default "{must be valid card number}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
