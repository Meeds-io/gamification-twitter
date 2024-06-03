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
package io.meeds.twitter.gamification.web;

import org.apache.commons.lang3.StringUtils;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.pkce.PKCE;
import com.github.scribejava.core.pkce.PKCECodeChallengeMethod;

import org.exoplatform.commons.utils.CommonsUtils;

import io.meeds.gamification.model.RemoteConnectorSettings;
import io.meeds.gamification.service.ConnectorSettingService;
import io.meeds.twitter.gamification.model.TwitterOAuth20Api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import static io.meeds.twitter.gamification.utils.Utils.CONNECTOR_NAME;

@Controller
@RequestMapping("/")
public class TwitterConnectionFilter {

  private OAuth20Service oAuthService;

  private long           remoteConnectorId;

  @GetMapping("/twitterOauth")
  public ModelAndView redirectToAuthorization() {
    ConnectorSettingService connectorSettingService = CommonsUtils.getService(ConnectorSettingService.class);
    RemoteConnectorSettings remoteConnectorSettings = connectorSettingService.getConnectorSettings(CONNECTOR_NAME);
    remoteConnectorSettings.setSecretKey(connectorSettingService.getConnectorSecretKey(CONNECTOR_NAME));

    if (StringUtils.isBlank(remoteConnectorSettings.getApiKey()) || StringUtils.isBlank(remoteConnectorSettings.getSecretKey())) {
      // Handle missing connector settings, perhaps by showing an error page or
      // message
      return new ModelAndView("errorPage"); // Assume errorPage is a valid view
    }

    String authorizationUrl = getAuthorizationUrl(remoteConnectorSettings);
    return new ModelAndView("redirect:" + authorizationUrl);
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

  private String getAuthorizationUrl(RemoteConnectorSettings remoteConnectorSettings) {
    String secretState = "state";
    PKCE pkce = new PKCE();
    pkce.setCodeChallenge("challenge");
    pkce.setCodeChallengeMethod(PKCECodeChallengeMethod.PLAIN);
    pkce.setCodeVerifier("challenge");

    return getOAuthService(remoteConnectorSettings).createAuthorizationUrlBuilder().pkce(pkce).state(secretState).build();
  }
}
