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
package io.meeds.twitter.gamification.service;

import static io.meeds.twitter.gamification.utils.Utils.CONNECTOR_NAME;
import static io.meeds.twitter.gamification.utils.Utils.MENTION_ACCOUNT_EVENT_NAME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.meeds.gamification.model.filter.RuleFilter;
import io.meeds.gamification.service.RuleService;
import io.meeds.twitter.gamification.model.RemoteTwitterAccount;
import io.meeds.twitter.gamification.model.Tweet;
import io.meeds.twitter.gamification.model.TwitterAccount;
import io.meeds.twitter.gamification.model.TwitterTrigger;
import io.meeds.twitter.gamification.service.impl.TwitterServiceImpl;
import io.meeds.twitter.gamification.storage.TwitterAccountStorage;
import io.meeds.twitter.gamification.storage.TwitterTweetStorage;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.web.security.codec.AbstractCodec;
import org.exoplatform.web.security.codec.CodecInitializer;
import org.gatein.common.util.Tools;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SpringBootTest(classes = { TwitterServiceImpl.class })
class TwitterServiceTest {

  private static final String    ADMIN_USER = "root";

  private static final String    USER       = "user";

  @MockBean
  private SettingService         settingService;

  @MockBean
  private TwitterConsumerService twitterConsumerService;

  @MockBean
  private TwitterAccountStorage  twitterAccountStorage;

  @MockBean
  private TwitterTweetStorage    twitterTweetStorage;

  @MockBean
  private RuleService            ruleService;

  @MockBean
  private CodecInitializer       codecInitializer;

  @Autowired
  private TwitterService         twitterService;

  @Test
  void testAddTwitterAccount() throws Exception {

    Throwable exception = assertThrows(IllegalAccessException.class,
                                       () -> twitterService.addTwitterAccount("twitterUsername", USER));
    assertEquals("The user is not authorized to add a twitter watched account", exception.getMessage());

    when(twitterAccountStorage.countTwitterAccounts()).thenReturn(2L);

    exception = assertThrows(IllegalStateException.class, () -> twitterService.addTwitterAccount("twitterUsername", ADMIN_USER));
    assertEquals("The maximum number of watched twitter accounts has been reached", exception.getMessage());

    RemoteTwitterAccount remoteTwitterAccount = new RemoteTwitterAccount(1, "username", "name", "description", "avatarUrl");
    when(twitterConsumerService.retrieveTwitterAccount(anyString(), anyString())).thenReturn(remoteTwitterAccount);
    List<TwitterTrigger> twitterTriggers = new ArrayList<>();
    TwitterTrigger twitterTrigger = new TwitterTrigger(MENTION_ACCOUNT_EVENT_NAME, "user1", 1254555L, "tweet", 11222121L);
    TwitterTrigger twitterTrigger1 = new TwitterTrigger(MENTION_ACCOUNT_EVENT_NAME, "user2", 12548855L, "tweet", 11222121L);
    twitterTriggers.add(twitterTrigger);
    twitterTriggers.add(twitterTrigger1);
    when(twitterConsumerService.getMentionEvents(any(), anyLong(), anyString())).thenReturn(twitterTriggers);

    // When
    when(twitterAccountStorage.countTwitterAccounts()).thenReturn(0L);

    // Then
    assertThrows(IllegalAccessException.class, () -> twitterService.getTwitterAccounts(USER, 0, 10, true));
    Assertions.assertNotNull(twitterService.getTwitterAccounts(ADMIN_USER, 0, 10, true));
    assertThrows(IllegalAccessException.class, () -> twitterService.countTwitterAccounts(USER));
    verify(twitterAccountStorage, times(1)).countTwitterAccounts();
    twitterService.getTwitterAccountById(1L);
    verify(twitterAccountStorage, times(1)).getTwitterAccountById(1L);

    assertThrows(IllegalAccessException.class, () -> twitterService.getTwitterAccountById(1L, USER));
    assertThrows(IllegalArgumentException.class, () -> twitterService.getTwitterAccountById(-10L));
    when(twitterAccountStorage.getTwitterAccountById(10L)).thenReturn(null);
    assertThrows(ObjectNotFoundException.class, () -> twitterService.getTwitterAccountById(10L, ADMIN_USER));
    TwitterAccount twitterAccount = new TwitterAccount();
    when(twitterAccountStorage.getTwitterAccountById(20L)).thenReturn(twitterAccount);
    Assertions.assertNotNull(twitterService.getTwitterAccountById(20L, ADMIN_USER));
  }

  @Test
  void testDeleteTwitterAccount() throws Exception {

    Throwable exception = assertThrows(IllegalAccessException.class, () -> twitterService.deleteTwitterAccount(1L, USER));
    assertEquals("The user is not authorized to delete Twitter account", exception.getMessage());

    when(twitterAccountStorage.getTwitterAccountById(1L)).thenReturn(null);

    exception = assertThrows(ObjectNotFoundException.class, () -> twitterService.deleteTwitterAccount(1L, ADMIN_USER));
    assertEquals("Twitter account with remote id : 1 wasn't found", exception.getMessage());

    when(twitterAccountStorage.getTwitterAccountById(2L)).thenReturn(new TwitterAccount());

    // When
    twitterService.deleteTwitterAccount(2L, ADMIN_USER);

    // Then
    verify(twitterAccountStorage, times(1)).deleteTwitterAccount(2L);
    verify(twitterConsumerService, times(1)).clearCache();
    RuleFilter ruleFilter = new RuleFilter(true);
    ruleFilter.setEventType(CONNECTOR_NAME);
    ruleFilter.setIncludeDeleted(true);
    verify(ruleService, times(1)).getRules(ruleFilter, 0, -1);
  }

  @Test
  void testSaveTwitterBearerToken() throws Exception {

    Throwable exception = assertThrows(IllegalAccessException.class,
                                       () -> twitterService.saveTwitterBearerToken("bearerToken", USER));
    assertEquals("The user is not authorized to save or update Twitter Bearer Token", exception.getMessage());

    // When
    AbstractCodec abstractCodec = mock(AbstractCodec.class);
    when(codecInitializer.getCodec()).thenReturn(abstractCodec);
    twitterService.saveTwitterBearerToken("bearerToken", ADMIN_USER);

    // Then
    verify(abstractCodec, times(1)).encode("bearerToken");
    verify(settingService, times(1)).set(any(), any(), any(), any());
  }

  @Test
  void testAddTweetToWatch() {
    Set<String> tweetLikers = Tools.toSet("user1", "user2", "user3");
    Set<String> tweetRetweeters = Tools.toSet("user1", "user2");

    Tweet tweet = new Tweet();
    when(twitterTweetStorage.getTweetByLink("existTweetLink")).thenReturn(tweet);
    assertNull(twitterService.addTweetToWatch("existTweetLink"));

    when(twitterTweetStorage.getTweetByLink("tweetLink")).thenReturn(null);
    when(twitterConsumerService.retrieveTweetLikers("tweetLink", "bearerToken")).thenReturn(tweetLikers);
    when(twitterConsumerService.retrieveTweetRetweeters("tweetLink", "bearerToken")).thenReturn(tweetRetweeters);

    // When
    twitterService.addTweetToWatch("tweetLink");

    // Then
    verify(twitterTweetStorage, times(1)).addTweetToWatch(any());

  }
}
