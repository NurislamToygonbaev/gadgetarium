package gadgetarium.validations.issueDate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {IssueDateValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface IssueDateValidation {

    String message() default "The date must be before today!";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

