package gadgetarium.validation.rating;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {RatingValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)

public @interface RatingValidation {
    String message() default "{The rating from 1 to 5 should be}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
