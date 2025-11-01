package com.ecommerce.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Custom annotation to check if the current user has admin privileges.
 * This annotation can be used on methods to restrict access to admin users only.
 * 
 * The user must have the 'SCOPE_ADMIN' authority to access the annotated method.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
public @interface IsAdminUser {
}