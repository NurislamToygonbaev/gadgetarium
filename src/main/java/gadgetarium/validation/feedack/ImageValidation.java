package gadgetarium.validation.feedack;

import gadgetarium.validation.feedack.ImageValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {ImageValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ImageValidation {

    String message() default "{Upload only 5 images}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

