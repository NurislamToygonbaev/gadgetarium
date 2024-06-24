package gadgetarium.validations.countSim;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {CountSimValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CountSimValidation {

    String message() default "The number of SIM cards should not be negative and no more than 2";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
