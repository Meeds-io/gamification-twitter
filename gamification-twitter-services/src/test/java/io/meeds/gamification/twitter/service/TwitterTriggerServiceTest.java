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
 *
 */
package io.meeds.gamification.twitter.service;

import io.meeds.gamification.model.EventDTO;
import io.meeds.gamification.twitter.BaseTwitterTest;
import io.meeds.gamification.twitter.model.TwitterTrigger;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.meeds.gamification.twitter.service.impl.TwitterTriggerServiceImpl.GAMIFICATION_GENERIC_EVENT;
import static io.meeds.gamification.twitter.utils.Utils.*;
import static org.mockito.Mockito.*;

public class TwitterTriggerServiceTest extends BaseTwitterTest {

  private static final String ADMIN_USER = "root1";

  private static final String USER       = "root";

  @Override
  public void setUp() throws Exception {
    super.setUp();
    registerAdministratorUser(ADMIN_USER);
    registerInternalUser(USER);
  }

  @Test
  public void testHandleTriggerAsync() throws Exception {

    TwitterTrigger twitterTrigger = new TwitterTrigger();
    twitterTrigger.setType("tweet");
    twitterTrigger.setTrigger("likeTweet");
    twitterTrigger.setTwitterUsername("liker");
    twitterTrigger.setTweetId(11112222L);
    when(triggerService.isTriggerEnabledForAccount(twitterTrigger.getTrigger(), twitterTrigger.getAccountId())).thenReturn(true);
    when(connectorService.getAssociatedUsername(CONNECTOR_NAME, twitterTrigger.getTwitterUsername())).thenReturn("root1");
    List<EventDTO> events = new ArrayList<>();
    EventDTO eventDTO = new EventDTO(1, "likeTweet", "twitter", "likeTweet", null, null);
    EventDTO eventDTO1 = new EventDTO(2, "likeTweet", "twitter", "likeTweet", null, null);
    events.add(eventDTO);
    events.add(eventDTO1);
    when(eventService.getEventsByTitle(twitterTrigger.getTrigger(), 0, -1)).thenReturn(events);
    twitterTriggerService.handleTriggerAsyncInternal(twitterTrigger);
    String eventDetails = "{" + ACCOUNT_ID + ": " + twitterTrigger.getAccountId() + ", " + TWEET_ID + ": "
        + twitterTrigger.getTweetId() + "}";

    Map<String, String> gam = new HashMap<>();
    gam.put("senderId", "root1");
    gam.put("receiverId", "root1");
    gam.put("objectId", "11112222");
    gam.put("objectType", "tweet");
    gam.put("ruleTitle", "likeTweet");
    gam.put("eventDetails", eventDetails);
    verify(listenerService, times(1)).broadcast(GAMIFICATION_GENERIC_EVENT, gam, "");
  }
}
