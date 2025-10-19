package com.ecommerce.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * Custom annotation to extract user ID directly from JWT.
 * 
 * Usage: @CurrentUserId String userId
 * 
 * This is much cleaner than manually extracting from Authentication or Jwt objects.
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal(expression = "subject")
public @interface CurrentUserId {
}