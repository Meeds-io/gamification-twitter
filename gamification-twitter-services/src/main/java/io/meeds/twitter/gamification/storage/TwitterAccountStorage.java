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
 */
package io.meeds.twitter.gamification.storage;

import java.util.Date;
import java.util.List;

import io.meeds.twitter.gamification.dao.TwitterAccountDAO;
import io.meeds.twitter.gamification.entity.TwitterAccountEntity;
import io.meeds.twitter.gamification.model.TwitterAccount;
import io.meeds.twitter.gamification.storage.mapper.TwitterAccountMapper;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.web.security.codec.CodecInitializer;
import org.exoplatform.web.security.security.TokenServiceInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import static io.meeds.twitter.gamification.storage.mapper.TwitterAccountMapper.fromEntity;
import static io.meeds.twitter.gamification.storage.mapper.TwitterAccountMapper.toEntity;

@Repository
public class TwitterAccountStorage {

  private static final Scope  TWITTER_CONNECTOR_SCOPE = Scope.APPLICATION.id("twitterConnector");

  private static final String BEARER_TOKEN_KEY        = "BEARER_TOKEN";

  @Autowired
  private TwitterAccountDAO   twitterAccountDAO;

  @Autowired
  private SettingService      settingService;

  @Autowired
  private CodecInitializer    codecInitializer;

  public TwitterAccount addTwitterAccount(TwitterAccount twitterAccount) throws ObjectAlreadyExistsException {
    TwitterAccount existsAccount = getTwitterAccountByRemoteId(twitterAccount.getRemoteId());
    if (existsAccount == null) {
      TwitterAccountEntity twitterAccountEntity = toEntity(twitterAccount);
      twitterAccountEntity.setWatchedDate(new Date());
      twitterAccountEntity.setUpdatedDate(new Date());
      twitterAccountEntity.setRefreshDate(new Date());
      twitterAccountEntity = twitterAccountDAO.save(twitterAccountEntity);
      return fromEntity(twitterAccountEntity);
    } else {
      throw new ObjectAlreadyExistsException(existsAccount);
    }
  }

  public TwitterAccount getTwitterAccountById(Long id) {
    return fromEntity(twitterAccountDAO.findById(id).orElse(null));
  }

  public Page<TwitterAccount> getTwitterAccounts(Pageable pageable) {
    Page<TwitterAccountEntity> page = twitterAccountDAO.findAll(pageable);
    return page.map(TwitterAccountMapper::fromEntity);
  }

  public List<TwitterAccount> getTwitterAccounts() {
    List<TwitterAccountEntity> twitterAccountEntities = twitterAccountDAO.findAll();
    return twitterAccountEntities.stream().map(TwitterAccountMapper::fromEntity).toList();
  }

  public long countTwitterAccounts() {
    return twitterAccountDAO.count();
  }

  public TwitterAccount getTwitterAccountByRemoteId(long remoteId) {
    TwitterAccountEntity twitterAccountEntity = twitterAccountDAO.findTwitterAccountEntityByRemoteId(remoteId);
    return fromEntity(twitterAccountEntity);
  }

  public TwitterAccount updateAccountLastMentionTweetId(long accountId, long lastMentionTweetId) {
    TwitterAccountEntity twitterAccountEntity = twitterAccountDAO.findById(accountId).orElse(null);
    if (twitterAccountEntity == null) {
      return null;
    }
    twitterAccountEntity.setLastMentionTweetId(lastMentionTweetId);
    return fromEntity(twitterAccountDAO.save(twitterAccountEntity));
  }

  public TwitterAccount deleteTwitterAccount(long accountId) {
    TwitterAccountEntity twitterAccountEntity = twitterAccountDAO.findById(accountId).orElse(null);
    if (twitterAccountEntity != null) {
      twitterAccountDAO.delete(twitterAccountEntity);
    }
    return fromEntity(twitterAccountEntity);
  }

  public void saveTwitterBearerToken(String bearerToken) {
    String encodedBearerToken = encode(bearerToken);
    this.settingService.set(Context.GLOBAL, TWITTER_CONNECTOR_SCOPE, BEARER_TOKEN_KEY, SettingValue.create(encodedBearerToken));
  }

  public void deleteTwitterBearerToken() {
    this.settingService.remove(Context.GLOBAL, TWITTER_CONNECTOR_SCOPE, BEARER_TOKEN_KEY);
  }

  public String getTwitterBearerToken() {
    SettingValue<?> settingValue = settingService.get(Context.GLOBAL, TWITTER_CONNECTOR_SCOPE, BEARER_TOKEN_KEY);
    if (settingValue != null && settingValue.getValue() != null && StringUtils.isNotBlank(settingValue.getValue().toString())) {
      return decode(settingValue.getValue().toString());
    }
    return null;
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
