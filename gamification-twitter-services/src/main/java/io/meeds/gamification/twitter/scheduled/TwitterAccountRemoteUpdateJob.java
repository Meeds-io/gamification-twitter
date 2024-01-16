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
package io.meeds.gamification.twitter.scheduled;

import io.meeds.gamification.twitter.model.TwitterAccount;
import io.meeds.gamification.twitter.model.TwitterTrigger;
import io.meeds.gamification.twitter.service.TwitterAccountService;
import io.meeds.gamification.twitter.service.TwitterConsumerService;
import io.meeds.gamification.twitter.service.TwitterTriggerService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.container.ExoContainerContext;

import java.util.List;

/**
 * A service that will manage the periodic updating of twitter account events.
 */
@DisallowConcurrentExecution
public class TwitterAccountRemoteUpdateJob implements Job {

  private static final Log             LOG = ExoLogger.getLogger(TwitterAccountRemoteUpdateJob.class);

  private final TwitterConsumerService twitterConsumerService;

  private final TwitterAccountService  twitterAccountService;

  private final TwitterTriggerService  twitterTriggerService;

  public TwitterAccountRemoteUpdateJob() {
    this.twitterTriggerService = ExoContainerContext.getService(TwitterTriggerService.class);
    this.twitterConsumerService = ExoContainerContext.getService(TwitterConsumerService.class);
    this.twitterAccountService = ExoContainerContext.getService(TwitterAccountService.class);
  }

  @Override
  @ExoTransactional
  public void execute(JobExecutionContext context) {
    String bearerToken = twitterAccountService.getTwitterBearerToken();
    if (StringUtils.isBlank(bearerToken)) {
      return;
    }
    List<TwitterAccount> twitterAccounts = twitterAccountService.getTwitterAccounts(0, -1);
    if (CollectionUtils.isNotEmpty(twitterAccounts)) {
      twitterAccounts.forEach(twitterAccount -> processTwitterAccount(twitterAccount, bearerToken));
    }
  }

  private void processTwitterAccount(TwitterAccount twitterAccount, String bearerToken) {
    try {
      List<TwitterTrigger> mentionTriggers = twitterConsumerService.getMentionEvents(twitterAccount,
                                                                                     twitterAccount.getLastMentionTweetId(),
                                                                                     bearerToken);

      if (CollectionUtils.isNotEmpty(mentionTriggers)) {
        processMentionTriggers(mentionTriggers, twitterAccount);
      }
    } catch (ObjectNotFoundException e) {
      LOG.warn("Error while updating twitter account {}", twitterAccount.getId(), e);
    }
  }

  private void processMentionTriggers(List<TwitterTrigger> mentionTriggers,
                                      TwitterAccount twitterAccount) throws ObjectNotFoundException {
    for (TwitterTrigger trigger : mentionTriggers) {
      twitterTriggerService.handleTriggerAsync(trigger);
    }
    twitterAccountService.updateAccountLastMentionTweetId(twitterAccount.getId(), mentionTriggers.get(0).getTweetId());
  }
}
