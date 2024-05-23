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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package io.meeds.gamification.twitter.storage;

import java.util.Date;
import java.util.List;

import io.meeds.gamification.twitter.dao.TwitterAccountDAO;
import io.meeds.gamification.twitter.entity.TwitterAccountEntity;
import io.meeds.gamification.twitter.model.TwitterAccount;
import io.meeds.gamification.twitter.storage.mapper.TwitterAccountMapper;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import static io.meeds.gamification.twitter.storage.mapper.TwitterAccountMapper.fromEntity;
import static io.meeds.gamification.twitter.storage.mapper.TwitterAccountMapper.toEntity;

@Repository
public class TwitterAccountStorage {

  @Autowired
  private TwitterAccountDAO twitterAccountDAO;

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

  public List<TwitterAccount> getTwitterAccounts(int offset, int limit) {
    if (limit > 0) {
      PageRequest pageable = PageRequest.of(Math.toIntExact(offset / limit), limit, Sort.by(Sort.Direction.ASC, "id"));
      return twitterAccountDAO.findAll(pageable).getContent().stream().map(TwitterAccountMapper::fromEntity).toList();
    } else {
      return twitterAccountDAO.findAll().stream().map(TwitterAccountMapper::fromEntity).toList();
    }
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
}
