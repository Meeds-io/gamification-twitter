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
 *
 */
package io.meeds.twitter.gamification.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.meeds.twitter.gamification.dao.TwitterTweetDAO;
import io.meeds.twitter.gamification.entity.TwitterTweetEntity;
import io.meeds.twitter.gamification.model.Tweet;
import org.gatein.common.util.Tools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@SpringBootTest(classes = { TwitterTweetStorage.class, })
@ExtendWith(MockitoExtension.class)
class TwitterTweetStorageTest {

  private static final Long     ID         = 2L;

  private static final String   TWEET_LINK = "tweetLink";

  private static final Pageable PAGEABLE   = Pageable.ofSize(2);

  @Autowired
  private TwitterTweetStorage   twitterTweetStorage;

  @MockBean
  private TwitterTweetDAO       twitterTweetDAO;

  @BeforeEach
  void setup() {
    when(twitterTweetDAO.save(any())).thenAnswer(invocation -> {
      TwitterTweetEntity entity = invocation.getArgument(0);
      if (entity.getId() == null) {
        entity.setId(ID);
      }
      when(twitterTweetDAO.findById(ID)).thenReturn(Optional.of(entity));
      when(twitterTweetDAO.findTwitterTweetEntityByTweetLink(TWEET_LINK)).thenReturn(entity);
      when(twitterTweetDAO.findAll(PAGEABLE)).thenReturn(new PageImpl<>(List.of(entity)));
      when(twitterTweetDAO.count()).thenReturn(1L);
      return entity;
    });
    doAnswer(invocation -> {
      TwitterTweetEntity entity = invocation.getArgument(0);
      when(twitterTweetDAO.findById(entity.getId())).thenReturn(Optional.empty());
      return null;
    }).when(twitterTweetDAO).delete(any());
  }

  @Test
  void testAddTweetToWatch() {
    // Given
    Tweet tweet = createTwitterTweetInstance();

    // When
    Tweet createdTweet = twitterTweetStorage.addTweetToWatch(tweet);

    // Then
    assertNotNull(createdTweet);
    assertEquals(tweet.getTweetLink(), createdTweet.getTweetLink());

    createdTweet = twitterTweetStorage.addTweetToWatch(tweet);
    assertNull(createdTweet);

  }

  @Test
  void testGetTweetById() {
    // Given
    Tweet createdTweet = twitterTweetStorage.addTweetToWatch(createTwitterTweetInstance());

    // When
    Tweet tweet = twitterTweetStorage.getTweetById(createdTweet.getTweetId());

    // Then
    assertNotNull(tweet);
    assertEquals(createdTweet.getTweetLink(), tweet.getTweetLink());
  }

  @Test
  void testGetTweets() {
    // Given
    Tweet tweet = createTwitterTweetInstance();

    // When
    Tweet createdTweet = twitterTweetStorage.addTweetToWatch(tweet);

    // Then
    assertNotNull(createdTweet);
    assertEquals(new PageImpl<>(List.of(createdTweet)), twitterTweetStorage.getTweets(PAGEABLE));
    assertEquals(1L, twitterTweetStorage.countTweets());
  }

  @Test
  void testTweetReactions() {
    // Given
    Tweet TwitterTweet = createTwitterTweetInstance();

    // When
    Set<String> tweetLikers = Tools.toSet("user1", "user2", "user3");
    Set<String> tweetRetweeters = Tools.toSet("user1", "user2");
    Tweet tweet = twitterTweetStorage.updateTweetReactions(10L, tweetLikers, tweetRetweeters);

    // Then
    assertNull(tweet);

    // When
    Tweet createdTweet = twitterTweetStorage.addTweetToWatch(TwitterTweet);
    tweet = twitterTweetStorage.updateTweetReactions(createdTweet.getTweetId(), tweetLikers, tweetRetweeters);

    // Then
    assertNotNull(tweet);
    assertEquals(createdTweet.getTweetLink(), tweet.getTweetLink());
    assertEquals(tweetLikers, tweet.getLikers());
    assertEquals(tweetRetweeters, tweet.getRetweeters());
  }

  @Test
  void testDeleteWebHook() {
    // Given
    Tweet createdTweet = twitterTweetStorage.addTweetToWatch(createTwitterTweetInstance());

    // When
    Tweet tweet = twitterTweetStorage.deleteTweet(createdTweet.getTweetId());

    // Then
    assertNotNull(tweet);
  }

  @Test
  void testGetTwitterAccountByRemoteId() {
    // Given
    Tweet createdTweet = twitterTweetStorage.addTweetToWatch(createTwitterTweetInstance());

    // When
    Tweet tweet = twitterTweetStorage.getTweetByLink(createdTweet.getTweetLink());

    // Then
    assertNotNull(tweet);
    assertEquals(createdTweet.getTweetLink(), tweet.getTweetLink());
  }

  protected Tweet createTwitterTweetInstance() {
    Tweet tweet = new Tweet();
    tweet.setTweetLink(TWEET_LINK);
    return tweet;
  }
}
