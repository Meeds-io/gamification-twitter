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
package io.meeds.twitter.gamification.plugin;

import io.meeds.twitter.gamification.utils.Utils;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = { TwitterConnectorPlugin.class, })
public class TwitterConnectorPluginTest {

  @Test
  public void testValidateToken() {
    // When
    TwitterConnectorPlugin twitterConnectorPlugin = new TwitterConnectorPlugin();

    // Then
    assertEquals(Utils.CONNECTOR_NAME, twitterConnectorPlugin.getConnectorName());
    assertEquals(Utils.CONNECTOR_NAME, twitterConnectorPlugin.getName());
  }
}
