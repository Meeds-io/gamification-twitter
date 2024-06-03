/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.twitter.gamification.service.impl;

import io.meeds.gamification.model.RuleDTO;
import io.meeds.gamification.model.filter.RuleFilter;
import io.meeds.gamification.service.RuleService;
import io.meeds.twitter.gamification.model.RemoteTwitterAccount;
import io.meeds.twitter.gamification.model.Tweet;
import io.meeds.twitter.gamification.model.TwitterAccount;
import io.meeds.twitter.gamification.model.TwitterTrigger;
import io.meeds.twitter.gamification.service.TwitterConsumerService;
import io.meeds.twitter.gamification.service.TwitterService;
import io.meeds.twitter.gamification.storage.TwitterAccountStorage;
import io.meeds.twitter.gamification.storage.TwitterTweetStorage;
import io.meeds.gamification.utils.Utils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.security.codec.CodecInitializer;
import org.exoplatform.web.security.security.TokenServiceInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static io.meeds.twitter.gamification.utils.Utils.ACCOUNT_ID;
import static io.meeds.twitter.gamification.utils.Utils.CONNECTOR_NAME;

@Primary
@Service
public class TwitterServiceImpl implements TwitterService {

  private static final Log       LOG                     = ExoLogger.getLogger(TwitterServiceImpl.class);

  private static final Scope     TWITTER_CONNECTOR_SCOPE = Scope.APPLICATION.id("twitterConnector");

  private static final String    BEARER_TOKEN_KEY        = "BEARER_TOKEN";

  public static final String     NOT_FOUND               = " wasn't found";

  @Autowired
  private SettingService         settingService;

  @Autowired
  private TwitterConsumerService twitterConsumerService;

  @Autowired
  private TwitterAccountStorage  twitterAccountStorage;

  @Autowired
  private TwitterTweetStorage    twitterTweetStorage;

  @Autowired
  private RuleService            ruleService;

  @Autowired
  private CodecInitializer       codecInitializer;

