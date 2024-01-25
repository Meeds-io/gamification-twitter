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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.gamification.twitter.dao;

import io.meeds.gamification.twitter.entity.TwitterAccountEntity;
import io.meeds.gamification.twitter.entity.TwitterTweetEntity;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

import java.util.List;

public class TwitterTweetDAO extends GenericDAOJPAImpl<TwitterTweetEntity, Long> {
    
    public static final String TWEET_LINK = "tweetLink";

    public List<Long> getTweetsIds(int offset, int limit) {
        TypedQuery<Long> query = getEntityManager().createNamedQuery("TwitterTweets.getTweetsIds", Long.class);
        if (offset > 0) {
            query.setFirstResult(offset);
        }
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }

    public TwitterTweetEntity getTweetByLink(String tweetLink) {
      TypedQuery<TwitterTweetEntity> query = getEntityManager().createNamedQuery("TwitterTweets.getTweetByLink",
                                                                                 TwitterTweetEntity.class);
      query.setParameter(TWEET_LINK, tweetLink);
      try {
        return query.getSingleResult();
      } catch (NoResultException e) {
        return null;
      }
    }

}
