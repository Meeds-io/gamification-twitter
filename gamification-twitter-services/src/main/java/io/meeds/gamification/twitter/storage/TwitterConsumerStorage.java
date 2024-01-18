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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.meeds.gamification.twitter.exception.TwitterConnectionException;
import io.meeds.gamification.twitter.model.RemoteTwitterAccount;
import io.meeds.gamification.twitter.model.TokenStatus;
import io.meeds.gamification.twitter.model.TwitterAccount;
import io.meeds.gamification.twitter.model.TwitterTrigger;
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

import static io.meeds.gamification.twitter.utils.Utils.MENTION_ACCOUNT_EVENT_NAME;

public class TwitterConsumerStorage {

  public static final String TWITTER_API_URL                = "https://api.twitter.com/2";

  public static final String BEARER                         = "Bearer ";

  public static final String AUTHORIZATION                  = "Authorization";

  public static final String TWITTER_CONNECTION_ERROR       = "twitter.connectionError";

  public static final String TWITTER_RETRIEVE_ACCOUNT_ERROR = "Unable to retrieve Twitter account info.";

  public static final String USERNAME                       = "username";

  public static final String USERS                          = "users";

  private HttpClient         client;

  public RemoteTwitterAccount retrieveTwitterAccount(String twitterUsername, String bearerToken) throws ObjectNotFoundException {
    URI uri = URI.create(TWITTER_API_URL + "/users/by/username/" + twitterUsername + "?user.fields=profile_image_url");
    String response;
    try {
      response = processGet(uri, bearerToken);
    } catch (TwitterConnectionException e) {
      throw new IllegalStateException(TWITTER_RETRIEVE_ACCOUNT_ERROR, e);
    }
    if (response == null) {
      throw new ObjectNotFoundException("twitter.accountNotFound");
    }
    Map<String, Object> resultMap = fromJsonStringToMap(response);
    RemoteTwitterAccount remoteTwitterAccount = new RemoteTwitterAccount();
    remoteTwitterAccount.setId(Long.parseLong(Objects.requireNonNull(extractSubItem(resultMap, "data", "id"))));
    remoteTwitterAccount.setName(extractSubItem(resultMap, "data", "name"));
    remoteTwitterAccount.setUsername(extractSubItem(resultMap, "data", USERNAME));
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
      throw new IllegalStateException(TWITTER_RETRIEVE_ACCOUNT_ERROR, e);
    }
    if (response == null) {
      return null;
    }
    Map<String, Object> resultMap = fromJsonStringToMap(response);
    RemoteTwitterAccount remoteTwitterAccount = new RemoteTwitterAccount();
    remoteTwitterAccount.setId(Long.parseLong(Objects.requireNonNull(extractSubItem(resultMap, "data", "id"))));
    remoteTwitterAccount.setName(extractSubItem(resultMap, "data", "name"));
    remoteTwitterAccount.setUsername(extractSubItem(resultMap, "data", USERNAME));
    remoteTwitterAccount.setDescription(extractSubItem(resultMap, "data", "description"));
    remoteTwitterAccount.setAvatarUrl(extractSubItem(resultMap, "data", "profile_image_url"));
    return remoteTwitterAccount;
  }

  public List<TwitterTrigger> getMentionEvents(TwitterAccount twitterAccount, long lastMentionTweetId, String bearerToken) {
    StringBuilder builder = new StringBuilder(TWITTER_API_URL);
    builder.append("/users/");
    builder.append(twitterAccount.getRemoteId());
    builder.append("/mentions?tweet.fields=conversation_id&expansions=author_id,entities.mentions.username&max_results=100");
    if (lastMentionTweetId > 0) {
      builder.append("&since_id=");
      builder.append(lastMentionTweetId);
    }
    URI uri = URI.create(builder.toString());
    String response;
    try {
      response = processGet(uri, bearerToken);
    } catch (TwitterConnectionException e) {
      throw new IllegalStateException(TWITTER_RETRIEVE_ACCOUNT_ERROR, e);
    }
    if (response == null) {
      return Collections.emptyList();
    }
    // Extract usernames from JSON using Jackson
    JsonNode rootNode = fromJsonStringToJsonNode(response);
    JsonNode usersNode = rootNode.path("includes").path(USERS);
    JsonNode dataNodes = rootNode.path("data");
    List<TwitterTrigger> twitterEvents = new ArrayList<>();
    for (JsonNode dataNode : dataNodes) {
      long tweetId = dataNode.path("id").asLong();
      long parentTweetId = dataNode.path("conversation_id").asLong();
      String text = dataNode.path("text").asText();
      if (tweetId != parentTweetId) {
        Pattern pattern = Pattern.compile("@" + twitterAccount.getIdentifier());
        Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
          count++;
        }
        if (count <= 1) {
          continue; // Skip if it is a default replay mention
        }
      }
      long userId = dataNode.path("author_id").asLong();
      String username = null;
      for (JsonNode userNode : usersNode) {
        if (userNode.has("id") && userNode.get("id").asLong() == userId) {
          username = userNode.path(USERNAME).asText();
          break; // Break the loop if the item is found
        }
      }
      TwitterTrigger twitterEvent =
                                  new TwitterTrigger(MENTION_ACCOUNT_EVENT_NAME, username, tweetId, "tweet", twitterAccount.getRemoteId());
      twitterEvents.add(twitterEvent);
    }
    return twitterEvents;
  }

  public TokenStatus checkTwitterTokenStatus(String bearerToken) {
    TokenStatus tokenStatus = new TokenStatus();
    if (StringUtils.isBlank(bearerToken)) {
      return tokenStatus;
    }
    URI uri = URI.create("https://api.twitter.com/1.1/application/rate_limit_status.json?resources=users");
    String response;
    HttpClient httpClient = getHttpClient();
    HttpGet request = new HttpGet(uri);
    request.setHeader(AUTHORIZATION, BEARER + bearerToken);
    HttpResponse httpResponse;
    try {
      httpResponse = httpClient.execute(request);
      boolean isSuccess = httpResponse != null
          && (httpResponse.getStatusLine().getStatusCode() >= 200 && httpResponse.getStatusLine().getStatusCode() < 300);
      if (isSuccess) {
        response = processSuccessResponse(httpResponse);
        Map<String, Object> resultMap = fromJsonStringToMap(response);
        String remaining = extractSubItem(resultMap, "resources", USERS, "/users/by/username/:username", "remaining");
        String reset = extractSubItem(resultMap, "resources", USERS, "/users/by/username/:username", "reset");
        tokenStatus.setIsValid(true);
        if (StringUtils.isNotBlank(remaining)) {
          tokenStatus.setRemaining(Long.parseLong(remaining));
        }
        if (StringUtils.isNotBlank(reset)) {
          tokenStatus.setReset(Long.parseLong(reset));
        }
        return tokenStatus;
      } else if (httpResponse != null
          && (httpResponse.getStatusLine().getStatusCode() == 401 || httpResponse.getStatusLine().getStatusCode() == 403)) {
        return new TokenStatus(false, null, null);
      } else {
        return null;
      }
    } catch (IOException e) {
      throw new IllegalStateException("Unable to retrieve Twitter bearer token status", e);
    }
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

  private JsonNode fromJsonStringToJsonNode(String jsonString) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readTree(jsonString);
    } catch (IOException e) {
      throw new IllegalStateException("Error converting JSON string to JsonNode: " + jsonString, e);
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
