package com.ecommerce.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * Custom annotation to extract user's first name directly from JWT.
 * 
 * Usage: @CurrentUserFirstName String firstName
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal(expression = "claims['firstName']")
public @interface CurrentUserFirstName {
}