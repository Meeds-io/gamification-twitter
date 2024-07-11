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
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.gamification.twitter.entity;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.*;

import lombok.Data;

@Entity(name = "TwitterAccounts")
@Table(name = "TWITTER_ACCOUNTS")

@NamedQuery(name = "TwitterAccounts.getTwitterAccountByRemoteId",
            query = "SELECT twitterAccount FROM TwitterAccounts twitterAccount"
                  + " WHERE twitterAccount.remoteId = :remoteId")
@NamedQuery(name = "TwitterAccounts.getAccountsIds",
            query = "SELECT twitterAccount.id FROM TwitterAccounts twitterAccount"
                  + " ORDER BY twitterAccount.id ASC")
@Data
public class TwitterAccountEntity implements Serializable {

  private static final long serialVersionUID = -7390409979056587159L;

  @Id
  @SequenceGenerator(name = "SEQ_TWITTER_ACCOUNTS_ID", sequenceName = "SEQ_TWITTER_ACCOUNTS_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_TWITTER_ACCOUNTS_ID")
  @Column(name = "ID")
  private Long   id;

  @Column(name = "REMOTE_ID")
  private Long   remoteId;

  @Column(name = "IDENTIFIER", nullable = false)
  private String identifier;

  @Column(name = "NAME", nullable = false)
  private String name;

  @Column(name = "WATCHED_DATE", nullable = false)
  private Date   watchedDate;

  @Column(name = "WATCHED_BY", nullable = false)
  private Long   watchedBy;

  @Column(name = "UPDATED_DATE", nullable = false)
  private Date   updatedDate;

  @Column(name = "REFRESH_DATE", nullable = false)
  private Date   refreshDate;

  @Column(name = "LAST_MENTION_TWEET_ID")
  private Long   lastMentionTweetId;
}
