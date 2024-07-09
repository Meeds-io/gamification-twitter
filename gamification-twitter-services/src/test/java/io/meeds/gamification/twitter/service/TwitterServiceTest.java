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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package io.meeds.gamification.twitter.service;

import static io.meeds.gamification.twitter.utils.Utils.MENTION_ACCOUNT_EVENT_NAME;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import io.meeds.gamification.twitter.BaseTwitterTest;
import io.meeds.gamification.twitter.model.RemoteTwitterAccount;
import io.meeds.gamification.twitter.model.Tweet;
import io.meeds.gamification.twitter.model.TwitterAccount;
import io.meeds.gamification.twitter.model.TwitterTrigger;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.gatein.common.util.Tools;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TwitterServiceTest extends BaseTwitterTest {

  private static final String ADMIN_USER = "root1";

  private static final String USER       = "root";

  @Override
  public void setUp() throws Exception {
    super.setUp();
    registerAdministratorUser(ADMIN_USER);
    registerInternalUser(USER);
  }

  @Test
  public void testAddTwitterAccount() throws Exception {
    assertThrows(IllegalAccessException.class, () -> twitterService.addTwitterAccount("twitterUsername", "root"));

    twitterService.saveTwitterBearerToken("bearerToken", "root1");
    RemoteTwitterAccount remoteTwitterAccount = new RemoteTwitterAccount(1, "username", "name", "description", "avatarUrl");
    RemoteTwitterAccount remoteTwitterAccount1 = new RemoteTwitterAccount(2, "username1", "name1", "description1", "avatarUrl1");
    when(twitterConsumerService.retrieveTwitterAccount("twitterUsername", "bearerToken")).thenReturn(remoteTwitterAccount);
    when(twitterConsumerService.retrieveTwitterAccount("twitterUsername1", "bearerToken")).thenReturn(remoteTwitterAccount1);
    List<TwitterTrigger> twitterTriggers = new ArrayList<>();
    TwitterTrigger twitterTrigger = new TwitterTrigger(MENTION_ACCOUNT_EVENT_NAME, "user1", 1254555L, "tweet", 11222121L);
    TwitterTrigger twitterTrigger1 = new TwitterTrigger(MENTION_ACCOUNT_EVENT_NAME, "user2", 12548855L, "tweet", 11222121L);
    twitterTriggers.add(twitterTrigger);
    twitterTriggers.add(twitterTrigger1);
    when(twitterConsumerService.getMentionEvents(any(), anyLong(), anyString())).thenReturn(twitterTriggers);

    // When
    TwitterAccount twitterAccount = twitterService.addTwitterAccount("twitterUsername", "root1");

    // Then
    assertThrows(IllegalAccessException.class, () -> twitterService.getTwitterAccounts("root", 0, 10, true));
    assertNotNull(twitterService.getTwitterAccounts("root1", 0, 10, true));
    assertThrows(IllegalAccessException.class, () -> twitterService.countTwitterAccounts("root"));
    assertEquals(1, twitterService.countTwitterAccounts("root1"));
    assertEquals(1254555L, twitterService.getTwitterAccountById(twitterAccount.getId()).getLastMentionTweetId());

    assertThrows(IllegalAccessException.class, () -> twitterService.getTwitterAccountById(twitterAccount.getId(), "root"));
    assertThrows(IllegalArgumentException.class, () -> twitterService.getTwitterAccountById(-10L));
    assertThrows(ObjectNotFoundException.class, () -> twitterService.getTwitterAccountById(10L, "root1"));
    assertNotNull(twitterService.getTwitterAccountById(twitterAccount.getId(), "root1"));

    // When
    TwitterAccount twitterAccount1 = twitterService.addTwitterAccount("twitterUsername1", "root1");

    // Then
    Throwable exception1 = assertThrows(IllegalStateException.class,
                                        () -> twitterService.addTwitterAccount("twitterUsername2", "root1"));
    assertEquals("The maximum number of watched twitter accounts has been reached", exception1.getMessage());

    // When
    twitterService.deleteTwitterAccount(twitterAccount1.getId(), "root1");

    // Then
    assertThrows(ObjectAlreadyExistsException.class, () -> twitterService.addTwitterAccount("twitterUsername", "root1"));

    // When
    twitterService.updateAccountLastMentionTweetId(twitterAccount.getId(), 111122222L);

    // Then
    assertEquals(111122222L, twitterService.getTwitterAccountById(twitterAccount.getId()).getLastMentionTweetId());
    assertThrows(IllegalArgumentException.class, () -> twitterService.updateAccountLastMentionTweetId(-10L, 111122222L));
    assertThrows(ObjectNotFoundException.class, () -> twitterService.updateAccountLastMentionTweetId(10L, 111122222L));
  }

  @Test
  public void testDeleteTwitterAccount() throws Exception {
    twitterService.saveTwitterBearerToken("bearerToken", "root1");
    RemoteTwitterAccount remoteTwitterAccount = new RemoteTwitterAccount(1, "username", "name", "description", "avatarUrl");
    when(twitterConsumerService.retrieveTwitterAccount("twitterUsername", "bearerToken")).thenReturn(remoteTwitterAccount);

    // When
    TwitterAccount twitterAccount = twitterService.addTwitterAccount("twitterUsername", "root1");

    // Then
    assertThrows(IllegalAccessException.class, () -> twitterService.deleteTwitterAccount(twitterAccount.getId(), "root"));
    assertThrows(ObjectNotFoundException.class, () -> twitterService.deleteTwitterAccount(10L, "root1"));

    // When
    twitterService.deleteTwitterAccount(twitterAccount.getId(), "root1");

    // Then
    assertNull(twitterService.getTwitterAccountById(twitterAccount.getId()));
  }

  @Test
  public void testSaveTwitterBearerToken() throws IllegalAccessException {
    // When
    assertThrows(IllegalAccessException.class, () -> twitterService.saveTwitterBearerToken("bearerToken", "root"));
    // When
    twitterService.saveTwitterBearerToken("bearerToken", "root1");
    // Then
    assertThrows(IllegalAccessException.class, () -> twitterService.getTwitterBearerToken("root"));

    assertNotNull(twitterService.getTwitterBearerToken("root1"));
    // When
    assertThrows(IllegalAccessException.class, () -> twitterService.deleteTwitterBearerToken("root"));
    // When
    twitterService.deleteTwitterBearerToken("root1");
    // Then
    assertNull(twitterService.getTwitterBearerToken());
  }

  @Test
  public void testAddTweetToWatch() throws Exception {
    Set<String> tweetLikers = Tools.toSet("user1", "user2", "user3");
    Set<String> tweetRetweeters = Tools.toSet("user1", "user2");

    twitterService.saveTwitterBearerToken("bearerToken", "root1");
    when(twitterConsumerService.retrieveTweetLikers("tweetLink", "bearerToken")).thenReturn(tweetLikers);
    when(twitterConsumerService.retrieveTweetRetweeters("tweetLink", "bearerToken")).thenReturn(tweetRetweeters);

    // When
    Tweet tweet = twitterService.addTweetToWatch("tweetLink");

    // Then
    assertEquals(1, twitterService.getTweets(0, 10).size());
    assertThrows(IllegalArgumentException.class, () -> twitterService.getTweetByLink(null));
    assertNotNull(twitterService.getTweetByLink("tweetLink"));

    // When
    twitterService.addTweetToWatch("tweetLink");

    // Then
    assertEquals(1, twitterService.getTweets(0, 10).size());

    // When
    twitterService.deleteTweet(tweet.getTweetId());

    // Then
    assertEquals(0, twitterService.getTweets(0, 10).size());
    assertThrows(ObjectNotFoundException.class, () -> twitterService.deleteTweet(-10L));

    // When
    tweet = twitterService.addTweetToWatch("tweetLink");
    tweetLikers = Tools.toSet("user1", "user2", "user3", "user4");
    tweetRetweeters = Tools.toSet("user1", "user2", "user3");
    twitterService.updateTweetReactions(tweet.getTweetId(), tweetLikers, tweetRetweeters);

    // Then
    assertEquals(tweetLikers, twitterService.getTweetByLink(tweet.getTweetLink()).getLikers());
    assertEquals(tweetRetweeters, twitterService.getTweetByLink(tweet.getTweetLink()).getRetweeters());
    assertThrows(IllegalArgumentException.class, () -> twitterService.updateTweetReactions(-10, null, null));
    assertThrows(ObjectNotFoundException.class, () -> twitterService.updateTweetReactions(10L, null, null));

  }
}
