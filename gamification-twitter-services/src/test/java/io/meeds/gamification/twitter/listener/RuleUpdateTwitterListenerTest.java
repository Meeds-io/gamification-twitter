/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.gamification.twitter.listener;

import static io.meeds.gamification.twitter.utils.Utils.TWEET_LINK;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.meeds.gamification.model.EventDTO;
import io.meeds.gamification.model.RuleDTO;
import io.meeds.gamification.model.filter.RuleFilter;
import io.meeds.gamification.service.RuleService;
import io.meeds.gamification.twitter.model.Tweet;
import io.meeds.gamification.twitter.service.TwitterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.ListenerService;

@SpringBootTest(classes = { RuleUpdateTwitterListener.class, })
class RuleUpdateTwitterListenerTest {

  @MockBean
  private TwitterService twitterAccountService;

  @MockBean
  private RuleService ruleService;

  @MockBean
  private ListenerService       listenerService;

  @MockBean
  private RuleDTO                     rule;

  @MockBean
  private Tweet tweet;

  @MockBean
  private Event<RuleDTO, String>      event;

  @Autowired
  private RuleUpdateTwitterListener ruleUpdateTwitterListener;

  @Test
  void createEvent() {
    EventDTO eventDTO = new EventDTO();
    Map<String, String> properties = new HashMap<>();
    properties.put(TWEET_LINK, "tweetLink");
    eventDTO.setProperties(properties);
    when(rule.getEvent()).thenReturn(eventDTO);
    when(ruleService.getRules(any(RuleFilter.class), anyInt(), anyInt())).thenReturn(List.of(rule));
    when(twitterAccountService.getTweets(0, -1)).thenReturn(List.of(tweet));
    when(twitterAccountService.getTwitterBearerToken()).thenReturn("bearerToken");
    ruleUpdateTwitterListener.onEvent(event);
    verify(twitterAccountService, times(1)).addTweetToWatch("tweetLink");
  }
}
