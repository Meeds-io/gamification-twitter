/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2023 Meeds Lab contact@meeds.io
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
package io.meeds.gamification.twitter.dao;

import io.meeds.gamification.twitter.entity.TwitterAccountEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class TwitterAccountDAO extends GenericDAOJPAImpl<TwitterAccountEntity, Long> {

  public static final String REMOTE_ID = "remoteId";

  public TwitterAccountEntity getAccountByRemoteId(long remoteId) {
    TypedQuery<TwitterAccountEntity> query = getEntityManager().createNamedQuery("TwitterAccounts.getTwitterAccountByRemoteId",
                                                                                 TwitterAccountEntity.class);
    query.setParameter(REMOTE_ID, remoteId);
    try {
      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  public List<Long> getAccountsIds(int offset, int limit) {
    TypedQuery<Long> query = getEntityManager().createNamedQuery("TwitterAccounts.getAccountsIds", Long.class);
    if (offset > 0) {
      query.setFirstResult(offset);
    }
    if (limit > 0) {
      query.setMaxResults(limit);
    }
    return query.getResultList();
  }
}
