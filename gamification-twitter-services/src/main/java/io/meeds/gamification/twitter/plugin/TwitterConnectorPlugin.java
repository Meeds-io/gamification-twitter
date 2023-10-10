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

import io.meeds.gamification.model.RemoteConnectorSettings;
import io.meeds.gamification.plugin.ConnectorPlugin;
import io.meeds.gamification.service.ConnectorSettingService;
import io.meeds.oauth.common.OAuthConstants;
import io.meeds.oauth.exception.OAuthException;
import io.meeds.oauth.exception.OAuthExceptionCode;
import io.meeds.oauth.twitter.TwitterAccessTokenContext;
import org.apache.commons.lang.StringUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.gatein.sso.agent.tomcat.ServletAccess;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TwitterConnectorPlugin extends ConnectorPlugin {

  private static final Log              LOG            = ExoLogger.getLogger(TwitterConnectorPlugin.class);

  private static final String           CONNECTOR_NAME = "twitter";

  private final ConnectorSettingService connectorSettingService;

  private TwitterFactory                twitterFactory;

  private long                          remoteConnectorId;

  public TwitterConnectorPlugin(ConnectorSettingService connectorSettingService) {
    this.connectorSettingService = connectorSettingService;
  }

  @Override
  public String validateToken(String accessTokens) {
    RemoteConnectorSettings remoteConnectorSettings = connectorSettingService.getConnectorSettings(CONNECTOR_NAME);
    remoteConnectorSettings.setSecretKey(connectorSettingService.getConnectorSecretKey(CONNECTOR_NAME));
    if (StringUtils.isBlank(remoteConnectorSettings.getApiKey()) || StringUtils.isBlank(remoteConnectorSettings.getSecretKey())) {
      LOG.warn("Missing '{}' connector settings", CONNECTOR_NAME);
      return null;
    }
    Twitter twitter = getTwitterFactory(remoteConnectorSettings).getInstance();
    String pattern = "oauth_verifier=([^&]+)";
    Pattern r = Pattern.compile(pattern);
    Matcher m = r.matcher(accessTokens);
    if (m.find()) {
      String verifier = m.group(1);
      AccessToken accessToken;
      User twitterUser;
      try {
        HttpServletRequest servletRequest = ServletAccess.getRequest();
        HttpSession session = servletRequest.getSession();
        RequestToken requestToken = (RequestToken) session.getAttribute(OAuthConstants.ATTRIBUTE_TWITTER_REQUEST_TOKEN);
        accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
        session.removeAttribute(OAuthConstants.ATTRIBUTE_TWITTER_REQUEST_TOKEN);
        TwitterAccessTokenContext accessTokenContext = new TwitterAccessTokenContext(accessToken.getToken(),
                                                                                     accessToken.getTokenSecret());

        twitter = getAuthorizedTwitterInstance(accessTokenContext, remoteConnectorSettings);
        twitterUser = twitter.verifyCredentials();
        if (twitterUser == null || StringUtils.isBlank(twitterUser.getScreenName())) {
          throw new OAuthException(OAuthExceptionCode.INVALID_STATE, "User Twitter identifier is empty");
        }
      } catch (TwitterException e) {
        throw new OAuthException(OAuthExceptionCode.TWITTER_ERROR, "Error when obtaining user", e);
      }
      return twitterUser.getScreenName();
    } else {
      return null;
    }
  }

  @Override
  public String getConnectorName() {
    return CONNECTOR_NAME;
  }

  private TwitterFactory getTwitterFactory(RemoteConnectorSettings remoteConnectorSettings) {
    if (twitterFactory == null || remoteConnectorSettings.hashCode() != remoteConnectorId) {
      remoteConnectorId = remoteConnectorSettings.hashCode();
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.setOAuthConsumerKey(remoteConnectorSettings.getApiKey())
             .setOAuthConsumerSecret(remoteConnectorSettings.getSecretKey());
      twitterFactory = new TwitterFactory(builder.build());
    }
    return twitterFactory;
  }

  private Twitter getAuthorizedTwitterInstance(TwitterAccessTokenContext accessTokenContext,
                                               RemoteConnectorSettings remoteConnectorSettings) {
    ConfigurationBuilder builder = new ConfigurationBuilder();
    builder.setOAuthConsumerKey(remoteConnectorSettings.getApiKey())
           .setOAuthConsumerSecret(remoteConnectorSettings.getSecretKey());

    builder.setOAuthAccessToken(accessTokenContext.getAccessToken());
    builder.setOAuthAccessTokenSecret(accessTokenContext.getAccessTokenSecret());

    return new TwitterFactory(builder.build()).getInstance();
  }
}
