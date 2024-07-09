/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2024 Meeds Lab contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package io.meeds.gamification.twitter.storage.mapper;

import io.meeds.gamification.twitter.entity.TwitterTweetEntity;
import io.meeds.gamification.twitter.model.Tweet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.social.core.manager.IdentityManager;

import io.meeds.gamification.twitter.entity.TwitterAccountEntity;
import io.meeds.gamification.twitter.model.TwitterAccount;
import io.meeds.gamification.utils.Utils;

public class TwitterTweetMapper {

  private TwitterTweetMapper() {
    // Class with static methods
  }

  public static TwitterTweetEntity toEntity(Tweet tweet) {
    if (tweet == null) {
      return null;
    }
    TwitterTweetEntity twitterTweetEntity = new TwitterTweetEntity();

    if (tweet.getTweetId() > 0) {
      twitterTweetEntity.setTweetId(tweet.getTweetId());
    }
    if (StringUtils.isNotBlank(tweet.getTweetLink())) {
      twitterTweetEntity.setTweetLink(tweet.getTweetLink());
    }
    if (CollectionUtils.isNotEmpty(tweet.getLikers())) {
      twitterTweetEntity.setLikers(tweet.getLikers());
    }
    if (CollectionUtils.isNotEmpty(tweet.getRetweeters())) {
      twitterTweetEntity.setRetweeters(tweet.getRetweeters());
    }
    return twitterTweetEntity;
  }

  public static Tweet fromEntity(TwitterTweetEntity twitterTweetEntity) {
    if (twitterTweetEntity == null) {
      return null;
    }
    return new Tweet(twitterTweetEntity.getTweetId(),
                     twitterTweetEntity.getTweetLink(),
                     twitterTweetEntity.getLikers(),
                     twitterTweetEntity.getRetweeters());
  }

}
