package com.kotomka.config;

import com.kotomka.core.security.AdminAuthProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@ComponentScan(basePackages = "com.kotomka")
public class WebAppSecurityConfig extends WebSecurityConfigurerAdapter {

    private final AdminAuthProvider adminAuthProvider;

    @Autowired
    public WebAppSecurityConfig(AdminAuthProvider adminAuthProvider) {
        this.adminAuthProvider = adminAuthProvider;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(adminAuthProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/admin*/**").authenticated()
                .anyRequest().permitAll();
        http
                .formLogin()
                .loginPage("/admin/login")
                .defaultSuccessUrl("/admin/")
                .failureUrl("/admin/login?error")
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin/login?logout")
                .invalidateHttpSession(true)
                .permitAll();
    }
}