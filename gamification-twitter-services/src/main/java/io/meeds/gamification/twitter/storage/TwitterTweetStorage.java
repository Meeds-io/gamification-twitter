/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.gamification.twitter.storage;

import io.meeds.gamification.twitter.dao.TwitterTweetDAO;
import io.meeds.gamification.twitter.entity.TwitterTweetEntity;
import io.meeds.gamification.twitter.model.Tweet;

import java.util.List;
import java.util.Set;

import static io.meeds.gamification.twitter.storage.mapper.TwitterTweetMapper.fromEntity;
import static io.meeds.gamification.twitter.storage.mapper.TwitterTweetMapper.toEntity;

public class TwitterTweetStorage {

  private final TwitterTweetDAO twitterTweetDAO;

  public TwitterTweetStorage(TwitterTweetDAO twitterTweetDAO) {
    this.twitterTweetDAO = twitterTweetDAO;
  }

  public Tweet addTweetToWatch(Tweet tweet) {
    Tweet existsTweet = getTweetByLink(tweet.getTweetLink());
    if (existsTweet == null) {
      TwitterTweetEntity twitterTweetEntity = toEntity(tweet);
      twitterTweetEntity = twitterTweetDAO.create(twitterTweetEntity);
      return fromEntity(twitterTweetEntity);
    } else {
      return null;
    }
  }

  public Tweet updateTweetReactions(long tweetId, Set<String> likers, Set<String> retweeters) {
    TwitterTweetEntity twitterTweetEntity = twitterTweetDAO.find(tweetId);
    twitterTweetEntity.setLikers(likers);
    twitterTweetEntity.setRetweeters(retweeters);
    return fromEntity(twitterTweetDAO.update(twitterTweetEntity));
  }

  public List<Long> getTweets(int offset, int limit) {
    return twitterTweetDAO.getTweetsIds(offset, limit);
  }

  public int countTweets() {
    return twitterTweetDAO.count().intValue();
  }

  public Tweet getTweetById(Long tweetId) {
    return fromEntity(twitterTweetDAO.find(tweetId));
  }

  public Tweet getTweetByLink(String tweetLink) {
    TwitterTweetEntity twitterTweetEntity = twitterTweetDAO.getTweetByLink(tweetLink);
    return fromEntity(twitterTweetEntity);
  }

  public Tweet deleteTweet(long tweetId) {
    TwitterTweetEntity twitterTweetEntity = twitterTweetDAO.find(tweetId);
    if (twitterTweetEntity != null) {
      twitterTweetDAO.delete(twitterTweetEntity);
    }
    return fromEntity(twitterTweetEntity);
  }
}
