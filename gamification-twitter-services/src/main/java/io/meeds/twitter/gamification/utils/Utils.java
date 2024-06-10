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
package io.meeds.twitter.gamification.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

  public static final String CONNECTOR_NAME             = "twitter";

  public static final String MENTION_ACCOUNT_EVENT_NAME = "mentionAccount";

  public static final String LIKE_TWEET_EVENT_NAME      = "likeTweet";

  public static final String RETWEET_TWEET_EVENT_NAME   = "retweet";

  public static final String ACCOUNT_ID                 = "accountId";

  public static final String TWEET_ID                   = "tweetId";

  public static final String TWEET_LINK                 = "tweetLink";

  private Utils() {
    // Private constructor for Utils class
  }

  public static String extractTweetId(String tweetUrl) {
    Pattern pattern = Pattern.compile("/status/(\\d+)");
    Matcher matcher = pattern.matcher(tweetUrl);
    if (matcher.find()) {
      return matcher.group(1);
    } else {
      return null;
    }
  }

  public static Map<String, String> stringToMap(String mapAsString) {
    Map<String, String> map = new HashMap<>();
    mapAsString = mapAsString.substring(1, mapAsString.length() - 1);
    String[] pairs = mapAsString.split(", ");
    for (String pair : pairs) {
      String[] keyValue = pair.split(": ");
      String key = keyValue[0].trim();
      String value = keyValue[1].trim();
      map.put(key, value);
    }
    return map;
  }
}
