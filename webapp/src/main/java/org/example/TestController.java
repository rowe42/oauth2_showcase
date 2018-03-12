package org.example;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

/**
 *
 * @author rowe42
 */
@RestController
@Configuration
public class TestController {

    @Autowired
    public OAuth2RestOperations restTemplate;

    @Autowired
    private TokenStore tokenStore;

    @RequestMapping(method = RequestMethod.GET, value = "/throw")
    public @ResponseBody
    String throwError(HttpServletRequest request, HttpServletResponse response) {

        restTemplate.getOAuth2ClientContext().setAccessToken(null); // No point hanging onto it now
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("client_id", "clientId");
        requestParams.put("redirect_uri", "http://localhost:8082/login");
        requestParams.put("response_type", "code");
        throw new UserRedirectRequiredException("http://localhost:8085/spring-security-oauth-server/oauth/authorize",
                requestParams);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/token")
    public @ResponseBody
    String refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {

        //get current Token
        SecurityContext ctx = SecurityContextHolder.getContext();
        OAuth2Authentication a = (OAuth2Authentication) ctx.getAuthentication();
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

    @RequestMapping(method = RequestMethod.GET, value = "/tokendetails")
    public @ResponseBody
    String getTokenDetails() {
        //get Token
        OAuth2Authentication a = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) a.getDetails();
        String tokenValue = details.getTokenValue();

        //get Claims from Token
        String claims = retrieveClaimsFromJWT(tokenValue);
        LocalDateTime expiredDate = calculateExpirationFromClaims(claims);
        LocalDateTime timeNow = LocalDateTime.now();

        String body = "tokenValue: " + tokenValue + "<p/>"
                + "claims " + claims + "<p/>"
                + "expiryDate " + expiredDate + "<p/>"
                + "now is " + timeNow + "<p/>"
                + "expired " + isTokenExpired(expiredDate, timeNow);
        return body;
    }

    private boolean isTokenExpired(LocalDateTime expiredDate, LocalDateTime timeNow) {
        return expiredDate.isBefore(timeNow);
    }

    private String retrieveClaimsFromJWT(String token) {
        Jwt jwt = JwtHelper.decode(token);
        String claims = jwt.getClaims();
        return claims;
    }

    private LocalDateTime calculateExpirationFromClaims(String claims) {
        JSONObject responseJSON = new JSONObject(claims);
        long exp = responseJSON.getInt("exp");
        Instant instant = Instant.ofEpochSecond(exp);
        LocalDateTime inBerlin = LocalDateTime.ofInstant(instant, ZoneId.of("Europe/Berlin"));

        return inBerlin;
    }
}
