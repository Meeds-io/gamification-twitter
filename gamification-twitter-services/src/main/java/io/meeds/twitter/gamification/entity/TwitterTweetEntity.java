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
package io.meeds.twitter.gamification.entity;

import java.io.Serializable;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Data;

@Entity(name = "TwitterTweets")
@Table(name = "TWITTER_TWEETS")
@Data
public class TwitterTweetEntity implements Serializable {

  private static final long serialVersionUID = -4871930064565777769L;

  @Id
  @SequenceGenerator(name = "SEQ_TWITTER_ACCOUNTS_ID", sequenceName = "SEQ_TWITTER_ACCOUNTS_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_TWITTER_ACCOUNTS_ID")
  @Column(name = "TWEET_ID")
  private Long              id;

  @Column(name = "TWEET_LINK")
  private String            tweetLink;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "TWITTER_TWEET_LIKERS", joinColumns = @JoinColumn(name = "TWEET_ID"))
  @Column(name = "LIKER_USERNAME")
  private Set<String>       likers;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "TWITTER_TWEET_RETWEETERS", joinColumns = @JoinColumn(name = "TWEET_ID"))
  @Column(name = "RETWEETER_USERNAME")
  private Set<String>       retweeters;
}
