/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Lab contact@meedslab.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.twitter.gamification.service;

import io.meeds.twitter.gamification.model.RemoteTwitterAccount;
import io.meeds.twitter.gamification.model.TokenStatus;
import io.meeds.twitter.gamification.model.TwitterAccount;
import io.meeds.twitter.gamification.model.TwitterTrigger;
import org.exoplatform.commons.exception.ObjectNotFoundException;

import java.util.List;
import java.util.Set;

public interface TwitterConsumerService {

  /**
   * Retrieve available Twitter account info.
   *
   * @param twitterUsername Twitter account username
   * @param bearerToken Twitter bearer token
   * @throws ObjectNotFoundException when the Twitter account identified by its
   *           technical name is not found
   * @return {@link RemoteTwitterAccount}
   */
  RemoteTwitterAccount retrieveTwitterAccount(String twitterUsername, String bearerToken) throws ObjectNotFoundException;

  /**
   * Retrieve available Twitter account info.
   *
   * @param twitterRemoteId Twitter account remote Id
   * @param bearerToken Twitter bearer token
   * @return {@link RemoteTwitterAccount}
   */
  RemoteTwitterAccount retrieveTwitterAccount(long twitterRemoteId, String bearerToken);

  /**
   * Retrieve the list of tweet likers.
   *
   * @param tweetLink Tweet link
   * @param bearerToken Twitter bearer token
   * @return the {@link List} of tweet likers
   */
  Set<String> retrieveTweetLikers(String tweetLink, String bearerToken);

  /**
   * Retrieve the list of tweet retweeters.
   *
   * @param tweetLink Tweet link
   * @param bearerToken Twitter bearer token
   * @return the {@link List} of tweet retweeters
   */
  Set<String> retrieveTweetRetweeters(String tweetLink, String bearerToken);

  /**
   * Check Twitter token status
   *
   * @param bearerToken Twitter bearer token
   * @return {@link TokenStatus}
   */
  TokenStatus checkTwitterTokenStatus(String bearerToken);

  /**
   * Retrieve available tweets in which the account identified by its identifier
   * was mentioned
   *
   * @param twitterAccount {@link TwitterAccount} Twitter account
   * @param lastMentionTweetId last mention tweet Id
   * @param bearerToken Twitter bearer token
   */
  List<TwitterTrigger> getMentionEvents(TwitterAccount twitterAccount, long lastMentionTweetId, String bearerToken);
}
