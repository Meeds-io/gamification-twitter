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
package io.meeds.gamification.twitter.rest;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.meeds.gamification.twitter.service.TwitterConsumerService;
import io.meeds.gamification.twitter.service.TwitterService;
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

@SpringBootTest(classes = { TwitterSettingsRest.class, PortalAuthenticationManager.class, })
@ContextConfiguration(classes = { WebSecurityConfiguration.class })
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class TwitterSettingsRestTest {

  private static final String    REST_PATH    = "/twitter/settings"; // NOSONAR

  private static final String    SIMPLE_USER   = "simple";

  private static final String    ADMIN_USER    = "admin";

  private static final String    TEST_PASSWORD = "testPassword";

  @MockBean
  private TwitterService twitterService;

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
  void checkTwitterTokenStatusAnonymously() throws Exception {
    ResultActions response = mockMvc.perform(get(REST_PATH));
    response.andExpect(status().isForbidden());
  }

  @Test
  void checkTwitterTokenStatusSimpleUser() throws Exception {
    ResultActions response =
                           mockMvc.perform(get(REST_PATH).with(testSimpleUser()));
    response.andExpect(status().isForbidden());
  }

  @Test
  void checkTwitterTokenStatusAdmin() throws Exception {
    ResultActions response = mockMvc.perform(get(REST_PATH).with(testAdminUser()));
    response.andExpect(status().isOk());

    doThrow(new IllegalAccessException()).when(twitterService).getTwitterBearerToken(ADMIN_USER);
    response = mockMvc.perform(get(REST_PATH).with(testAdminUser()));
    response.andExpect(status().isUnauthorized());
  }

  @Test
  void saveBearerTokenAnonymously() throws Exception {
    ResultActions response = mockMvc.perform(post(REST_PATH).param("bearerToken", "bearerToken")
                                                             .contentType(MediaType.APPLICATION_JSON)
                                                             .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isForbidden());
  }

  @Test
  void saveBearerTokenSimpleUser() throws Exception {
    ResultActions response = mockMvc.perform(post(REST_PATH).param("bearerToken", "bearerToken")
                                                             .with(testSimpleUser())
                                                             .contentType(MediaType.APPLICATION_JSON)
                                                             .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isForbidden());
  }

  @Test
  void saveBearerTokenAdmin() throws Exception {
    ResultActions response = mockMvc.perform(post(REST_PATH).param("bearerToken", "")
                                                             .with(testAdminUser())
                                                             .contentType(MediaType.APPLICATION_JSON)
                                                             .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isBadRequest());

    response = mockMvc.perform(post(REST_PATH).param("bearerToken", "bearerToken")
                                               .with(testAdminUser())
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isOk());

    doThrow(new IllegalAccessException()).when(twitterService).saveTwitterBearerToken("bearerToken", ADMIN_USER);

    response = mockMvc.perform(post(REST_PATH).param("bearerToken", "bearerToken")
            .with(testAdminUser())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isUnauthorized());
  }

  @Test
  void deleteTwitterBearerTokenAnonymously() throws Exception {
    ResultActions response = mockMvc.perform(delete(REST_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isForbidden());
  }

  @Test
  void deleteTwitterBearerTokenSimpleUser() throws Exception {
    ResultActions response = mockMvc.perform(delete(REST_PATH)
            .with(testSimpleUser())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isForbidden());
  }

  @Test
  void deleteTwitterBearerTokenAdmin() throws Exception {
    ResultActions response = mockMvc.perform(delete(REST_PATH)
            .with(testAdminUser())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isOk());

    doThrow(new IllegalAccessException()).when(twitterService).deleteTwitterBearerToken(ADMIN_USER);

    response = mockMvc.perform(delete(REST_PATH)
            .with(testAdminUser())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));

    response.andExpect(status().isUnauthorized());


  }

  private RequestPostProcessor testAdminUser() {
    return user(ADMIN_USER).password(TEST_PASSWORD).authorities(new SimpleGrantedAuthority("rewarding"));
  }

  private RequestPostProcessor testSimpleUser() {
    return user(SIMPLE_USER).password(TEST_PASSWORD).authorities(new SimpleGrantedAuthority("users"));
  }

}
