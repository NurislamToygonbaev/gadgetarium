package gadgetarium.validation.quantity;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {QuantityValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface QuantityValidation {

    String message() default "{The quantity should not be minus!}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
