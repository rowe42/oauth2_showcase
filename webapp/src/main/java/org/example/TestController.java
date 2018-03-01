package org.example;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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


/**
 *
 * @author rowe42
 */
@RestController
@Configuration
public class TestController {

    @RequestMapping(method = RequestMethod.GET, value = "/")
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
        
        String body = "tokenValue: " + tokenValue + "<p/>" +
                "claims " + claims + "<p/>" +
                "expiryDate " + expiredDate + "<p/>" + 
                "now is " + timeNow + "<p/>" +
                "expired " + isTokenExpired(expiredDate, timeNow);
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
        Instant instant  = Instant.ofEpochSecond(exp);
        LocalDateTime inBerlin = LocalDateTime.ofInstant(instant, ZoneId.of("Europe/Berlin"));

        return inBerlin;
    }
}
