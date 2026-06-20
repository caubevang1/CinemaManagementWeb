package com.cinemaweb.API.Cinema.Web.validator;

import java.lang.annotation.*;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueEmailValidator.class)
@Documented
public @interface UniqueEmail {
    String message() default "Email is already in use!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
