/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Lab contact@meedslab.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.twitter.gamification.scheduling.task;

import io.meeds.twitter.gamification.model.Tweet;
import io.meeds.twitter.gamification.model.TwitterAccount;
import io.meeds.twitter.gamification.model.TwitterTrigger;
import io.meeds.twitter.gamification.service.TwitterConsumerService;
import io.meeds.twitter.gamification.service.TwitterService;
import io.meeds.twitter.gamification.service.TwitterTriggerService;
import io.meeds.twitter.gamification.utils.Utils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * A service that will manage the periodic updating of twitter events.
 */
@Component
public class TwitterRemoteUpdateTask {

  private static final Log       LOG = ExoLogger.getLogger(TwitterRemoteUpdateTask.class);

  @Autowired
  private TwitterConsumerService twitterConsumerService;

  @Autowired
  private TwitterService         twitterAccountService;

  @Autowired
  private TwitterTriggerService  twitterTriggerService;

  @ExoTransactional
  @Scheduled(cron = "${io.meeds.gamification.TwitterAccountRemoteUpdate.expression:0 */15 * * * ?}")
  public void execute() {
    String bearerToken = twitterAccountService.getTwitterBearerToken();
    if (StringUtils.isBlank(bearerToken)) {
      return;
    }
    List<TwitterAccount> twitterAccounts = twitterAccountService.getTwitterAccounts();
    if (!twitterAccounts.isEmpty()) {
      twitterAccounts.forEach(twitterAccount -> processTwitterAccount(twitterAccount, bearerToken));
    }
    List<Tweet> tweets = twitterAccountService.getTweets();
    if (!tweets.isEmpty()) {
      tweets.forEach(tweet -> processTweetReactionsUpdate(tweet, bearerToken));
    }
  }

  private void processTwitterAccount(TwitterAccount twitterAccount, String bearerToken) {
    try {
      List<TwitterTrigger> mentionTriggers = twitterConsumerService.getMentionEvents(twitterAccount,
                                                                                     twitterAccount.getLastMentionTweetId(),
                                                                                     bearerToken);

      if (CollectionUtils.isNotEmpty(mentionTriggers)) {
        processMentionTriggers(mentionTriggers, twitterAccount);
      }
    } catch (ObjectNotFoundException e) {
      LOG.warn("Error while updating twitter account {}", twitterAccount.getId(), e);
    }
  }

  private void processMentionTriggers(List<TwitterTrigger> mentionTriggers,
                                      TwitterAccount twitterAccount) throws ObjectNotFoundException {
    for (TwitterTrigger trigger : mentionTriggers) {
      twitterTriggerService.handleTriggerAsync(trigger);
    }
    twitterAccountService.updateAccountLastMentionTweetId(twitterAccount.getId(), mentionTriggers.get(0).getTweetId());
  }

  private void processTweetReactionsUpdate(Tweet tweet, String bearerToken) {
    Set<String> tweetLikers = twitterConsumerService.retrieveTweetLikers(tweet.getTweetLink(), bearerToken);
    Set<String> tweetRetweeters = twitterConsumerService.retrieveTweetRetweeters(tweet.getTweetLink(), bearerToken);
    if (!CollectionUtils.isEqualCollection(tweetLikers, tweet.getLikers())) {
      tweetLikers.stream().filter(liker -> !tweet.getLikers().contains(liker)).forEach(liker -> {
        TwitterTrigger twitterTrigger = new TwitterTrigger();
        twitterTrigger.setType("tweet");
        twitterTrigger.setTrigger("likeTweet");
        twitterTrigger.setTwitterUsername(liker);
        String tweetId = Utils.extractTweetId(tweet.getTweetLink());
        if (StringUtils.isNotBlank(tweetId)) {
          twitterTrigger.setTweetId(Long.parseLong(tweetId));
        }
        twitterTriggerService.handleTriggerAsync(twitterTrigger);
      });
    }
    if (!CollectionUtils.isEqualCollection(tweetRetweeters, tweet.getRetweeters())) {
      tweetRetweeters.stream().filter(retweeter -> !tweet.getRetweeters().contains(retweeter)).forEach(retweeter -> {
        TwitterTrigger twitterTrigger = new TwitterTrigger();
        twitterTrigger.setType("tweet");
        twitterTrigger.setTrigger("retweet");
        twitterTrigger.setTwitterUsername(retweeter);
        String tweetId = Utils.extractTweetId(tweet.getTweetLink());
        if (StringUtils.isNotBlank(tweetId)) {
          twitterTrigger.setTweetId(Long.parseLong(tweetId));
        }
        twitterTriggerService.handleTriggerAsync(twitterTrigger);
      });
    }
    if (!CollectionUtils.isEqualCollection(tweetLikers, tweet.getLikers())
        || !CollectionUtils.isEqualCollection(tweetRetweeters, tweet.getRetweeters())) {
      try {
        twitterAccountService.updateTweetReactions(tweet.getTweetId(), tweetLikers, tweetRetweeters);
      } catch (ObjectNotFoundException e) {
        LOG.warn("Error while updating tweet reactions {}", tweet.getTweetId(), e);
      }
    }
  }
}
