package com.ecommerce.gateway.filter;

import java.util.Arrays;
import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class RoleBasedAuthorizationFilter extends AbstractGatewayFilterFactory<RoleBasedAuthorizationFilter.Config> {

    public RoleBasedAuthorizationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String userRoles = exchange.getRequest().getHeaders().getFirst("X-User-Roles");
            
            if (userRoles == null || userRoles.isEmpty()) {
                return handleUnauthorized(exchange, "No roles found in request");
            }

            List<String> userRoleList = Arrays.asList(userRoles.split(","));
            List<String> requiredRoles = config.getRequiredRoles();

            if (requiredRoles == null || requiredRoles.isEmpty()) {
                // If no required roles specified, allow access
                return chain.filter(exchange);
            }

            boolean hasRequiredRole = requiredRoles.stream()
                .anyMatch(role -> userRoleList.contains(role.trim()));

            if (!hasRequiredRole) {
                return handleForbidden(exchange, 
                    "User roles: " + userRoles + " do not match required roles: " + requiredRoles);
            }

            return chain.filter(exchange);
        };
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("X-Auth-Error", message);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> handleForbidden(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().add("X-Auth-Error", message);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        private List<String> requiredRoles;

        public List<String> getRequiredRoles() {
            return requiredRoles;
        }

        public void setRequiredRoles(List<String> requiredRoles) {
            this.requiredRoles = requiredRoles;
        }
    }
}