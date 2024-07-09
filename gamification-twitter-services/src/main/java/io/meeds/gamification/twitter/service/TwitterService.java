/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2023 Meeds Lab contact@meeds.io
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
package io.meeds.gamification.twitter.service;

import io.meeds.gamification.twitter.model.Tweet;
import io.meeds.gamification.twitter.model.TwitterAccount;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;

import java.util.List;
import java.util.Set;

public interface TwitterService {

  /**
   * Get available watched twitter accounts using offset and limit.
   *
   * @param currentUser user name attempting to access watched twitter accounts
   * @param offset Offset of result
   * @param limit Limit of result
   * @param forceUpdate force Load remote accounts or not.
   * @return {@link List} of {@link TwitterAccount}
   * @throws IllegalAccessException when user is not authorized to access watched
   *           twitter accounts
   */
  List<TwitterAccount> getTwitterAccounts(String currentUser,
                                          int offset,
                                          int limit,
                                          boolean forceUpdate) throws IllegalAccessException;

  /**
   * Retrieves a watched twitter account identified by its technical identifier.
   *
   * @param accountId watched twitter account technical identifier
   * @return found {@link TwitterAccount}
   */
  TwitterAccount getTwitterAccountById(long accountId);

  /**
   * Retrieves a watched twitter account identified by its technical identifier
   * accessed by a user
   *
   * @param accountId watched twitter account technical identifier
   * @param username user name attempting to access watched twitter account
   * @return found {@link TwitterAccount}
   * @throws IllegalAccessException when user is not authorized to access watched
   *           twitter account
   * @throws ObjectNotFoundException twitter account not found
   */
  TwitterAccount getTwitterAccountById(long accountId, String username) throws IllegalAccessException, ObjectNotFoundException;

  /**
   * Get available watched twitter accounts using offset and limit.
   *
   * @param offset Offset of result
   * @param limit Limit of result
   * @return {@link List} of {@link TwitterAccount}
   */
  List<TwitterAccount> getTwitterAccounts(int offset, int limit);

  /**
   * Count all watched twitter accounts
   *
   * @param currentUser User name accessing watched twitter accounts
   * @return Watched twitter accounts count
   * @throws IllegalAccessException when user is not authorized to get watched
   *           twitter accounts
   */
  int countTwitterAccounts(String currentUser) throws IllegalAccessException;

  /**
   * Add watched Twitter account.
   *
   * @param twitterUsername Twitter username
   * @param currentUser user name attempting to add watched Twitter account.
   * @throws ObjectAlreadyExistsException when watched Twitter account already
   *           exists
   * @throws IllegalAccessException when user is not authorized to add watched
   *           Twitter account.
   * @throws ObjectNotFoundException when the Twitter account identified by its
   *           technical name is not found
   * @return {@link TwitterAccount}
   */
  TwitterAccount addTwitterAccount(String twitterUsername, String currentUser) throws ObjectAlreadyExistsException,
                                                                               IllegalAccessException,
                                                                               ObjectNotFoundException;

  /**
   * delete watched Twitter account
   *
   * @param twitterAccountId twitter remote account id
   * @param currentUser user name attempting to delete watched Twitter account
   * @throws IllegalAccessException when user is not authorized to delete the
   *           watched Twitter account
   */
  void deleteTwitterAccount(long twitterAccountId, String currentUser) throws IllegalAccessException, ObjectNotFoundException;

  /**
   * Add watched Tweet.
   *
   * @param tweetLink Tweet link
   * @return {@link TwitterAccount}
   */
  Tweet addTweetToWatch(String tweetLink);

  /**
   * Get available watched tweets using offset and limit.
   *
   * @param offset Offset of result
   * @param limit Limit of result
   * @return {@link List} of {@link Tweet}
   */
  List<Tweet> getTweets(int offset, int limit);

  /**
   * Count all watched tweets
   *
   * @return Watched tweets count
   */
  int countTweets();

  /**
   * Retrieves a watched tweet identified by its link
   *
   * @param tweetLink watched tweet link
   * @return found {@link TwitterAccount}
   */
  Tweet getTweetByLink(String tweetLink);

  /**
   * delete watched Tweet
   *
   * @param tweetId tweet Id
   */
  void deleteTweet(long tweetId) throws ObjectNotFoundException;

  void deleteTweetById(long tweetId);

  /**
   * Saves Twitter bearer token
   *
   * @param bearerToken twitter bearer token
   * @param currentUser user name attempting to save Twitter bearer token
   * @throws IllegalAccessException when user is not authorized save Twitter
   *           bearer token
   */
  void saveTwitterBearerToken(String bearerToken, String currentUser) throws IllegalAccessException;

  /**
   * Deletes Twitter bearer token
   *
   * @param currentUser user name attempting to delete Twitter bearer token
   * @throws IllegalAccessException when user is not authorized to delete Twitter
   *           bearer token
   */
  void deleteTwitterBearerToken(String currentUser) throws IllegalAccessException;

  /**
   * gets Twitter bearer token
   *
   * @param currentUser user name attempting to access Twitter bearer token
   * @throws IllegalAccessException when user is not authorized to access Twitter
   *           bearer token
   * @return Twitter bearer token
   */
  String getTwitterBearerToken(String currentUser) throws IllegalAccessException;

  /**
   * gets Twitter bearer token
   *
   * @return Twitter bearer token
   */
  String getTwitterBearerToken();

  /**
   * Update twitter account last mention tweet Id.
   *
   * @param accountId account Id
   * @param lastMentionTweetId last mention Tweet Id
   * @throws ObjectNotFoundException when the Twitter account identified by its
   *           technical name is not found
   */
  void updateAccountLastMentionTweetId(long accountId, long lastMentionTweetId) throws ObjectNotFoundException;

  /**
   * Update tweet with last reactions.
   *
   * @param tweetId tweetId
   * @param likers tweet likers
   * @param retweeters tweet retweeters
   * @throws ObjectNotFoundException when the tweet identified by its technical id
   *           is not found
   */
  void updateTweetReactions(long tweetId, Set<String> likers, Set<String> retweeters) throws ObjectNotFoundException;
}
