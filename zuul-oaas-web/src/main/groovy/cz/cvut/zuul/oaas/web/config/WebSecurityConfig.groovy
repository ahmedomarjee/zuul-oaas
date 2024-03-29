/*
 * The MIT License
 *
 * Copyright 2013-2014 Czech Technical University in Prague.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package cz.cvut.zuul.oaas.web.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

import javax.inject.Inject

import static org.springframework.core.Ordered.LOWEST_PRECEDENCE

/**
 * This configuration must be loaded in the root context!
 */
@Configuration
@EnableWebSecurity @Order(LOWEST_PRECEDENCE)
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    // external service
    @Inject @Qualifier('user')
    AuthenticationManager userAuthenticationManager


    AuthenticationManager authenticationManager() {
        userAuthenticationManager
    }

    void configure(HttpSecurity http) {
        http.antMatcher('/**')
            .csrf()
                .disable()  //TODO enable?
            .exceptionHandling()
                .accessDeniedPage('/login.html?authorization_error=true')
                .and()
            .anonymous()
                .and()
            .formLogin()
                .loginPage('/login.html')
                .loginProcessingUrl('/login.do')
                .usernameParameter('j_username')
                .passwordParameter('j_password')
                .defaultSuccessUrl('/')
                .failureUrl('/login.html?authentication_error=true')
                .and()
            .authorizeRequests()
                .antMatchers('/oauth/**').fullyAuthenticated()
    }
}

