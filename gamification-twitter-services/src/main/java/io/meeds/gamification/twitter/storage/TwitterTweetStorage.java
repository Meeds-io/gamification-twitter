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
import io.meeds.gamification.twitter.storage.mapper.TwitterTweetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

import static io.meeds.gamification.twitter.storage.mapper.TwitterTweetMapper.fromEntity;
import static io.meeds.gamification.twitter.storage.mapper.TwitterTweetMapper.toEntity;

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

  public List<Tweet> getTweets(int offset, int limit) {
    if (limit > 0) {
      PageRequest pageable = PageRequest.of(Math.toIntExact(offset / limit), limit, Sort.by(Sort.Direction.ASC, "id"));
      return twitterTweetDAO.findAll(pageable).getContent().stream().map(TwitterTweetMapper::fromEntity).toList();
    } else {
      return twitterTweetDAO.findAll().stream().map(TwitterTweetMapper::fromEntity).toList();
    }
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
