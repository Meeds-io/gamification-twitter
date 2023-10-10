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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import io.meeds.gamification.model.RemoteConnectorSettings;
import io.meeds.gamification.service.ConnectorSettingService;
import io.meeds.oauth.common.OAuthConstants;
import io.meeds.oauth.exception.OAuthException;
import io.meeds.oauth.exception.OAuthExceptionCode;
import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.filter.Filter;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterConnectorFilter implements Filter {

  private static final Log    LOG            = ExoLogger.getLogger(TwitterConnectorFilter.class);

  private static final String CONNECTOR_NAME = "twitter";

  private TwitterFactory      twitterFactory;

  private long                remoteConnectorId;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException {
    ConnectorSettingService connectorSettingService = CommonsUtils.getService(ConnectorSettingService.class);
    RemoteConnectorSettings remoteConnectorSettings = connectorSettingService.getConnectorSettings(CONNECTOR_NAME);
    remoteConnectorSettings.setSecretKey(connectorSettingService.getConnectorSecretKey(CONNECTOR_NAME));
    if (StringUtils.isBlank(remoteConnectorSettings.getApiKey()) || StringUtils.isBlank(remoteConnectorSettings.getSecretKey())) {
      LOG.warn("Missing '{}' connector settings", CONNECTOR_NAME);
      return;
    }
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    HttpSession session = httpRequest.getSession();

    Twitter twitter = getTwitterFactory(remoteConnectorSettings).getInstance();

    try {
      String redirectUrl = URLEncoder.encode(remoteConnectorSettings.getRedirectUrl(), StandardCharsets.UTF_8);
      RequestToken requestToken = twitter.getOAuthRequestToken(redirectUrl);

      // Save requestToken to session, but only temporarily until oauth
      // workflow is finished
      session.setAttribute(OAuthConstants.ATTRIBUTE_TWITTER_REQUEST_TOKEN, requestToken);

      // Redirect to twitter to perform authentication
      httpResponse.sendRedirect(requestToken.getAuthenticationURL());

    } catch (TwitterException twitterException) {
      throw new OAuthException(OAuthExceptionCode.TWITTER_ERROR, twitterException);
    }
  }

  public TwitterFactory getTwitterFactory(RemoteConnectorSettings remoteConnectorSettings) {
    if (twitterFactory == null || remoteConnectorSettings.hashCode() != remoteConnectorId) {
      remoteConnectorId = remoteConnectorSettings.hashCode();
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.setOAuthConsumerKey(remoteConnectorSettings.getApiKey())
             .setOAuthConsumerSecret(remoteConnectorSettings.getSecretKey());
      twitterFactory = new TwitterFactory(builder.build());
    }
    return twitterFactory;
  }
}
