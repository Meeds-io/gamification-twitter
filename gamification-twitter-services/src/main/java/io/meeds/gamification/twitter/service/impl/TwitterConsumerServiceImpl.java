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
package io.meeds.gamification.twitter.service.impl;

import io.meeds.gamification.twitter.model.RemoteTwitterAccount;
import io.meeds.gamification.twitter.model.TokenStatus;
import io.meeds.gamification.twitter.model.TwitterAccount;
import io.meeds.gamification.twitter.model.TwitterTrigger;
import io.meeds.gamification.twitter.service.TwitterConsumerService;
import io.meeds.gamification.twitter.storage.TwitterConsumerStorage;
import org.exoplatform.commons.exception.ObjectNotFoundException;

import java.util.List;

public class TwitterConsumerServiceImpl implements TwitterConsumerService {

  private final TwitterConsumerStorage twitterConsumerStorage;

  public TwitterConsumerServiceImpl(TwitterConsumerStorage twitterConsumerStorage) {
    this.twitterConsumerStorage = twitterConsumerStorage;
  }

  @Override
  public RemoteTwitterAccount retrieveTwitterAccount(String twitterUsername, String bearerToken) throws ObjectNotFoundException {
    return twitterConsumerStorage.retrieveTwitterAccount(twitterUsername, bearerToken);
  }

  @Override
  public RemoteTwitterAccount retrieveTwitterAccount(long twitterRemoteId, String bearerToken) {
    return twitterConsumerStorage.retrieveTwitterAccount(twitterRemoteId, bearerToken);
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
  public void clearCache() {
    twitterConsumerStorage.clearCache();
  }

  @Override
  public void clearCache(TwitterAccount twitterAccount, String bearerToken) {
    twitterConsumerStorage.clearCache(twitterAccount, bearerToken);
  }
}
