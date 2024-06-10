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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package io.meeds.twitter.gamification.listener;

import io.meeds.gamification.constant.DateFilterType;
import io.meeds.gamification.constant.EntityStatusType;
import io.meeds.gamification.model.filter.RuleFilter;
import io.meeds.gamification.service.RuleService;
import io.meeds.twitter.gamification.model.Tweet;
import io.meeds.twitter.gamification.service.TwitterService;
import io.meeds.twitter.gamification.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;

import io.meeds.gamification.model.RuleDTO;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class RuleUpdateTwitterListener extends Listener<RuleDTO, String> {

  private static final Log      LOG             = ExoLogger.getLogger(RuleUpdateTwitterListener.class);

  private static final String[] LISTENER_EVENTS = { "rule.created", "rule.deleted", "rule.updated" };

  @Autowired
  private TwitterService        twitterAccountService;

  @Autowired
  private RuleService           ruleService;

  @Autowired
  private ListenerService       listenerService;

  @PostConstruct
  public void init() {
    for (String eventName : LISTENER_EVENTS) {
      listenerService.addListener(eventName, this);
    }
  }

  @Override
  @ExoTransactional
  public void onEvent(Event<RuleDTO, String> event) {
    RuleFilter ruleFilter = new RuleFilter();
    ruleFilter.setEventType(Utils.CONNECTOR_NAME);
    ruleFilter.setStatus(EntityStatusType.ENABLED);
    ruleFilter.setDateFilterType(DateFilterType.ACTIVE);
    ruleFilter.setAllSpaces(true);
    List<RuleDTO> rules = ruleService.getRules(ruleFilter, 0, -1);

    List<String> watchedTweets = rules.stream()
                                      .filter(r -> !r.getEvent().getProperties().isEmpty()
                                          && StringUtils.isNotBlank(r.getEvent().getProperties().get(Utils.TWEET_LINK)))
                                      .map(r -> r.getEvent().getProperties().get(Utils.TWEET_LINK))
                                      .toList();

    List<Tweet> tweets = twitterAccountService.getTweets();

    String bearerToken = twitterAccountService.getTwitterBearerToken();
    watchedTweets.forEach(watchedTweet -> {
      Tweet tweet = twitterAccountService.getTweetByLink(watchedTweet);
      if (tweet == null) {
        if (StringUtils.isNotBlank(bearerToken)) {
          twitterAccountService.addTweetToWatch(watchedTweet);
        } else {
          LOG.warn("The Tweet {} could not be viewed, because the Twitter bearer token was not configured",
                   Utils.extractTweetId(watchedTweet));
        }
      }
    });

    tweets.stream()
          .filter(tweet -> !watchedTweets.contains(tweet.getTweetLink()))
          .map(Tweet::getTweetId)
          .forEach(twitterAccountService::deleteTweetById);
  }
}