  @Override
  public List<TwitterAccount> getTwitterAccounts(String currentUser,
                                                 int offset,
                                                 int limit,
                                                 boolean forceUpdate) throws IllegalAccessException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException("The user is not authorized to access Twitter watched accounts");
    }
    if (forceUpdate) {
      twitterConsumerService.clearCache();
    }
    return getTwitterAccounts(offset, limit);
  }

  @Override
  public TwitterAccount getTwitterAccountById(long accountId) {
    if (accountId <= 0) {
      throw new IllegalArgumentException("Twitter account id is mandatory");
    }
    return twitterAccountStorage.getTwitterAccountById(accountId);
  }

  @Override
  public TwitterAccount getTwitterAccountById(long accountId, String username) throws IllegalAccessException,
                                                                               ObjectNotFoundException {
    if (!Utils.isRewardingManager(username)) {
      throw new IllegalAccessException("The user is not authorized to access Twitter watched account");
    }
    TwitterAccount twitterAccount = getTwitterAccountById(accountId);
    if (twitterAccount == null) {
      throw new ObjectNotFoundException("Twitter account doesn't exist");
    }
    return twitterAccount;
  }

  @Override
  public List<TwitterAccount> getTwitterAccounts(int offset, int limit) {
    return twitterAccountStorage.getTwitterAccounts(offset, limit);
  }

  @Override
  public long countTwitterAccounts(String currentUser) throws IllegalAccessException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException("The user is not authorized to access Twitter watched accounts");
    }
    return twitterAccountStorage.countTwitterAccounts();
  }

  @Override
  public TwitterAccount addTwitterAccount(String twitterUsername, String currentUser) throws ObjectAlreadyExistsException,
                                                                                      IllegalAccessException,
                                                                                      ObjectNotFoundException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException("The user is not authorized to add a twitter watched account");
    }
    if (twitterAccountStorage.countTwitterAccounts() >= 2) {
      throw new IllegalStateException("The maximum number of watched twitter accounts has been reached");
    }
    RemoteTwitterAccount remoteTwitterAccount = twitterConsumerService.retrieveTwitterAccount(twitterUsername,
                                                                                              getTwitterBearerToken(currentUser));
    if (remoteTwitterAccount != null) {
      TwitterAccount existsAccount = twitterAccountStorage.getTwitterAccountByRemoteId(remoteTwitterAccount.getId());
      if (existsAccount != null) {
        throw new ObjectAlreadyExistsException(existsAccount);
      }
      TwitterAccount twitterAccount = new TwitterAccount();
      twitterAccount.setIdentifier(remoteTwitterAccount.getUsername());
      twitterAccount.setRemoteId(remoteTwitterAccount.getId());
      twitterAccount.setName(remoteTwitterAccount.getName());
      twitterAccount.setIdentifier(remoteTwitterAccount.getUsername());
      twitterAccount.setWatchedBy(currentUser);
      List<TwitterTrigger> mentionTriggers = twitterConsumerService.getMentionEvents(twitterAccount, 0L, getTwitterBearerToken());
      if (CollectionUtils.isNotEmpty(mentionTriggers)) {
        twitterAccount.setLastMentionTweetId(mentionTriggers.get(0).getTweetId());
      }
      return twitterAccountStorage.addTwitterAccount(twitterAccount);
    }
    return null;
  }

  @Override
  public void deleteTwitterAccount(long twitterAccountId, String currentUser) throws IllegalAccessException,
                                                                              ObjectNotFoundException {

    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException("The user is not authorized to delete Twitter account");
    }
    TwitterAccount twitterAccount = twitterAccountStorage.getTwitterAccountById(twitterAccountId);
    if (twitterAccount == null) {
      throw new ObjectNotFoundException("Twitter account with remote id : " + twitterAccountId + NOT_FOUND);
    }
    twitterAccountStorage.deleteTwitterAccount(twitterAccountId);
    twitterConsumerService.clearCache();
    RuleFilter ruleFilter = new RuleFilter(true);
    ruleFilter.setEventType(CONNECTOR_NAME);
    ruleFilter.setIncludeDeleted(true);
    List<RuleDTO> rules = ruleService.getRules(ruleFilter, 0, -1);
    rules.stream()
         .filter(r -> !r.getEvent().getProperties().isEmpty() && r.getEvent().getProperties().get(ACCOUNT_ID) != null
             && r.getEvent().getProperties().get(ACCOUNT_ID).equals(String.valueOf(twitterAccount.getRemoteId())))
         .forEach(rule -> {
           try {
             rule.setEnabled(false);
             ruleService.updateRule(rule);
           } catch (ObjectNotFoundException e) {
             LOG.warn("Error while automatically switching rule status. Rule = {} ", rule, e);
           }
         });
  }

  @Override
  public Tweet addTweetToWatch(String tweetLink) {
    Tweet existsTweet = twitterTweetStorage.getTweetByLink(tweetLink);
    if (existsTweet != null) {
      return null;
    }
    Tweet tweet = new Tweet();
    tweet.setTweetLink(tweetLink);
    Set<String> tweetLikers = twitterConsumerService.retrieveTweetLikers(tweetLink, getTwitterBearerToken());
    if (CollectionUtils.isNotEmpty(tweetLikers)) {
      tweet.setLikers(tweetLikers);
    }
    Set<String> tweetRetweeters = twitterConsumerService.retrieveTweetRetweeters(tweetLink, getTwitterBearerToken());
    if (CollectionUtils.isNotEmpty(tweetLikers)) {
      tweet.setRetweeters(tweetRetweeters);
    }
    return twitterTweetStorage.addTweetToWatch(tweet);
  }

  @Override
  public List<Tweet> getTweets(int offset, int limit) {
    return twitterTweetStorage.getTweets(offset, limit);
  }

  @Override
  public long countTweets() {
    return twitterTweetStorage.countTweets();
  }

  @Override
  public Tweet getTweetByLink(String tweetLink) {
    if (StringUtils.isBlank(tweetLink)) {
      throw new IllegalArgumentException("Tweet link is mandatory");
    }
    return twitterTweetStorage.getTweetByLink(tweetLink);
  }

  @Override
  public void deleteTweet(long tweetId) throws ObjectNotFoundException {
    Tweet tweet = twitterTweetStorage.getTweetById(tweetId);
    if (tweet == null) {
      throw new ObjectNotFoundException("Tweet with id : " + tweetId + NOT_FOUND);
    }
    twitterTweetStorage.deleteTweet(tweetId);
  }

  @Override
  public void deleteTweetById(long tweetId) {
    try {
      deleteTweet(tweetId);
    } catch (ObjectNotFoundException e) {
      LOG.debug("Tweet with id {} not found. Continue processing without interrupting current operation.", tweetId, e);
    }
  }

  @Override
  public void saveTwitterBearerToken(String bearerToken, String currentUser) throws IllegalAccessException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException("The user is not authorized to save or update Twitter Bearer Token");
    }
    String encodedBearerToken = encode(bearerToken);
    this.settingService.set(Context.GLOBAL, TWITTER_CONNECTOR_SCOPE, BEARER_TOKEN_KEY, SettingValue.create(encodedBearerToken));
  }

  public void deleteTwitterBearerToken(String currentUser) throws IllegalAccessException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException("The user is not authorized to delete Twitter Bearer Token");
    }
    this.settingService.remove(Context.GLOBAL, TWITTER_CONNECTOR_SCOPE, BEARER_TOKEN_KEY);
  }

  @Override
  public String getTwitterBearerToken(String currentUser) throws IllegalAccessException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException("The user is not authorized to check Twitter Bearer Token status");
    }
    return getTwitterBearerToken();
  }

  @Override
  public String getTwitterBearerToken() {
    SettingValue<?> settingValue = settingService.get(Context.GLOBAL, TWITTER_CONNECTOR_SCOPE, BEARER_TOKEN_KEY);
    if (settingValue != null && settingValue.getValue() != null && StringUtils.isNotBlank(settingValue.getValue().toString())) {
      return decode(settingValue.getValue().toString());
    }
    return null;
  }

  public void updateAccountLastMentionTweetId(long accountId, long lastMentionTweetId) throws ObjectNotFoundException {
    if (accountId <= 0) {
      throw new IllegalArgumentException("Account id must be positive");
    }
    TwitterAccount account = twitterAccountStorage.getTwitterAccountById(accountId);
    if (account == null) {
      throw new ObjectNotFoundException("Twitter account with id : " + accountId + NOT_FOUND);
    }
    twitterAccountStorage.updateAccountLastMentionTweetId(accountId, lastMentionTweetId);
  }

  public void updateTweetReactions(long tweetId, Set<String> likers, Set<String> retweeters) throws ObjectNotFoundException {
    if (tweetId <= 0) {
      throw new IllegalArgumentException("Tweet id must be positive");
    }
    Tweet tweet = twitterTweetStorage.getTweetById(tweetId);
    if (tweet == null) {
      throw new ObjectNotFoundException("Tweet with id : " + tweetId + NOT_FOUND);
    }
    twitterTweetStorage.updateTweetReactions(tweetId, likers, retweeters);
  }

  private String encode(String token) {
    try {
      return codecInitializer.getCodec().encode(token);
    } catch (TokenServiceInitializationException e) {
      throw new IllegalStateException("Error encrypting token", e);
    }
  }

  private String decode(String encryptedToken) {
    try {
      return codecInitializer.getCodec().decode(encryptedToken);
    } catch (TokenServiceInitializationException e) {
      throw new IllegalStateException("Error decrypting token", e);
    }
  }
}
