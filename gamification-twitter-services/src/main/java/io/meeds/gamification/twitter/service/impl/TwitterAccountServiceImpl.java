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
package io.meeds.gamification.twitter.service.impl;

import io.meeds.gamification.twitter.model.RemoteTwitterAccount;
import io.meeds.gamification.twitter.model.TwitterAccount;
import io.meeds.gamification.twitter.service.TwitterAccountService;
import io.meeds.gamification.twitter.storage.TwitterAccountStorage;
import io.meeds.gamification.twitter.storage.TwitterConsumerStorage;
import io.meeds.gamification.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.security.codec.CodecInitializer;
import org.exoplatform.web.security.security.TokenServiceInitializationException;

import java.util.List;

public class TwitterAccountServiceImpl implements TwitterAccountService {

  private static final Log             LOG                     = ExoLogger.getLogger(TwitterAccountServiceImpl.class);

  private static final Scope           TWITTER_CONNECTOR_SCOPE = Scope.APPLICATION.id("twitterConnector");

  private static final String          BEARER_TOKEN_KEY        = "BEARER_TOKEN";

  private final SettingService         settingService;

  private final TwitterConsumerStorage twitterConsumerStorage;

  private final TwitterAccountStorage  twitterAccountStorage;

  public TwitterAccountServiceImpl(SettingService settingService,
                                   TwitterConsumerStorage twitterConsumerStorage,
                                   TwitterAccountStorage twitterAccountStorage) {
    this.settingService = settingService;
    this.twitterConsumerStorage = twitterConsumerStorage;
    this.twitterAccountStorage = twitterAccountStorage;
  }

  @Override
  public List<TwitterAccount> getTwitterAccounts(String currentUser,
                                                 int offset,
                                                 int limit,
                                                 boolean forceUpdate) throws IllegalAccessException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException("The user is not authorized to access Twitter watched accounts");
    }
    if (forceUpdate) {
      twitterConsumerStorage.clearCache();
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
    List<Long> hooksIds = twitterAccountStorage.getTwitterAccountIds(offset, limit);
    return hooksIds.stream().map(twitterAccountStorage::getTwitterAccountById).toList();
  }

  @Override
  public int countTwitterAccounts(String currentUser) throws IllegalAccessException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException("The user is not authorized to access Twitter watched accounts");
    }
    return twitterAccountStorage.countTwitterAccounts();
  }

  @Override
  public void addTwitterAccount(String twitterUsername, String currentUser) throws ObjectAlreadyExistsException,
                                                                            IllegalAccessException,
                                                                            ObjectNotFoundException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException("The user is not authorized to add a twitter watched account");
    }
    RemoteTwitterAccount remoteTwitterAccount = twitterConsumerStorage.retrieveTwitterAccount(twitterUsername,
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
      twitterAccount.setWatchedBy(currentUser);
      twitterAccountStorage.addTwitterAccount(twitterAccount);
    }
  }

  @Override
  public void deleteTwitterAccount(long twitterAccountId, String currentUser) throws IllegalAccessException,
                                                                              ObjectNotFoundException {

    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException("The user is not authorized to delete Twitter account");
    }
    TwitterAccount twitterAccount = twitterAccountStorage.getTwitterAccountById(twitterAccountId);
    if (twitterAccount == null) {
      throw new ObjectNotFoundException("Twitter account with remote id : " + twitterAccountId + " wasn't found");
    }
    twitterAccountStorage.deleteTwitterAccount(twitterAccountId);
    twitterConsumerStorage.clearCache(twitterAccount, getTwitterBearerToken());
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

  private String encode(String token) {
    try {
      CodecInitializer codecInitializer = CommonsUtils.getService(CodecInitializer.class);
      return codecInitializer.getCodec().encode(token);
    } catch (TokenServiceInitializationException e) {
      LOG.warn("Error when encoding token", e);
      return null;
    }
  }

  public static String decode(String encryptedToken) {
    try {
      CodecInitializer codecInitializer = CommonsUtils.getService(CodecInitializer.class);
      return codecInitializer.getCodec().decode(encryptedToken);
    } catch (TokenServiceInitializationException e) {
      LOG.warn("Error when decoding token", e);
      return null;
    }
  }
}
