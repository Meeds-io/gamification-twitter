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
package io.meeds.twitter.gamification.plugin;

import io.meeds.twitter.gamification.utils.Utils;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.meeds.twitter.gamification.plugin.TwitterEventPlugin.EVENT_TYPE;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = { TwitterEventPlugin.class, })
public class TwitterEventPluginTest {

  @Test
  public void testIsValidEvent() {
    TwitterEventPlugin twitterEventPlugin = new TwitterEventPlugin();
    assertEquals(EVENT_TYPE, twitterEventPlugin.getEventType());
    assertEquals(List.of(Utils.MENTION_ACCOUNT_EVENT_NAME, Utils.LIKE_TWEET_EVENT_NAME, Utils.RETWEET_TWEET_EVENT_NAME),
                 twitterEventPlugin.getTriggers());

    Map<String, String> eventProperties = new HashMap<>();
    eventProperties.put(Utils.ACCOUNT_ID, "132452");
    assertFalse(twitterEventPlugin.isValidEvent(eventProperties,
                                                "{" + Utils.ACCOUNT_ID + ": " + 13245258 + ", " + Utils.TWEET_ID + ": " + null
                                                    + "}"));
    assertTrue(twitterEventPlugin.isValidEvent(eventProperties,
                                               "{" + Utils.ACCOUNT_ID + ": " + 132452 + ", " + Utils.TWEET_ID + ": " + null
                                                   + "}"));

    eventProperties = new HashMap<>();
    eventProperties.put(Utils.TWEET_LINK, "https://twitter.com/IoMeeds/status/1760291687425798481");
    assertFalse(twitterEventPlugin.isValidEvent(eventProperties,
                                                "{" + Utils.ACCOUNT_ID + ": " + null + ", " + Utils.TWEET_ID + ": "
                                                    + 1760291687425798482L + "}"));
    assertTrue(twitterEventPlugin.isValidEvent(eventProperties,
                                               "{" + Utils.ACCOUNT_ID + ": " + null + ", " + Utils.TWEET_ID + ": "
                                                   + 1760291687425798481L + "}"));

  }
}
