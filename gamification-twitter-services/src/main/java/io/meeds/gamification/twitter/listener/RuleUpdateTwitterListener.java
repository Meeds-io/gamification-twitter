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
package io.meeds.gamification.twitter.listener;

import io.meeds.gamification.model.filter.RuleFilter;
import io.meeds.gamification.service.RuleService;
import io.meeds.gamification.twitter.model.Tweet;
import io.meeds.gamification.twitter.service.TwitterService;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;

import io.meeds.gamification.model.RuleDTO;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.List;

import static io.meeds.gamification.twitter.utils.Utils.*;

public class RuleUpdateTwitterListener extends Listener<RuleDTO, String> {

  private static final Log            LOG = ExoLogger.getLogger(RuleUpdateTwitterListener.class);

  private final TwitterService twitterAccountService;

  private final RuleService           ruleService;

  public RuleUpdateTwitterListener(TwitterService twitterAccountService, RuleService ruleService) {
    this.twitterAccountService = twitterAccountService;
    this.ruleService = ruleService;
  }

  @Override
  @ExoTransactional
  public void onEvent(Event<RuleDTO, String> event) {
    RuleFilter ruleFilter = new RuleFilter();
    ruleFilter.setEventType(CONNECTOR_NAME);
    List<RuleDTO> rules = ruleService.getRules(ruleFilter, 0, -1);

    List<String> watchedTweets = rules.stream()
                                      .filter(r -> !r.getEvent().getProperties().isEmpty()
                                          && StringUtils.isNotBlank(r.getEvent().getProperties().get(TWEET_LINK)))
                                      .map(r -> r.getEvent().getProperties().get(TWEET_LINK))
                                      .toList();

    List<Tweet> tweets = twitterAccountService.getTweets(0, -1);

    String bearerToken = twitterAccountService.getTwitterBearerToken();
    watchedTweets.forEach(watchedTweet -> {
      Tweet tweet = twitterAccountService.getTweetByLink(watchedTweet);
      if (tweet == null) {
        if (StringUtils.isNotBlank(bearerToken)) {
          twitterAccountService.addTweetToWatch(watchedTweet);
        } else {
          LOG.warn("The Tweet {} could not be viewed, because the Twitter bearer token was not configured", extractTweetId(watchedTweet));
        }
      }
    });

    tweets.stream()
          .filter(tweet -> !watchedTweets.contains(tweet.getTweetLink()))
          .map(Tweet::getTweetId)
          .forEach(twitterAccountService::deleteTweetById);
  }
}
