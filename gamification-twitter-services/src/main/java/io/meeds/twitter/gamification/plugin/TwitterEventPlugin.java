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

import io.meeds.gamification.plugin.EventPlugin;
import io.meeds.gamification.service.EventService;
import io.meeds.twitter.gamification.utils.Utils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class TwitterEventPlugin extends EventPlugin {

  public static final String EVENT_TYPE = "twitter";

  @Autowired
  private EventService       eventService;

  @PostConstruct
  public void init() {
    eventService.addPlugin(this);
  }

  @Override
  public String getEventType() {
    return EVENT_TYPE;
  }

  public List<String> getTriggers() {
    return List.of(Utils.MENTION_ACCOUNT_EVENT_NAME, Utils.LIKE_TWEET_EVENT_NAME, Utils.RETWEET_TWEET_EVENT_NAME);
  }

  @Override
  public boolean isValidEvent(Map<String, String> eventProperties, String triggerDetails) {
    String desiredAccountId = eventProperties.get(Utils.ACCOUNT_ID);
    String desiredTweetLink = eventProperties.get(Utils.TWEET_LINK);
    String desiredTweetId = desiredTweetLink != null ? Utils.extractTweetId(desiredTweetLink) : null;
    Map<String, String> triggerDetailsMop = Utils.stringToMap(triggerDetails);
    return (desiredAccountId != null && desiredAccountId.equals(triggerDetailsMop.get(Utils.ACCOUNT_ID)))
        || (desiredTweetId != null && desiredTweetId.equals(triggerDetailsMop.get(Utils.TWEET_ID)));
  }
}
