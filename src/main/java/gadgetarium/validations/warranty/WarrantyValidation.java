package gadgetarium.validations.warranty;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {WarrantyValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface WarrantyValidation {

    String message() default "{The guarantee should not be 0!}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

