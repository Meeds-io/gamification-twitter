/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.twitter.gamification.rest;

import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.meeds.twitter.gamification.model.TwitterAccount;
import io.meeds.twitter.gamification.service.TwitterConsumerService;
import io.meeds.twitter.gamification.service.TwitterService;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import io.meeds.spring.web.security.PortalAuthenticationManager;
import io.meeds.spring.web.security.WebSecurityConfiguration;
import jakarta.servlet.Filter;

@SpringBootTest(classes = { TwitterAccountRest.class, PortalAuthenticationManager.class, })
@ContextConfiguration(classes = { WebSecurityConfiguration.class })
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class TwitterAccountRestTest {

  private static final String    REST_PATH     = "/twitter/accounts"; // NOSONAR

  private static final String    SIMPLE_USER   = "simple";

  private static final String    TEST_PASSWORD = "testPassword";

  @MockBean
  private TwitterService         twitterService;

  @MockBean
  private TwitterConsumerService twitterConsumerService;

  @Autowired
  private SecurityFilterChain    filterChain;

  @Autowired
  private WebApplicationContext  context;

  private MockMvc                mockMvc;

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilters(filterChain.getFilters().toArray(new Filter[0])).build();
  }

  @Test
  void getWatchedAccountsAnonymously() throws Exception {
    ResultActions response = mockMvc.perform(get(REST_PATH + "?offset=0&limit=10&forceUpdate=false&returnSize=true"));
    response.andExpect(status().isForbidden());
  }

  @Test
  void getWatchedAccountsSimpleUser() throws Exception {
    ResultActions response = mockMvc.perform(get(REST_PATH
        + "?offset=0&limit=10&forceUpdate=false&returnSize=true").with(testSimpleUser()));
    response.andExpect(status().isOk());

    doThrow(new IllegalAccessException()).when(twitterService).getTwitterAccounts(SIMPLE_USER, 0, 10);

    response = mockMvc.perform(get(REST_PATH + "?offset=0&limit=10&forceUpdate=false&returnSize=true").with(testSimpleUser()));
    response.andExpect(status().isUnauthorized());
  }

  @Test
  void getWatchedAccountByIdAnonymously() throws Exception {
    ResultActions response = mockMvc.perform(get(REST_PATH + "/" + 1));
    response.andExpect(status().isForbidden());
  }

  @Test
  void getWatchedAccountByIdSimpleUser() throws Exception {
    ResultActions response = mockMvc.perform(get(REST_PATH + "/" + 1).with(testSimpleUser()));
    response.andExpect(status().isOk());

    response = mockMvc.perform(get(REST_PATH + "/" + 0).with(testSimpleUser()));
    response.andExpect(status().isBadRequest());

    doThrow(new IllegalAccessException()).when(twitterService).getTwitterAccountById(1, SIMPLE_USER);

    response = mockMvc.perform(get(REST_PATH + "/" + 1).with(testSimpleUser()));
    response.andExpect(status().isUnauthorized());

    doThrow(new IllegalArgumentException()).when(twitterService).getTwitterAccountById(1, SIMPLE_USER);

    response = mockMvc.perform(get(REST_PATH + "/" + 1).with(testSimpleUser()));
    response.andExpect(status().isBadRequest());

    doThrow(new ObjectNotFoundException("Twitter account doesn't exist")).when(twitterService)
                                                                         .getTwitterAccountById(1, SIMPLE_USER);

    response = mockMvc.perform(get(REST_PATH + "/" + 1).with(testSimpleUser()));
    response.andExpect(status().isNotFound());
  }

  @Test
  void createWatchedAccountAnonymously() throws Exception {
    ResultActions response = mockMvc.perform(post(REST_PATH).param("twitterUsername", "twitterUsername")
                                                            .contentType(MediaType.APPLICATION_JSON)
                                                            .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isForbidden());
  }

  @Test
  void createWatchedAccountSimpleUser() throws Exception {
    ResultActions response = mockMvc.perform(post(REST_PATH).param("twitterUsername", "")
                                                            .with(testSimpleUser())
                                                            .contentType(MediaType.APPLICATION_JSON)
                                                            .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest());

    response = mockMvc.perform(post(REST_PATH).param("twitterUsername", "twitterUsername")
                                              .with(testSimpleUser())
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isOk());

    doThrow(new IllegalAccessException()).when(twitterService).addTwitterAccount("twitterUsername", SIMPLE_USER);

    response = mockMvc.perform(post(REST_PATH).param("twitterUsername", "twitterUsername")
                                              .with(testSimpleUser())
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isUnauthorized());

    doThrow(new ObjectAlreadyExistsException(new TwitterAccount())).when(twitterService)
                                                                   .addTwitterAccount("twitterUsername", SIMPLE_USER);

    response = mockMvc.perform(post(REST_PATH).param("twitterUsername", "twitterUsername")
                                              .with(testSimpleUser())
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isConflict());

    doThrow(new ObjectNotFoundException("twitter.accountNotFound")).when(twitterService)
                                                                   .addTwitterAccount("twitterUsername", SIMPLE_USER);

    response = mockMvc.perform(post(REST_PATH).param("twitterUsername", "twitterUsername")
                                              .with(testSimpleUser())
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isNotFound());
  }

  @Test
  void deleteTwitterBearerTokenAnonymously() throws Exception {
    ResultActions response = mockMvc.perform(delete(REST_PATH + "/" + 1).contentType(MediaType.APPLICATION_JSON)
                                                                        .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isForbidden());
  }

  @Test
  void deleteTwitterBearerTokenSimpleUser() throws Exception {
    ResultActions response = mockMvc.perform(delete(REST_PATH + "/" + 1).with(testSimpleUser())
                                                                        .contentType(MediaType.APPLICATION_JSON)
                                                                        .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isOk());

    doThrow(new IllegalAccessException()).when(twitterService).deleteTwitterAccount(1, SIMPLE_USER);

    response = mockMvc.perform(delete(REST_PATH + "/" + 1).with(testSimpleUser())
                                                          .contentType(MediaType.APPLICATION_JSON)
                                                          .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isUnauthorized());

    doThrow(new ObjectNotFoundException("Twitter account doesn't exist")).when(twitterService)
                                                                         .deleteTwitterAccount(1, SIMPLE_USER);

    response = mockMvc.perform(delete(REST_PATH + "/" + 1).with(testSimpleUser())
                                                          .contentType(MediaType.APPLICATION_JSON)
                                                          .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isNotFound());
  }

  private RequestPostProcessor testSimpleUser() {
    return user(SIMPLE_USER).password(TEST_PASSWORD).authorities(new SimpleGrantedAuthority("users"));
  }

}
