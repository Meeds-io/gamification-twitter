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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.gamification.twitter.rest.builder;

import java.util.Collection;
import java.util.List;

import io.meeds.gamification.twitter.model.RemoteTwitterAccount;
import io.meeds.gamification.twitter.model.TokenStatus;
import io.meeds.gamification.twitter.model.TwitterAccount;
import io.meeds.gamification.twitter.rest.model.TwitterAccountRestEntity;
import io.meeds.gamification.twitter.service.TwitterService;
import io.meeds.gamification.twitter.service.TwitterConsumerService;

public class TwitterAccountBuilder {

  private TwitterAccountBuilder() {
    // Class with static methods
  }

  public static TwitterAccountRestEntity toRestEntity(String twitterBearerToken,
                                                      TokenStatus tokenStatus,
                                                      TwitterConsumerService twitterConsumerService,
                                                      TwitterAccount twitterAccount) {
    if (twitterAccount == null) {
      return null;
    }
    RemoteTwitterAccount remoteTwitterAccount = null;
    if (Boolean.TRUE.equals(tokenStatus != null && tokenStatus.getIsValid() != null && tokenStatus.getIsValid())
        && tokenStatus.getRemaining() > 0) {
      remoteTwitterAccount = twitterConsumerService.retrieveTwitterAccount(twitterAccount.getRemoteId(), twitterBearerToken);
    }

    return new TwitterAccountRestEntity(twitterAccount.getId(),
                                        String.valueOf(twitterAccount.getRemoteId()),
                                        twitterAccount.getIdentifier(),
                                        remoteTwitterAccount != null ? remoteTwitterAccount.getName() : twitterAccount.getName(),
                                        twitterAccount.getWatchedDate(),
                                        twitterAccount.getWatchedBy(),
                                        twitterAccount.getUpdatedDate(),
                                        twitterAccount.getRefreshDate(),
                                        remoteTwitterAccount != null ? remoteTwitterAccount.getDescription() : null,
                                        remoteTwitterAccount != null ? remoteTwitterAccount.getAvatarUrl() : null);
  }

  public static List<TwitterAccountRestEntity> toRestEntities(TwitterService twitterAccountService,
                                                              TwitterConsumerService twitterConsumerService,
                                                              Collection<TwitterAccount> twitterAccounts) {
    String twitterBearerToken = twitterAccountService.getTwitterBearerToken();
    TokenStatus tokenStatus = twitterConsumerService.checkTwitterTokenStatus(twitterBearerToken);

    return twitterAccounts.stream()
                          .map(twitterAccount -> toRestEntity(twitterBearerToken,
                                                              tokenStatus,
                                                              twitterConsumerService,
                                                              twitterAccount))
                          .toList();
  }
}
