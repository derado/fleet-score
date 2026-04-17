package com.fleetscore.regatta.api.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PositionOrCircumstanceValidator.class)
@Documented
public @interface PositionOrCircumstance {

  String message() default "Exactly one of position or circumstance must be provided";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
