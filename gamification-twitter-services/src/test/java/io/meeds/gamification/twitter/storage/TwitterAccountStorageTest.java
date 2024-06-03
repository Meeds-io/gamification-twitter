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
package io.meeds.gamification.twitter.storage;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import io.meeds.gamification.twitter.dao.TwitterAccountDAO;
import io.meeds.gamification.twitter.entity.TwitterAccountEntity;
import io.meeds.gamification.twitter.model.TwitterAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@SpringBootTest(classes = { TwitterAccountStorage.class, })
@ExtendWith(MockitoExtension.class)
class TwitterAccountStorageTest {

  private static final Long     ID        = 2L;

  private static final Long     REMOTE_ID = 1232L;

  @Autowired
  private TwitterAccountStorage twitterAccountStorage;

  @MockBean
  private TwitterAccountDAO     twitterAccountDAO;

  @BeforeEach
  void setup() {
    when(twitterAccountDAO.save(any())).thenAnswer(invocation -> {
      TwitterAccountEntity entity = invocation.getArgument(0);
      if (entity.getId() == null) {
        entity.setId(ID);
      }
      when(twitterAccountDAO.findById(ID)).thenReturn(Optional.of(entity));
      when(twitterAccountDAO.findTwitterAccountEntityByRemoteId(REMOTE_ID)).thenReturn(entity);
      PageRequest pageable = PageRequest.of(Math.toIntExact(0 / 10), 10, Sort.by(Sort.Direction.ASC, "id"));
      when(twitterAccountDAO.findAll(pageable)).thenReturn(new PageImpl<>(List.of(entity)));
      when(twitterAccountDAO.count()).thenReturn(1L);
      return entity;
    });
    doAnswer(invocation -> {
      TwitterAccountEntity entity = invocation.getArgument(0);
      when(twitterAccountDAO.findById(entity.getId())).thenReturn(Optional.empty());
      return null;
    }).when(twitterAccountDAO).delete(any());
  }

  @Test
  void testAddTwitterAccount() throws Exception {
    // Given
    TwitterAccount twitterAccount = createTwitterAccountInstance();

    // When
    TwitterAccount createdTwitterAccount = twitterAccountStorage.addTwitterAccount(twitterAccount);

    // Then
    assertNotNull(createdTwitterAccount);
    assertEquals(twitterAccount.getName(), createdTwitterAccount.getName());
    assertEquals(twitterAccount.getIdentifier(), createdTwitterAccount.getIdentifier());
    assertEquals(twitterAccount.getRemoteId(), createdTwitterAccount.getRemoteId());

    assertThrows(ObjectAlreadyExistsException.class, () -> twitterAccountStorage.addTwitterAccount(twitterAccount));
  }

  @Test
  void testGetTwitterAccounts() throws Exception {
    // Given
    TwitterAccount twitterAccount = createTwitterAccountInstance();

    // When
    TwitterAccount createdTwitterAccount = twitterAccountStorage.addTwitterAccount(twitterAccount);

    // Then
    assertNotNull(createdTwitterAccount);
    assertEquals(List.of(createdTwitterAccount), twitterAccountStorage.getTwitterAccounts(0, 10));
    assertEquals(1L, twitterAccountStorage.countTwitterAccounts());
  }

  @Test
  void testUpdateAccountLastMentionTweetId() throws Exception {
    // Given
    TwitterAccount twitterAccount = createTwitterAccountInstance();

    // When
    TwitterAccount account = twitterAccountStorage.updateAccountLastMentionTweetId(10L, 122121L);

    // Then
    assertNull(account);

    // When
    TwitterAccount createdTwitterAccount = twitterAccountStorage.addTwitterAccount(twitterAccount);
    account = twitterAccountStorage.updateAccountLastMentionTweetId(createdTwitterAccount.getId(), 122121L);

    assertNotNull(account);
    assertEquals(createdTwitterAccount.getIdentifier(), account.getIdentifier());
    assertEquals(createdTwitterAccount.getRemoteId(), account.getRemoteId());
    assertEquals(122121, account.getLastMentionTweetId());
  }

  @Test
  void testGetTwitterAccountById() throws Exception {
    // Given
    TwitterAccount createdTwitterAccount = twitterAccountStorage.addTwitterAccount(createTwitterAccountInstance());

    // When
    TwitterAccount twitterAccount = twitterAccountStorage.getTwitterAccountById(createdTwitterAccount.getId());

    // Then
    assertNotNull(twitterAccount);
    assertEquals(createdTwitterAccount.getName(), twitterAccount.getName());
    assertEquals(createdTwitterAccount.getIdentifier(), twitterAccount.getIdentifier());
    assertEquals(createdTwitterAccount.getRemoteId(), twitterAccount.getRemoteId());
  }

  @Test
  void testDeleteTwitterAccount() throws Exception {
    // Given
    TwitterAccount createdTwitterAccount = twitterAccountStorage.addTwitterAccount(createTwitterAccountInstance());

    // When
    TwitterAccount twitterAccount = twitterAccountStorage.deleteTwitterAccount(createdTwitterAccount.getId());

    // Then
    assertNotNull(twitterAccount);
  }

  @Test
  void testGetTwitterAccountByRemoteId() throws Exception {
    // Given
    TwitterAccount createdTwitterAccount = twitterAccountStorage.addTwitterAccount(createTwitterAccountInstance());

    // When
    TwitterAccount twitterAccount = twitterAccountStorage.getTwitterAccountByRemoteId(createdTwitterAccount.getRemoteId());

    // Then
    assertNotNull(twitterAccount);
    assertEquals(createdTwitterAccount.getName(), twitterAccount.getName());
    assertEquals(createdTwitterAccount.getIdentifier(), twitterAccount.getIdentifier());
    assertEquals(createdTwitterAccount.getRemoteId(), twitterAccount.getRemoteId());
  }

  protected TwitterAccount createTwitterAccountInstance() {
    TwitterAccount twitterAccount = new TwitterAccount();
    twitterAccount.setIdentifier("twitterIdentifier");
    twitterAccount.setName("twitterName");
    twitterAccount.setRemoteId(REMOTE_ID);
    return twitterAccount;
  }
}
