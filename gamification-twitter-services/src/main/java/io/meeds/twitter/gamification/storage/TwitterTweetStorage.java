/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Lab contact@meedslab.com
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
package io.meeds.twitter.gamification.storage;

import io.meeds.twitter.gamification.dao.TwitterTweetDAO;
import io.meeds.twitter.gamification.entity.TwitterTweetEntity;
import io.meeds.twitter.gamification.model.Tweet;
import io.meeds.twitter.gamification.storage.mapper.TwitterTweetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

import static io.meeds.twitter.gamification.storage.mapper.TwitterTweetMapper.fromEntity;
import static io.meeds.twitter.gamification.storage.mapper.TwitterTweetMapper.toEntity;

@Repository
public class TwitterTweetStorage {

  @Autowired
  private TwitterTweetDAO twitterTweetDAO;

  public Tweet addTweetToWatch(Tweet tweet) {
    Tweet existsTweet = getTweetByLink(tweet.getTweetLink());
    if (existsTweet == null) {
      TwitterTweetEntity twitterTweetEntity = toEntity(tweet);
      twitterTweetEntity = twitterTweetDAO.save(twitterTweetEntity);
      return fromEntity(twitterTweetEntity);
    } else {
      return null;
    }
  }

  public Tweet updateTweetReactions(long tweetId, Set<String> likers, Set<String> retweeters) {
    TwitterTweetEntity twitterTweetEntity = twitterTweetDAO.findById(tweetId).orElse(null);
    if (twitterTweetEntity == null) {
      return null;
    }
    twitterTweetEntity.setLikers(likers);
    twitterTweetEntity.setRetweeters(retweeters);
    return fromEntity(twitterTweetDAO.save(twitterTweetEntity));
  }

  public Page<Tweet> getTweets(Pageable pageable) {
    Page<TwitterTweetEntity> page = twitterTweetDAO.findAll(pageable);
    return page.map(TwitterTweetMapper::fromEntity);
  }

  public List<Tweet> getTweets() {
    List<TwitterTweetEntity> twitterTweetEntities = twitterTweetDAO.findAll();
    return twitterTweetEntities.stream().map(TwitterTweetMapper::fromEntity).toList();
  }

  public long countTweets() {
    return twitterTweetDAO.count();
  }

  public Tweet getTweetById(Long tweetId) {
    return fromEntity(twitterTweetDAO.findById(tweetId).orElse(null));
  }

  public Tweet getTweetByLink(String tweetLink) {
    TwitterTweetEntity twitterTweetEntity = twitterTweetDAO.findTwitterTweetEntityByTweetLink(tweetLink);
    return fromEntity(twitterTweetEntity);
  }

  public Tweet deleteTweet(long tweetId) {
    TwitterTweetEntity twitterTweetEntity = twitterTweetDAO.findById(tweetId).orElse(null);
    if (twitterTweetEntity != null) {
      twitterTweetDAO.delete(twitterTweetEntity);
    }
    return fromEntity(twitterTweetEntity);
  }
}
