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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package io.meeds.gamification.twitter.storage.cached;

import java.io.Serializable;

import io.meeds.gamification.twitter.model.RemoteTwitterAccount;
import io.meeds.gamification.twitter.model.TwitterAccount;
import io.meeds.gamification.twitter.storage.TwitterConsumerStorage;
import io.meeds.gamification.twitter.storage.cached.model.CacheKey;
import org.exoplatform.commons.cache.future.FutureExoCache;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;

public class TwitterConsumerCachedStorage extends TwitterConsumerStorage {

  public static final String                                   TWITTER_CACHE_NAME    = "twitter.connector";

  private static final int                                     ACCOUNT_BY_ID_CONTEXT = 1;

  private final FutureExoCache<Serializable, Object, CacheKey> twitterFutureCache;

  public TwitterConsumerCachedStorage(CacheService cacheService) {
    ExoCache<Serializable, Object> cacheInstance = cacheService.getCacheInstance(TWITTER_CACHE_NAME);
    this.twitterFutureCache = new FutureExoCache<>((context, key) -> {
      if (ACCOUNT_BY_ID_CONTEXT == context.getContext()) {
        return TwitterConsumerCachedStorage.super.retrieveTwitterAccount(context.getRemoteId(), context.getBearerToken());
      } else {
        throw new UnsupportedOperationException();
      }
    }, cacheInstance);
  }

  @Override
  public RemoteTwitterAccount retrieveTwitterAccount(long twitterRemoteId, String bearerToken) {
    CacheKey cacheKey = new CacheKey(ACCOUNT_BY_ID_CONTEXT, twitterRemoteId, bearerToken);
    return (RemoteTwitterAccount) this.twitterFutureCache.get(cacheKey, cacheKey.hashCode());
  }

  @Override
  public void clearCache() {
    this.twitterFutureCache.clear();
  }

  @Override
  public void clearCache(TwitterAccount twitterAccount, String bearerToken) {
    this.twitterFutureCache.remove(new CacheKey(ACCOUNT_BY_ID_CONTEXT, twitterAccount.getRemoteId(), bearerToken).hashCode());
  }
}
