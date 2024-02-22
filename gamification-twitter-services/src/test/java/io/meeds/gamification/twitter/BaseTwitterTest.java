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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package io.meeds.gamification.twitter;

import io.meeds.gamification.service.ConnectorService;
import io.meeds.gamification.service.EventService;
import io.meeds.gamification.service.RuleService;
import io.meeds.gamification.service.TriggerService;
import io.meeds.gamification.twitter.dao.TwitterAccountDAO;
import io.meeds.gamification.twitter.dao.TwitterTweetDAO;
import io.meeds.gamification.twitter.service.TwitterConsumerService;
import io.meeds.gamification.twitter.service.TwitterService;
import io.meeds.gamification.twitter.service.TwitterTriggerService;
import io.meeds.gamification.twitter.service.impl.TwitterServiceImpl;
import io.meeds.gamification.twitter.service.impl.TwitterTriggerServiceImpl;
import io.meeds.gamification.twitter.storage.TwitterAccountStorage;
import io.meeds.gamification.twitter.storage.TwitterTweetStorage;
import io.meeds.gamification.utils.Utils;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.web.security.codec.CodecInitializer;
import org.junit.After;
import org.junit.Before;

import org.exoplatform.commons.testing.BaseExoTestCase;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.security.ConversationState;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
@ConfiguredBy({ @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/twitter-test-configuration.xml"), })
public abstract class BaseTwitterTest extends BaseExoTestCase { // NOSONAR

  protected TwitterService         twitterService;

  protected TwitterTriggerService  twitterTriggerService;

  protected TwitterAccountDAO      twitterAccountDAO;

  protected SettingService         settingService;

  protected TwitterAccountStorage  twitterAccountStorage;

  protected TwitterTweetStorage    twitterTweetStorage;

  @Mock
  protected TwitterConsumerService twitterConsumerService;

  @Mock
  protected RuleService            ruleService;

  @Mock
  protected ConnectorService       connectorService;

  @Mock
  protected TriggerService         triggerService;

  @Mock
  protected ListenerService        listenerService;

  @Mock
  protected EventService           eventService;

  protected CodecInitializer       codecInitializer;

  protected TwitterTweetDAO        twitterTweetDAO;

  protected IdentityManager        identityManager;

  protected IdentityRegistry       identityRegistry;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    ExoContainerContext.setCurrentContainer(getContainer());
    twitterAccountDAO = ExoContainerContext.getService(TwitterAccountDAO.class);
    twitterTweetDAO = ExoContainerContext.getService(TwitterTweetDAO.class);
    settingService = ExoContainerContext.getService(SettingService.class);
    twitterAccountStorage = ExoContainerContext.getService(TwitterAccountStorage.class);
    twitterTweetStorage = ExoContainerContext.getService(TwitterTweetStorage.class);
    codecInitializer = ExoContainerContext.getService(CodecInitializer.class);
    identityRegistry = ExoContainerContext.getService(IdentityRegistry.class);
    identityManager = ExoContainerContext.getService(IdentityManager.class);
    twitterTriggerService = new TwitterTriggerServiceImpl(listenerService,
                                                          connectorService,
                                                          identityManager,
                                                          eventService,
                                                          triggerService);
    twitterService = new TwitterServiceImpl(settingService,
                                            twitterConsumerService,
                                            twitterAccountStorage,
                                            twitterTweetStorage,
                                            ruleService,
                                            codecInitializer);
    resetUserSession();
    begin();
  }

  @Override
  @After
  public void tearDown() {
    restartTransaction();
    twitterTweetDAO.deleteAll();
    twitterAccountDAO.deleteAll();
    end();
  }

  protected void resetUserSession() {
    ConversationState.setCurrent(null);
  }

  protected org.exoplatform.services.security.Identity registerAdministratorUser(String user) {
    org.exoplatform.services.security.Identity identity =
                                                        new org.exoplatform.services.security.Identity(user,
                                                                                                       Collections.singletonList(new MembershipEntry(Utils.REWARDING_GROUP)));
    identityRegistry.register(identity);
    return identity;
  }

  protected org.exoplatform.services.security.Identity registerInternalUser(String username) {
    org.exoplatform.services.security.Identity identity =
                                                        new org.exoplatform.services.security.Identity(username,
                                                                                                       Collections.singletonList(new MembershipEntry("/platform/users")));
    identityRegistry.register(identity);
    return identity;
  }
}
