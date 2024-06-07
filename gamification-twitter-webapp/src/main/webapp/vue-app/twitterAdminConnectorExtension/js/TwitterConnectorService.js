/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2023 Meeds Association
 * contact@meeds.io
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
 */

export function checkTwitterTokenStatus() {
  return fetch('/gamification-twitter/rest/twitter/settings', {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting twitter bearer token status');
    }
  });
}

export function saveBearerToken(bearerToken) {
  const formData = new FormData();
  formData.append('bearerToken', bearerToken);
  return fetch('/gamification-twitter/rest/twitter/settings', {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: new URLSearchParams(formData).toString(),
  }).then((resp) => {
    if (!resp?.ok) {
      throw new Error('Error when saving twitter bearer token');
    }
  });
}

export function deleteTwitterBearerToken() {
  return fetch('/gamification-twitter/rest/twitter/settings', {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Error when deleting twitter bearer token');
    }
  });
}

export function addAccountToWatch(twitterUsername) {
  const formData = new FormData();
  formData.append('twitterUsername', twitterUsername);
  return fetch('/gamification-twitter/rest/twitter/accounts', {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: new URLSearchParams(formData).toString(),
  }).then(resp => {
    if (!resp?.ok) {
      if (resp.status === 404 || resp.status === 401) {
        return resp.text().then((text) => {
          throw new Error(text);
        });
      } else {
        throw new Error('Error when saving twitter account');
      }
    }
  });
}

export function deleteAccountToWatch(accountId) {
  return fetch(`/gamification-twitter/rest/twitter/accounts/${accountId}`, {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Error when deleting twitter account');
    }
  });
}

export function getWatchedAccounts(offset, limit) {
  return fetch(`/gamification-twitter/rest/twitter/accounts?offset=${offset || 0}&limit=${limit|| 10}&returnSize=true`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting twitter accounts');
    }
  });
}

export function getWatchedAccountById(accountId) {
  return fetch(`/gamification-twitter/rest/twitter/accounts/${accountId}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting twitter account');
    }
  });
}

export function getWatchedTweets(offset, limit) {
  return fetch(`/gamification-twitter/rest/twitter/tweets?offset=${offset || 0}&limit=${limit|| 10}&returnSize=true`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting watched tweet');
    }
  });
}


