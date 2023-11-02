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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package io.meeds.gamification.twitter.storage.mapper;

import io.meeds.gamification.twitter.entity.TwitterAccountEntity;
import io.meeds.gamification.twitter.model.TwitterAccount;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.social.core.manager.IdentityManager;

import io.meeds.gamification.utils.Utils;

public class TwitterAccountMapper {

  private TwitterAccountMapper() {
    // Class with static methods
  }

  public static TwitterAccountEntity toEntity(TwitterAccount twitterAccount) {
    if (twitterAccount == null) {
      return null;
    }
    TwitterAccountEntity twitterAccountEntity = new TwitterAccountEntity();

    if (twitterAccount.getId() > 0) {
      twitterAccountEntity.setId(twitterAccount.getId());
    }
    if (twitterAccount.getRemoteId() > 0) {
      twitterAccountEntity.setRemoteId(twitterAccount.getRemoteId());
    }
    if (StringUtils.isNotEmpty(twitterAccount.getIdentifier())) {
      twitterAccountEntity.setIdentifier(twitterAccount.getIdentifier());
    }
    if (StringUtils.isNotEmpty(twitterAccount.getName())) {
      twitterAccountEntity.setName(twitterAccount.getName());
    }
    if (twitterAccount.getWatchedBy() != null) {
      IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
      String userIdentityId = identityManager.getOrCreateUserIdentity(twitterAccount.getWatchedBy()).getId();
      twitterAccountEntity.setWatchedBy(Long.parseLong(userIdentityId));
    }
    return twitterAccountEntity;
  }

  public static TwitterAccount fromEntity(TwitterAccountEntity twitterAccountEntity) {
    if (twitterAccountEntity == null) {
      return null;
    }
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    String watchedBy = identityManager.getIdentity(String.valueOf(twitterAccountEntity.getWatchedBy())).getRemoteId();
    return new TwitterAccount(twitterAccountEntity.getId(),
                              twitterAccountEntity.getRemoteId(),
                              twitterAccountEntity.getIdentifier(),
                              twitterAccountEntity.getName(),
                              twitterAccountEntity.getWatchedDate() != null ? Utils.toSimpleDateFormat(twitterAccountEntity.getWatchedDate())
                                                                            : null,
                              watchedBy,
                              twitterAccountEntity.getUpdatedDate() != null ? Utils.toSimpleDateFormat(twitterAccountEntity.getUpdatedDate())
                                                                            : null,
                              twitterAccountEntity.getRefreshDate() != null ? Utils.toSimpleDateFormat(twitterAccountEntity.getRefreshDate())
                                                                            : null);
  }

}
