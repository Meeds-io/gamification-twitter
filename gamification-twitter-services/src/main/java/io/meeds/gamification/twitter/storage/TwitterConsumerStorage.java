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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package io.meeds.gamification.twitter.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.meeds.gamification.twitter.exception.TwitterConnectionException;
import io.meeds.gamification.twitter.model.RemoteTwitterAccount;
import io.meeds.gamification.twitter.model.TwitterAccount;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import org.exoplatform.commons.exception.ObjectNotFoundException;

public class TwitterConsumerStorage {

  public static final String TWITTER_API_URL          = "https://api.twitter.com/2";

  public static final String BEARER                   = "Bearer ";

  public static final String AUTHORIZATION            = "Authorization";

  public static final String TWITTER_CONNECTION_ERROR = "twitter.connectionError";

  private HttpClient         client;

  public RemoteTwitterAccount retrieveTwitterAccount(String twitterUsername, String bearerToken) throws ObjectNotFoundException {
    URI uri = URI.create(TWITTER_API_URL + "/users/by/username/" + twitterUsername + "?user.fields=profile_image_url");
    String response;
    try {
      response = processGet(uri, bearerToken);
    } catch (TwitterConnectionException e) {
      throw new IllegalStateException("Unable to retrieve Twitter account info.", e);
    }
    if (response == null) {
      throw new ObjectNotFoundException("twitter.accountNotFound");
    }
    Map<String, Object> resultMap = fromJsonStringToMap(response);
    RemoteTwitterAccount remoteTwitterAccount = new RemoteTwitterAccount();
    remoteTwitterAccount.setId(Long.parseLong(Objects.requireNonNull(extractSubItem(resultMap, "data", "id"))));
    remoteTwitterAccount.setName(extractSubItem(resultMap, "data", "name"));
    remoteTwitterAccount.setUsername(extractSubItem(resultMap, "data", "username"));
    remoteTwitterAccount.setDescription(extractSubItem(resultMap, "data", "description"));
    remoteTwitterAccount.setAvatarUrl(extractSubItem(resultMap, "data", "profile_image_url"));
    return remoteTwitterAccount;
  }

  public RemoteTwitterAccount retrieveTwitterAccount(long twitterRemoteId, String bearerToken) {
    URI uri = URI.create(TWITTER_API_URL + "/users/" + twitterRemoteId + "?user.fields=profile_image_url,description");
    String response;
    try {
      response = processGet(uri, bearerToken);
    } catch (TwitterConnectionException e) {
      throw new IllegalStateException("Unable to retrieve Twitter account info.", e);
    }
    if (response == null) {
      return null;
    }
    Map<String, Object> resultMap = fromJsonStringToMap(response);
    RemoteTwitterAccount remoteTwitterAccount = new RemoteTwitterAccount();
    remoteTwitterAccount.setId(Long.parseLong(Objects.requireNonNull(extractSubItem(resultMap, "data", "id"))));
    remoteTwitterAccount.setName(extractSubItem(resultMap, "data", "name"));
    remoteTwitterAccount.setUsername(extractSubItem(resultMap, "data", "username"));
    remoteTwitterAccount.setDescription(extractSubItem(resultMap, "data", "description"));
    remoteTwitterAccount.setAvatarUrl(extractSubItem(resultMap, "data", "profile_image_url"));
    return remoteTwitterAccount;
  }

  private String processGet(URI uri, String bearerToken) throws TwitterConnectionException {
    HttpClient httpClient = getHttpClient();
    HttpGet request = new HttpGet(uri);
    try {
      request.setHeader(AUTHORIZATION, BEARER + bearerToken);
      return processRequest(httpClient, request);
    } catch (IOException e) {
      throw new TwitterConnectionException(TWITTER_CONNECTION_ERROR, e);
    }
  }

  private String processRequest(HttpClient httpClient, HttpRequestBase request) throws IOException, TwitterConnectionException {
    HttpResponse response = httpClient.execute(request);
    boolean isSuccess = response != null
        && (response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 300);
    if (isSuccess) {
      return processSuccessResponse(response);
    } else if (response != null && response.getStatusLine().getStatusCode() == 404) {
      return null;
    } else {
      processErrorResponse(response);
      return null;
    }
  }

  private String processSuccessResponse(HttpResponse response) throws IOException {
    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
      return String.valueOf(HttpStatus.SC_NO_CONTENT);
    } else if ((response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED
        || response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) && response.getEntity() != null
        && response.getEntity().getContentLength() != 0) {
      try (InputStream is = response.getEntity().getContent()) {
        return IOUtils.toString(is, StandardCharsets.UTF_8);
      }
    } else {
      return null;
    }
  }

  private void processErrorResponse(HttpResponse response) throws TwitterConnectionException, IOException {
    if (response == null) {
      throw new TwitterConnectionException("Error when connecting twitter");
    } else if (response.getEntity() != null) {
      try (InputStream is = response.getEntity().getContent()) {
        String errorMessage = IOUtils.toString(is, StandardCharsets.UTF_8);
        if (StringUtils.contains(errorMessage, "")) {
          throw new TwitterConnectionException(errorMessage);
        } else {
          throw new TwitterConnectionException(TWITTER_CONNECTION_ERROR + errorMessage);
        }
      }
    } else {
      throw new TwitterConnectionException(TWITTER_CONNECTION_ERROR + response.getStatusLine().getStatusCode());
    }
  }

  private HttpClient getHttpClient() {
    if (client == null) {
      RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
      HttpClientConnectionManager clientConnectionManager = getClientConnectionManager();
      HttpClientBuilder httpClientBuilder = HttpClients.custom()
                                                       .setDefaultRequestConfig(requestConfig)
                                                       .setDefaultCookieStore(new BasicCookieStore())
                                                       .setConnectionManager(clientConnectionManager)
                                                       .setConnectionReuseStrategy(new DefaultConnectionReuseStrategy());
      client = httpClientBuilder.build();
    }
    return client;
  }

  private HttpClientConnectionManager getClientConnectionManager() {
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    connectionManager.setDefaultMaxPerRoute(10);
    return connectionManager;
  }

  public void clearCache() { // NOSONAR
    // implemented in cached storage
  }

  public void clearCache(TwitterAccount twitterAccount, String bearerToken) {
    // implemented in cached storage
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> fromJsonStringToMap(String jsonString) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readValue(jsonString, Map.class);
    } catch (IOException e) {
      throw new IllegalStateException("Error converting JSON string to map: " + jsonString, e);
    }
  }

  @SuppressWarnings("unchecked")
  private String extractSubItem(Map<String, Object> map, String... keys) {
    Object currentObject = map;
    for (String key : keys) {
      if (currentObject instanceof Map) {
        currentObject = ((Map<String, Object>) currentObject).get(key);
      } else {
        return null;
      }
    }
    return currentObject != null ? currentObject.toString() : null;
  }
}
