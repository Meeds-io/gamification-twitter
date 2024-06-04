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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.twitter.gamification.service.impl;

import io.meeds.twitter.gamification.model.RemoteTwitterAccount;
import io.meeds.twitter.gamification.model.TokenStatus;
import io.meeds.twitter.gamification.model.TwitterAccount;
import io.meeds.twitter.gamification.model.TwitterTrigger;
import io.meeds.twitter.gamification.service.TwitterConsumerService;
import io.meeds.twitter.gamification.storage.TwitterConsumerStorage;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class TwitterConsumerServiceImpl implements TwitterConsumerService {

  @Autowired
  private TwitterConsumerStorage twitterConsumerStorage;

  @Override
  public RemoteTwitterAccount retrieveTwitterAccount(String twitterUsername, String bearerToken) throws ObjectNotFoundException {
    return twitterConsumerStorage.retrieveTwitterAccount(twitterUsername, bearerToken);
  }

  @Override
  public RemoteTwitterAccount retrieveTwitterAccount(long twitterRemoteId, String bearerToken) {
    return twitterConsumerStorage.retrieveTwitterAccount(twitterRemoteId, bearerToken);
  }

  public Set<String> retrieveTweetLikers(String tweetLink, String bearerToken) {
    return twitterConsumerStorage.retrieveTweetLikers(tweetLink, bearerToken);
  }

  public Set<String> retrieveTweetRetweeters(String tweetLink, String bearerToken) {
    return twitterConsumerStorage.retrieveTweetRetweeters(tweetLink, bearerToken);
  }

  @Override
  public TokenStatus checkTwitterTokenStatus(String bearerToken) {
    return twitterConsumerStorage.checkTwitterTokenStatus(bearerToken);
  }

  @Override
  public List<TwitterTrigger> getMentionEvents(TwitterAccount twitterAccount, long lastMentionTweetId, String bearerToken) {
    return twitterConsumerStorage.getMentionEvents(twitterAccount, lastMentionTweetId, bearerToken);
  }

  @Override
  @CacheEvict(value = "twitterAccount", allEntries = true)
  public void clearCache() {
    twitterConsumerStorage.clearCache();
  }
}
