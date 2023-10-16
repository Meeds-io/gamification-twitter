/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.gamification.twitter.plugin;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.AccessTokenRequestParams;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.pkce.PKCE;
import com.github.scribejava.core.pkce.PKCECodeChallengeMethod;
import io.meeds.gamification.model.RemoteConnectorSettings;
import io.meeds.gamification.plugin.ConnectorPlugin;
import io.meeds.gamification.service.ConnectorSettingService;
import io.meeds.gamification.twitter.model.TwitterAccessTokenContext;
import io.meeds.gamification.twitter.model.TwitterOAuth20Api;
import io.meeds.oauth.exception.OAuthException;
import io.meeds.oauth.exception.OAuthExceptionCode;
import org.apache.commons.lang.StringUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class TwitterConnectorPlugin extends ConnectorPlugin {

  private static final Log              LOG            = ExoLogger.getLogger(TwitterConnectorPlugin.class);

  private static final String           CONNECTOR_NAME = "twitter";

  private final ConnectorSettingService connectorSettingService;

  private OAuth20Service                oAuthService;

  private long                          remoteConnectorId;

  public TwitterConnectorPlugin(ConnectorSettingService connectorSettingService) {
    this.connectorSettingService = connectorSettingService;
  }

  @Override
  public String validateToken(String accessToken) {
    RemoteConnectorSettings remoteConnectorSettings = connectorSettingService.getConnectorSettings(CONNECTOR_NAME);
    remoteConnectorSettings.setSecretKey(connectorSettingService.getConnectorSecretKey(CONNECTOR_NAME));
    if (StringUtils.isBlank(remoteConnectorSettings.getApiKey()) || StringUtils.isBlank(remoteConnectorSettings.getSecretKey())) {
      LOG.warn("Missing '{}' connector settings", CONNECTOR_NAME);
      return null;
    }
    try {
      PKCE pkce = new PKCE();
      pkce.setCodeChallenge("challenge");
      pkce.setCodeChallengeMethod(PKCECodeChallengeMethod.PLAIN);
      pkce.setCodeVerifier("challenge");
      AccessTokenRequestParams params = AccessTokenRequestParams.create(accessToken);
      params = params.pkceCodeVerifier(pkce.getCodeVerifier());

      OAuth2AccessToken oAuth2AccessToken = getOAuthService(remoteConnectorSettings).getAccessToken(params);
      TwitterAccessTokenContext twitterAccessTokenContext = new TwitterAccessTokenContext(oAuth2AccessToken);
      String twitterIdentifier = fetchUsernameFromAccessToken(twitterAccessTokenContext);
      if (StringUtils.isBlank(twitterIdentifier)) {
        throw new OAuthException(OAuthExceptionCode.INVALID_STATE, "User Twitter identifier is empty");
      }
      return twitterIdentifier;
    } catch (InterruptedException | IOException e) { // NOSONAR
      throw new OAuthException(OAuthExceptionCode.IO_ERROR, e);
    } catch (ExecutionException e) {
      throw new OAuthException(OAuthExceptionCode.UNKNOWN_ERROR, e);
    }
  }

  @Override
  public String getConnectorName() {
    return CONNECTOR_NAME;
  }

  private OAuth20Service getOAuthService(RemoteConnectorSettings remoteConnectorSettings) {
    if (oAuthService == null || remoteConnectorSettings.hashCode() != remoteConnectorId) {
      remoteConnectorId = remoteConnectorSettings.hashCode();
      oAuthService = new ServiceBuilder(remoteConnectorSettings.getApiKey()).apiSecret(remoteConnectorSettings.getSecretKey())
                                                                            .callback(remoteConnectorSettings.getRedirectUrl())
                                                                            .defaultScope("users.read tweet.read")
                                                                            .build(TwitterOAuth20Api.instance());
    }
    return oAuthService;
  }

  private static String fetchUsernameFromAccessToken(TwitterAccessTokenContext accessToken) throws IOException {
    URL url = new URL("https://api.twitter.com/2/users/me");
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    connection.setRequestProperty("Authorization", "Bearer " + accessToken.getAccessToken());
    int responseCode = connection.getResponseCode();
    if (responseCode == HttpURLConnection.HTTP_OK) { // success
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String inputLine;
      StringBuilder response = new StringBuilder();

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();
      // Extract username from the JSON response
      JSONObject jsonResponse = new JSONObject(response.toString());
      return jsonResponse.getJSONObject("data").getString("username");
    } else {
      throw new IOException("Error retrieving user information from Twitter. Response code: " + responseCode);
    }
  }
}
