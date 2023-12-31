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
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.meeds.gamification.twitter.model.TwitterTrigger;
import io.meeds.gamification.twitter.service.TwitterTriggerService;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.picocontainer.Startable;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;

import io.meeds.gamification.model.EventDTO;
import io.meeds.gamification.service.ConnectorService;
import io.meeds.gamification.service.EventService;

public class TwitterTriggerServiceImpl implements TwitterTriggerService, Startable {

  private static final Log       LOG                        = ExoLogger.getLogger(TwitterTriggerServiceImpl.class);

  public static final String     CONNECTOR_NAME             = "twitter";

  public static final String     GAMIFICATION_GENERIC_EVENT = "exo.gamification.generic.action";

  private final ConnectorService connectorService;

  private final EventService     eventService;

  private final IdentityManager  identityManager;

  private final ListenerService  listenerService;

  private ExecutorService        executorService;

  public TwitterTriggerServiceImpl(ListenerService listenerService,
                                   ConnectorService connectorService,
                                   IdentityManager identityManager,
                                   EventService eventService) {
    this.listenerService = listenerService;
    this.connectorService = connectorService;
    this.identityManager = identityManager;
    this.eventService = eventService;
  }

  @Override
  public void start() {
    QueuedThreadPool threadFactory = new QueuedThreadPool(5, 1, 1);
    threadFactory.setName("Gamification - Twitter connector");
    executorService = Executors.newCachedThreadPool(threadFactory);
  }

  @Override
  public void stop() {
    if (executorService != null) {
      executorService.shutdownNow();
    }
  }

  public void handleTriggerAsync(TwitterTrigger twitterTrigger) {
    executorService.execute(() -> handleTriggerAsyncInternal(twitterTrigger));
  }

  @ExoTransactional
  public void handleTriggerAsyncInternal(TwitterTrigger twitterTrigger) {
    processEvent(twitterTrigger);
  }

  private boolean isEventEnabled(String eventName, String trigger, String remoteAccountId) {
    EventDTO eventDTO = eventService.getEventByTitleAndTrigger(eventName, trigger);
    return eventDTO != null && isAccountEventEnabled(eventDTO, remoteAccountId);
  }

  private boolean isAccountEventEnabled(EventDTO eventDTO, String remoteAccountId) {
    String accountPropertyKey = remoteAccountId + ".enabled";
    Map<String, String> properties = eventDTO.getProperties();
    if (properties != null && !properties.isEmpty()) {
      return Boolean.parseBoolean(properties.get(accountPropertyKey));
    }
    return true;
  }

  private void processEvent(TwitterTrigger twitterTrigger) {
    if (!isEventEnabled(twitterTrigger.getTrigger(),
                        twitterTrigger.getTrigger(),
                        String.valueOf(twitterTrigger.getAccountId()))) {
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
      EventDTO eventDTO = eventService.getEventByTypeAndTitle(CONNECTOR_NAME, ruleTitle);
      if (eventDTO != null) {
        Map<String, String> gam = new HashMap<>();
        gam.put("senderId", receiverId);
        gam.put("receiverId", receiverId);
        gam.put("objectId", objectId);
        gam.put("objectType", objectType);
        gam.put("ruleTitle", eventDTO.getTitle());
        listenerService.broadcast(GAMIFICATION_GENERIC_EVENT, gam, "");
        LOG.info("Twitter action {} broadcasted for user {}", ruleTitle, receiverId);
      }
    } catch (Exception e) {
      LOG.error("Cannot broadcast twitter gamification event", e);
    }
  }
}
