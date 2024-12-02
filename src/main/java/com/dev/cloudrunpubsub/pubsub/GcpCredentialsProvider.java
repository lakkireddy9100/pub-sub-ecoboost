package dev.cloudrunpubsub.pubsub;


import com.google.api.gax.core.CredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

    public class GcpCredentialsProvider implements CredentialsProvider {

    private final String ClientId;
    private final String ClientSecret;
    private final String gcpProjectId;
    private final String gcpServiceAccount;
    private final RestTemplate restTemplate;

    @Value("${pubsub.source-uri}")
    String sourceURI;
    @Value("${pubsub.sourcetoken-uri}")
    String sourceURIToken;

    public GcpCredentialsProvider(String ClientId, String ClientSecret, String gcpProjectId, String gcpServiceAccount) {
        this.ClientId = ClientId;
        this.ClientSecret = ClientSecret;
        this.gcpProjectId = gcpProjectId;
        this.gcpServiceAccount = gcpServiceAccount;
        restTemplate = new RestTemplate();
    }

    @Override
    public Credentials getCredentials() {
        String wifToken = getWifToken();
        String googleToken = exchangeWifForGoogleToken(wifToken);
        String serviceAccountToken = exchangeGoogleTokenForServiceAccountToken(googleToken);


        return GoogleCredentials.create(new AccessToken(serviceAccountToken, null));
    }

    private String getWifToken() {
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("grant_type", "client_credentials");
        request.add("response_type", "token");
        request.add("client_id", ClientId);
        request.add("client_secret", ClientSecret);
        request.add("resource", String.format(sourceURI, gcpProjectId));
        ResponseEntity<TokenResponse> response = restTemplate.postForEntity("https://dev.com/adfs/oauth2/token", request, TokenResponse.class);
        return response.getBody().getAccess_token();
    }

    private String exchangeWifForGoogleToken(String wifToken) {
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("audience", String.format(sourceURIToken, gcpProjectId));
        request.add("grantType", "urn:ietf:params:oauth:grant-type:token-exchange");
        request.add("requestedTokenType", "urn:ietf:params:oauth:token-type:access_token");
        request.add("scope", "https://www.googleapis.com/auth/cloud-platform");
        request.add("subjectTokenType", "urn:ietf:params:oauth:token-type:jwt");
        request.add("subjectToken", wifToken);
        ResponseEntity<TokenResponse> response = restTemplate.postForEntity("https://sts.googleapis.com/v1/token", request, TokenResponse.class);
        return response.getBody().getAccess_token();
    }

    private String exchangeGoogleTokenForServiceAccountToken(String googleToken) {
        MultiValueMap<String, List<String>> request = new LinkedMultiValueMap<>();
        request.add("scope", Arrays.asList("https://www.googleapis.com/auth/cloud-platform"));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(googleToken);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity requestEntity = new HttpEntity(request, httpHeaders);
        ResponseEntity<ServiceAccountTokenResponse> response = restTemplate.exchange(String.format("https://iamcredentials.googleapis.com/v1/projects/-/serviceAccounts/%s:generateAccessToken", gcpServiceAccount), HttpMethod.POST, requestEntity, ServiceAccountTokenResponse.class);
        return response.getBody().getAccessToken();
    }

    @Data
    public static class ServiceAccountTokenResponse {
        private String accessToken;
        private String expireTime;
    }

    @Data
    public static class TokenResponse {
        private String access_token;
        private String token_type;
        private int expires_in;
    }
}
