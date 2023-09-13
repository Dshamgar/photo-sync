package com.schroeder.photosync.util;

import static com.schroeder.photosync.util.Constants.*;

import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthorizationUtil {
  private static final Logger logger = LoggerFactory.getLogger(AuthorizationUtil.class);

  public static GoogleCredentials getCredentials(
      String code, String secret, String photosyncCallbackEndpoint, String googleApiClientId)
      throws IOException {
    GoogleTokenResponse response = null;
    GoogleCredentials googleCredentials = null;

    try {

      logger.debug("SECRET!!!!: " + secret);
      response =
          new GoogleAuthorizationCodeTokenRequest(
                  new NetHttpTransport(),
                  new GsonFactory(),
                  googleApiClientId,
                  secret,
                  code,
                  photosyncCallbackEndpoint)
              .execute();
      logger.debug("AccessToken: " + response.getAccessToken());
      logger.debug("ExpiresInSeconds: " + response.getExpiresInSeconds());
      logger.debug("RefreshToken" + response.getRefreshToken());
      AccessToken accessToken =
          AccessToken.newBuilder()
              .setTokenValue(response.getAccessToken())
              .setScopes(SCOPES)
              .build();
      googleCredentials = GoogleCredentials.create(accessToken);
    } catch (TokenResponseException e) {
      if (e.getDetails() != null) {
        logger.error("Error: " + e.getDetails().getError());
        if (e.getDetails().getErrorDescription() != null) {
          logger.error(e.getDetails().getErrorDescription());
        }
        if (e.getDetails().getErrorUri() != null) {
          logger.error(e.getDetails().getErrorUri());
        }
      } else {
        logger.error(e.getMessage());
      }
    }
    logger.debug("googleCredentials: " + googleCredentials);
    return googleCredentials;
  }
}
