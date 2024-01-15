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
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.gamification.twitter.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.meeds.gamification.service.TriggerService;
import io.meeds.gamification.twitter.model.TwitterTrigger;
import io.meeds.gamification.twitter.service.TwitterTriggerService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;

import io.meeds.gamification.model.EventDTO;
import io.meeds.gamification.service.ConnectorService;
import io.meeds.gamification.service.EventService;

public class TwitterTriggerServiceImpl implements TwitterTriggerService {

  private static final Log       LOG                        = ExoLogger.getLogger(TwitterTriggerServiceImpl.class);

  public static final String     CONNECTOR_NAME             = "twitter";

  public static final String     GAMIFICATION_GENERIC_EVENT = "exo.gamification.generic.action";

  private final ConnectorService connectorService;

  private final EventService     eventService;

  private final TriggerService   triggerService;

  private final IdentityManager  identityManager;

  private final ListenerService  listenerService;

  public TwitterTriggerServiceImpl(ListenerService listenerService,
                                   ConnectorService connectorService,
                                   IdentityManager identityManager,
                                   EventService eventService,
                                   TriggerService triggerService) {
    this.listenerService = listenerService;
    this.connectorService = connectorService;
    this.identityManager = identityManager;
    this.eventService = eventService;
    this.triggerService = triggerService;
  }

  @ExoTransactional
  public void handleTrigger(TwitterTrigger twitterTrigger) {
    processEvent(twitterTrigger);
  }

  private boolean isTriggerEnabled(String trigger, long remoteAccountId) {
    return triggerService.isTriggerEnabledForAccount(trigger, remoteAccountId);
  }

  private void processEvent(TwitterTrigger twitterTrigger) {
    if (!isTriggerEnabled(twitterTrigger.getTrigger(), twitterTrigger.getAccountId())) {
      return;
    }
    String receiverId = connectorService.getAssociatedUsername(CONNECTOR_NAME, twitterTrigger.getTwitterUsername());
    if (StringUtils.isNotBlank(receiverId)) {
      Identity socialIdentity = identityManager.getOrCreateUserIdentity(receiverId);
      if (socialIdentity != null) {
        broadcastTwitterEvent(twitterTrigger.getTrigger(),
                              receiverId,
                              String.valueOf(twitterTrigger.getTweetId()),
                              twitterTrigger.getType());
      }
    }
  }

  private void broadcastTwitterEvent(String ruleTitle, String receiverId, String objectId, String objectType) {
    try {
      List<EventDTO> events = eventService.getEventsByTitle(ruleTitle, 0, -1);
      if (CollectionUtils.isNotEmpty(events)) {
        Map<String, String> gam = new HashMap<>();
        gam.put("senderId", receiverId);
        gam.put("receiverId", receiverId);
        gam.put("objectId", objectId);
        gam.put("objectType", objectType);
        gam.put("ruleTitle", ruleTitle);
        listenerService.broadcast(GAMIFICATION_GENERIC_EVENT, gam, "");
        LOG.info("Twitter action {} broadcasted for user {}", ruleTitle, receiverId);
      }
    } catch (Exception e) {
      LOG.error("Cannot broadcast twitter gamification event", e);
    }
  }
}
