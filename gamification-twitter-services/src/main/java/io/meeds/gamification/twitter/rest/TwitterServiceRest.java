/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * 
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.gamification.twitter.rest;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import io.meeds.gamification.twitter.model.TokenStatus;
import io.meeds.gamification.twitter.model.Tweet;
import io.meeds.gamification.twitter.model.TwitterAccount;
import io.meeds.gamification.twitter.rest.builder.TwitterAccountBuilder;
import io.meeds.gamification.twitter.rest.model.EntityList;
import io.meeds.gamification.twitter.rest.model.TwitterAccountRestEntity;
import io.meeds.gamification.twitter.service.TwitterService;
import io.meeds.gamification.twitter.service.TwitterConsumerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;

import java.util.Collection;
import java.util.List;

import static io.meeds.gamification.utils.Utils.getCurrentUser;

@Path("/gamification/connectors/twitter")
public class TwitterServiceRest implements ResourceContainer {

  public static final String           TWITTER_ACCOUNT_NOT_FOUND = "The TWitter account doesn't exit";

  private final TwitterService         twitterService;

  private final TwitterConsumerService twitterConsumerService;

  public TwitterServiceRest(TwitterService twitterAccountService, TwitterConsumerService twitterConsumerService) {
    this.twitterService = twitterAccountService;
    this.twitterConsumerService = twitterConsumerService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("account")
  @RolesAllowed("users")
  @Operation(summary = "Retrieves the list of twitter watched accounts", method = "GET")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "401", description = "Unauthorized operation"), })
  public Response getWatchedAccounts(@QueryParam("offset") int offset,
                                     @Parameter(description = "Query results limit", required = true) @QueryParam("limit") int limit,
                                     @Parameter(description = "Force update accounts") @Schema(defaultValue = "false") @QueryParam("forceUpdate") boolean forceUpdate,
                                     @Parameter(description = "Watched accounts total size") @Schema(defaultValue = "false") @QueryParam("returnSize") boolean returnSize) {

    String currentUser = getCurrentUser();
    List<TwitterAccountRestEntity> twitterAccountRestEntities;
    try {
      EntityList<TwitterAccountRestEntity> accountEntityList = new EntityList<>();
      twitterAccountRestEntities = getTwitterAccountRestEntities(currentUser, offset, limit, forceUpdate);
      accountEntityList.setEntities(twitterAccountRestEntities);
      accountEntityList.setOffset(offset);
      accountEntityList.setLimit(limit);
      if (returnSize) {
        int twitterAccountsSize = twitterService.countTwitterAccounts(currentUser);
        accountEntityList.setSize(twitterAccountsSize);
      }
      return Response.ok(accountEntityList).build();
    } catch (IllegalAccessException e) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("account/{accountId}")
  @RolesAllowed("users")
  @Operation(summary = "Retrieves a twitter watched account by its technical identifier", method = "GET")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "404", description = "Not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public Response getWatchedAccountById(@Parameter(description = "Account technical identifier", required = true) @PathParam("accountId") long accountId) {
    if (accountId == 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Account Id must be not null").build();
    }
    String currentUser = getCurrentUser();
    try {
      TwitterAccount twitterAccount = twitterService.getTwitterAccountById(accountId, currentUser);
      return Response.ok(twitterAccount).build();
    } catch (IllegalArgumentException e) {
      return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
    } catch (IllegalAccessException e) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
    } catch (ObjectNotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
    }
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Path("account")
  @RolesAllowed("users")
  @Operation(summary = "Create a watched Twitter account.", description = "Create a watched Twitter account.", method = "POST")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response createWatchedAccount(@Parameter(description = "Twitter username", required = true) @FormParam("twitterUsername") String twitterUsername) {

    if (StringUtils.isBlank(twitterUsername)) {
      return Response.status(Response.Status.BAD_REQUEST).entity("'twitterUsername' parameter is mandatory").build();
    }
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    try {
      twitterService.addTwitterAccount(twitterUsername, currentUser);
      return Response.status(Response.Status.CREATED).build();
    } catch (IllegalAccessException e) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
    } catch (ObjectAlreadyExistsException e) {
      return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
    } catch (ObjectNotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
    }
  }

  @DELETE
  @Path("account/{accountId}")
  @RolesAllowed("users")
  @Operation(summary = "Deletes watched Twitter account.", description = "Deletes watched Twitter account.", method = "DELETE")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Request fulfilled"),
      @ApiResponse(responseCode = "400", description = "Bad request"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public Response deleteWatchedAccount(@Parameter(description = "Twitter account id", required = true) @PathParam("accountId") long accountId) {
    if (accountId <= 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity("'accountId' parameter is mandatory").build();
    }
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    try {
      twitterService.deleteTwitterAccount(accountId, currentUser);
      return Response.noContent().build();
    } catch (IllegalAccessException e) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
    } catch (ObjectNotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND).entity(TWITTER_ACCOUNT_NOT_FOUND).build();
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("tweet")
  @RolesAllowed("users")
  @Operation(summary = "Retrieves the list of twitter watched tweet", method = "GET")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "401", description = "Unauthorized operation"), })
  public Response getWatchedTweets(@QueryParam("offset") int offset,
                                   @Parameter(description = "Query results limit", required = true) @QueryParam("limit") int limit,
                                   @Parameter(description = "Watched tweet total size") @Schema(defaultValue = "false") @QueryParam("returnSize") boolean returnSize) {
    List<Tweet> tweet = twitterService.getTweets(offset, limit);
    EntityList<Tweet> tweetEntityList = new EntityList<>();
    tweetEntityList.setEntities(tweet);
    tweetEntityList.setOffset(offset);
    tweetEntityList.setLimit(limit);
    if (returnSize) {
      tweetEntityList.setSize(twitterService.countTweets());
    }
    return Response.ok(tweetEntityList).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Path("/bearerToken")
  @RolesAllowed("users")
  @Operation(summary = "Saves a Twitter bearer token.", description = "Saves a Twitter bearer token.", method = "POST")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response saveBearerToken(@Parameter(description = "Twitter bearer token", required = true) @FormParam("bearerToken") String bearerToken) {

    if (StringUtils.isBlank(bearerToken)) {
      return Response.status(Response.Status.BAD_REQUEST).entity("'bearerToken' parameter is mandatory").build();
    }
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    try {
      twitterService.saveTwitterBearerToken(bearerToken, currentUser);
      return Response.status(Response.Status.CREATED).build();
    } catch (IllegalAccessException e) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
    }
  }

  @DELETE
  @Path("/bearerToken")
  @RolesAllowed("users")
  @Operation(summary = "Deletes Twitter bearer token.", description = "Deletes Twitter bearer token.", method = "DELETE")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
          @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public Response deleteTwitterBearerToken() {
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    try {
      twitterService.deleteTwitterBearerToken(currentUser);
      return Response.noContent().build();
    } catch (IllegalAccessException e) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/bearerToken")
  @RolesAllowed("users")
  @Operation(summary = "Checks if a twitter bearer token is stored", description = "This returns if twitter bearer token is stored or not", method = "GET")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "404", description = "Resource not found"),
      @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response checkTwitterTokenStatus() {
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    try {
      String bearerToken = twitterService.getTwitterBearerToken(currentUser);
      TokenStatus tokenStatus = twitterConsumerService.checkTwitterTokenStatus(bearerToken);
      return Response.ok(tokenStatus).build();
    } catch (IllegalAccessException e) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
    }
  }

  private List<TwitterAccountRestEntity> getTwitterAccountRestEntities(String username, int offset, int limit, boolean forceUpdate) throws IllegalAccessException {
    Collection<TwitterAccount> twitterAccounts = twitterService.getTwitterAccounts(username, offset, limit, forceUpdate);
    return TwitterAccountBuilder.toRestEntities(twitterService, twitterConsumerService, twitterAccounts);
  }
}
