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
package io.meeds.gamification.twitter.web;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.pkce.PKCE;
import com.github.scribejava.core.pkce.PKCECodeChallengeMethod;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.filter.Filter;

import io.meeds.gamification.model.RemoteConnectorSettings;
import io.meeds.gamification.service.ConnectorSettingService;
import io.meeds.gamification.twitter.model.TwitterOAuth20Api;
import io.meeds.oauth.exception.OAuthException;
import io.meeds.oauth.exception.OAuthExceptionCode;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

public class TwitterConnectorFilter implements Filter {

  private static final Log    LOG            = ExoLogger.getLogger(TwitterConnectorFilter.class);

  private static final String CONNECTOR_NAME = "twitter";

  private OAuth20Service      oAuthService;

  private long                remoteConnectorId;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
    ConnectorSettingService connectorSettingService = CommonsUtils.getService(ConnectorSettingService.class);
    RemoteConnectorSettings remoteConnectorSettings = connectorSettingService.getConnectorSettings(CONNECTOR_NAME);
    remoteConnectorSettings.setSecretKey(connectorSettingService.getConnectorSecretKey(CONNECTOR_NAME));
    if (StringUtils.isBlank(remoteConnectorSettings.getApiKey()) || StringUtils.isBlank(remoteConnectorSettings.getSecretKey())) {
      LOG.warn("Missing '{}' connector settings", CONNECTOR_NAME);
      return;
    }
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    try {
      String secretState = "state";
      PKCE pkce = new PKCE();
      pkce.setCodeChallenge("challenge");
      pkce.setCodeChallengeMethod(PKCECodeChallengeMethod.PLAIN);
      pkce.setCodeVerifier("challenge");
      String authorizationUrl = getOAuthService(remoteConnectorSettings).createAuthorizationUrlBuilder()
                                                                        .pkce(pkce)
                                                                        .state(secretState)
                                                                        .build();
      if (StringUtils.isNotBlank(authorizationUrl)) {
        // Redirect to twitter to perform authentication
        httpResponse.sendRedirect(authorizationUrl);
      }
    } catch (IOException e) { // NOSONAR
      throw new OAuthException(OAuthExceptionCode.IO_ERROR, e);
    }
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
}
