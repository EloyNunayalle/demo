package org.example.proyecto.config;

import lombok.RequiredArgsConstructor;
import org.example.proyecto.Usuario.Domain.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    @Autowired
    private UsuarioService userService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        // Rutas públicas
                        .requestMatchers("/auth/**").permitAll()


                        // Acceso de USER a sus propios ítems
                        .requestMatchers(HttpMethod.POST, "/item").hasAuthority("USER")
                        .requestMatchers(HttpMethod.PUT, "/item/**").hasAuthority("USER")
                        .requestMatchers(HttpMethod.DELETE, "/item/**").hasAuthority("USER")
                        .requestMatchers(HttpMethod.GET, "/item/category/{categoryId}").hasAuthority("USER")
                        .requestMatchers(HttpMethod.GET, "/item/user/{userId}").hasAuthority("USER")
                        .requestMatchers(HttpMethod.GET, "/item/mine").hasAuthority("USER")
                        .requestMatchers(HttpMethod.GET, "/item/{id}").hasAuthority("USER")
                        // Acceso de ADMIN a sus propios ítems
                        .requestMatchers(HttpMethod.POST, "/item/{itemId}/approve").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/item").hasAuthority("ADMIN")

                        // Acceso de USER a categorías
                        .requestMatchers(HttpMethod.GET, "/category").hasAuthority("USER") // Ver todas las categorías
                        .requestMatchers(HttpMethod.GET, "/category/{id}").hasAuthority("USER") // Ver una categoría por ID
                        // Acceso de ADMIN a categorías
                        .requestMatchers(HttpMethod.POST, "/category").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/category/{id}").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/category/{id}").hasAuthority("ADMIN")

                        // Acceso de USER a ratings
                        .requestMatchers(HttpMethod.POST, "/ratings/crear").hasAuthority("USER")
                        .requestMatchers(HttpMethod.GET, "/ratings/usuario/{usuarioId}").hasAuthority("USER")
                        .requestMatchers(HttpMethod.DELETE, "/ratings/{id}").hasAuthority("USER")
                        // Acceso de ADMIN a ratings
                        .requestMatchers(HttpMethod.GET, "/ratings/listar").hasAuthority("ADMIN")


                        // Acceso de USER a agreements
                        .requestMatchers(HttpMethod.POST, "/agreements/crear").hasAuthority("USER")
                        .requestMatchers(HttpMethod.PUT, "/agreements/{id}/accept").hasAuthority("USER")
                        .requestMatchers(HttpMethod.PUT, "/agreements/{id}/reject").hasAuthority("USER")
                        // Acceso de ADMIN a agreements
                        .requestMatchers(HttpMethod.GET, "/agreements/{id}").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/agreements/{id}").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/agreements/{id}").hasAuthority("ADMIN")

                        // Acceso de USER a sus propios datos de usuario
                        .requestMatchers(HttpMethod.GET, "/usuarios/me").hasAuthority("USER")
                        .requestMatchers(HttpMethod.PUT, "/usuarios/{id}").hasAuthority("USER")
                        .requestMatchers(HttpMethod.DELETE, "/usuarios/{id}").hasAuthority("USER")
                        // Acceso de ADMIN a sus propios datos de usuario
                        .requestMatchers(HttpMethod.GET, "/usuarios/listar").hasAuthority("ADMIN")

                        // Acceso de ADMIN a todas las rutas
                        .requestMatchers("/item/**").hasAuthority("ADMIN")
                        .requestMatchers("/category/**").hasAuthority("ADMIN")
                        .requestMatchers("/usuarios/**").hasAuthority("ADMIN")
                        .requestMatchers("/shipments/**").hasAuthority("ADMIN")
                        .requestMatchers("/agreements/**").hasAuthority("ADMIN")
                        .requestMatchers("/ratings/**").hasAuthority("ADMIN")
                )
                .sessionManagement(manager -> manager.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setUserDetailsService(userService.userDetailsService());
        return authProvider;
    }


    @Bean
    static RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy("ADMIN > USER");

        return hierarchy;
    }

    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy);
        expressionHandler.setDefaultRolePrefix("");

        return expressionHandler;
    }




    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
