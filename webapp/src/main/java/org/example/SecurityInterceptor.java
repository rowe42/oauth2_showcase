/**
 *
 * @author roland
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.example;

/**
 *
 * @author roland
 */

//import com.google.common.base.Strings;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.ResponseBody;

public class SecurityInterceptor extends HandlerInterceptorAdapter {

    private static Logger log = LoggerFactory.getLogger(SecurityInterceptor.class);

        @Autowired
    public OAuth2RestOperations restTemplate;

    @Autowired
    private TokenStore tokenStore;
    /**
     * Executed before actual handler is executed
     **/
    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {
        this.refreshToken(request, response);
        return true;
    }

    private String refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        //get current Token
        SecurityContext ctx = SecurityContextHolder.getContext();
        OAuth2Authentication a = (OAuth2Authentication) ctx.getAuthentication();
        if (a == null) return "";
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) a.getDetails();
        String tokenValue = details.getTokenValue();
        System.out.println("Old: " + tokenValue);

        try {
            //get new access token if renewal is necessary
            OAuth2AccessToken newAccessToken = restTemplate.getAccessToken();
            String newTokenValue = newAccessToken.getValue();
            System.out.println("New: " + newTokenValue);

            //create new OAuth2Authentication object
            OAuth2Authentication newOAuth2Authentication = tokenStore.readAuthentication(newAccessToken);

            //create new OAuth2AuthenticationDetails object and attach to OAuth2Authentication
            request.setAttribute(OAuth2AuthenticationDetails.ACCESS_TOKEN_VALUE, newTokenValue);
            request.setAttribute(OAuth2AuthenticationDetails.ACCESS_TOKEN_TYPE, "Bearer");
            OAuth2AuthenticationDetails newDetails = new OAuth2AuthenticationDetails(request);
            newOAuth2Authentication.setDetails(newDetails);

            //put new OAuth2Authentication with new token into SecurityContext
            ctx.setAuthentication(newOAuth2Authentication);
            SecurityContextHolder.setContext(ctx);

        } catch (UserRedirectRequiredException e) {
            //Refresh token not valid any more
            //now reset the SecurityContext, so redirect will work
            SecurityContext ctxNew = new SecurityContextImpl();
            SecurityContextHolder.setContext(ctxNew);
            //redirect to self (re-throwing the Exception will result in a forever-living token - don't know why)
            response.sendRedirect(request.getRequestURI());
        }

        String body = "Token: " + tokenValue;
        return body;
    }

    
}

