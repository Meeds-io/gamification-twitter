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
package io.meeds.gamification.twitter.service;

import io.meeds.gamification.twitter.model.RemoteTwitterAccount;
import io.meeds.gamification.twitter.model.TokenStatus;
import org.exoplatform.commons.exception.ObjectNotFoundException;

public interface TwitterConsumerService {

  /**
   * Retrieve available Twitter account info.
   *
   * @param twitterUsername Twitter account username
   * @param bearerToken Twitter bearer token
   * @throws ObjectNotFoundException when the Twitter account identified by its
   *           technical name is not found
   * @return {@link RemoteTwitterAccount}
   */
  RemoteTwitterAccount retrieveTwitterAccount(String twitterUsername, String bearerToken) throws ObjectNotFoundException;

  /**
   * Retrieve available Twitter account info.
   *
   * @param twitterRemoteId Twitter account remote Id
   * @param bearerToken Twitter bearer token
   * @return {@link RemoteTwitterAccount}
   */
  RemoteTwitterAccount retrieveTwitterAccount(long twitterRemoteId, String bearerToken);

  /**
   * Check Twitter token status
   *
   * @param bearerToken Twitter bearer token
   * @return {@link TokenStatus}
   */
  TokenStatus checkTwitterTokenStatus(String bearerToken);

  /**
   * clear remote webhook entities cache
   */
  void clearCache();
}
