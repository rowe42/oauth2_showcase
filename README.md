# OAuth2 JWT Showcase

This showcase includes
- a Spring Security OAuth2 Authorization Server, which delivers JWT-based Access- and Refresh-Tokens
- a Spring-based Web Application, which is secured against the Autorization Server with Keyword @EnableOAuth2Sso

It demontrates the problem described in Issues
- https://github.com/xdoo/lhm_animad_admin_html5/issues/210 (German)
- https://github.com/spring-projects/spring-security-oauth2-boot/issues/5 (English)

How to use it:

## Checkout
- git checkout
- if you're NOT in Timezone of Germany, you should change org.example.TestController.java, line 63 (`LocalDateTime inBerlin = LocalDateTime.ofInstant(instant, ZoneId.of("Europe/Berlin"));`) according to your timezone.

## Start the Auth Server
- cd oauth-server
- mvn spring-boot:run

## Start the Web App
- cd webapp
- mvn spring-boot:run

## Access the Web App
- Start with a "clean" Browser (no cookies, no caches, reopened)
- Browse to: http://localhost:8082
- Gets redirected to http://localhost:8085/spring-security-oauth-server/login
- Enter username "user" and password "password" (without the "), click "Login"
- Shows page "OAuth Approval - Do you authorize 'clientId' to access your protected resources?"
- Click "Authorize"
- Gets redirected back to http://localhost:8082
- There it displays the following content

```
tokenValue: eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1MTk5MDkxMjgsInVzZXJfbmFtZSI6InVzZXIiLCJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiXSwianRpIjoiZGUwMjk1MDQtZGZhNS00MTI1LTljODgtNWFhNmQxYmU2ODFmIiwiY2xpZW50X2lkIjoiY2xpZW50SWQiLCJzY29wZSI6WyJiYXIiLCJyZWFkIiwid3JpdGUiXX0.cw1URBX7bKcvBvIIOUeD2bKWtZNi0xYPPiOyIvf1hOIi_JmP53IJwDkByoO5WvOpyF0REuW-Sy2znBLd5HPPUyNaDwhFtPVAAByKa4SNY54FXexWoab9NVNSpMCewasWXNCFfbd6SkLGjRBXisLyNlSPjb2mo9AzIuWbSYMikg9phWSxRbESXi_2sk4G_ROYdE7X07TF745MK4s96xLPdDxz_Zo3DXrGgr70IFOQIHmfGg4FI8ggI58-8deRZRwP5AY0XN0_zlmbWHxJIHI3DeSlw-mnVYEDifwxZoKHV4MPGZWrjoxmTHeM26Gpf9S4Pw2n0cuHzynm037KAdOEpA
claims {"exp":1519909128,"user_name":"user","authorities":["ROLE_USER"],"jti":"de029504-dfa5-4125-9c88-5aa6d1be681f","client_id":"clientId","scope":["bar","read","write"]}

expiryDate 2018-03-01T13:58:48

now is 2018-03-01T13:58:39.068

expired false
```

- Hit refresh a couple of times, at some point the text changes to

```
tokenValue: eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1MTk5MDkxMjgsInVzZXJfbmFtZSI6InVzZXIiLCJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiXSwianRpIjoiZGUwMjk1MDQtZGZhNS00MTI1LTljODgtNWFhNmQxYmU2ODFmIiwiY2xpZW50X2lkIjoiY2xpZW50SWQiLCJzY29wZSI6WyJiYXIiLCJyZWFkIiwid3JpdGUiXX0.cw1URBX7bKcvBvIIOUeD2bKWtZNi0xYPPiOyIvf1hOIi_JmP53IJwDkByoO5WvOpyF0REuW-Sy2znBLd5HPPUyNaDwhFtPVAAByKa4SNY54FXexWoab9NVNSpMCewasWXNCFfbd6SkLGjRBXisLyNlSPjb2mo9AzIuWbSYMikg9phWSxRbESXi_2sk4G_ROYdE7X07TF745MK4s96xLPdDxz_Zo3DXrGgr70IFOQIHmfGg4FI8ggI58-8deRZRwP5AY0XN0_zlmbWHxJIHI3DeSlw-mnVYEDifwxZoKHV4MPGZWrjoxmTHeM26Gpf9S4Pw2n0cuHzynm037KAdOEpA
claims {"exp":1519909128,"user_name":"user","authorities":["ROLE_USER"],"jti":"de029504-dfa5-4125-9c88-5aa6d1be681f","client_id":"clientId","scope":["bar","read","write"]}

expiryDate 2018-03-01T13:58:48

now is 2018-03-01T14:00:02.488

expired true
```

Notice the last line, which shows that the JWT token is expired now (the access token is configured to only 10 seconds livetime, so this will happen quickly). 

However, the WebApp does NOT
1. refuse access or
2. get a new access token using the refresh token or
3. forward to user back to the Auth Server

How does the configuration need to change in order to make this happen? Obviously Option 2. above is preferred / expected.
