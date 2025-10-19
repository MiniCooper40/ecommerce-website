package com.ecommerce.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * Custom annotation to extract user roles directly from JWT.
 * 
 * Usage: @CurrentUserRoles List<String> roles
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal(expression = "claims['roles']")
public @interface CurrentUserRoles {
}