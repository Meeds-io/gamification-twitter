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
package io.meeds.twitter.gamification.rest;

import io.meeds.twitter.gamification.model.TwitterAccount;
import io.meeds.twitter.gamification.rest.builder.TwitterAccountBuilder;
import io.meeds.twitter.gamification.rest.model.TwitterAccountRestEntity;
import io.meeds.twitter.gamification.service.TwitterConsumerService;
import io.meeds.twitter.gamification.service.TwitterService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.hateoas.EntityModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;

@RestController
@RequestMapping("accounts")
@Tag(name = "accounts", description = "Manage and access twitter watched accounts") // NOSONAR
public class TwitterAccountRest {

  @Autowired
  private TwitterService         twitterService;

  @Autowired
  private TwitterConsumerService twitterConsumerService;

  @GetMapping
  @Secured("users")
  @Operation(summary = "Retrieves the list of twitter watched accounts", method = "GET")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public PagedModel<EntityModel<TwitterAccountRestEntity>> getWatchedAccounts(HttpServletRequest request,
                                                                              Pageable pageable,
                                                                              PagedResourcesAssembler<TwitterAccountRestEntity> assembler) {

    try {
      Page<TwitterAccountRestEntity> twitterAccountRestEntities =
                                                                getTwitterAccountRestEntities(request.getRemoteUser(), pageable);
      return assembler.toModel(twitterAccountRestEntities);
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
    }
  }

  @GetMapping(path = "{accountId}")
  @Secured("users")
  @Operation(summary = "Retrieves a twitter watched account by its technical identifier", method = "GET")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "404", description = "Not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public TwitterAccount getWatchedAccountById(HttpServletRequest request,
                                              @Parameter(description = "Account technical identifier", required = true)
                                              @PathVariable("accountId")
                                              long accountId) {
    if (accountId == 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account Id must be not null");
    }
    try {
      return twitterService.getTwitterAccountById(accountId, request.getRemoteUser());
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    }
  }

  @PostMapping
  @Secured("users")
  @Operation(summary = "Create a watched Twitter account.", description = "Create a watched Twitter account.", method = "POST")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public void createWatchedAccount(HttpServletRequest request,
                                   @Parameter(description = "Twitter username", required = true)
                                   @RequestParam("twitterUsername")
                                   String twitterUsername) {

    if (StringUtils.isBlank(twitterUsername)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'twitterUsername' parameter is mandatory");
    }
    try {
      twitterService.addTwitterAccount(twitterUsername, request.getRemoteUser());
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
    } catch (ObjectAlreadyExistsException e) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    }
  }

  @DeleteMapping(path = "{accountId}")
  @Secured("users")
  @Operation(summary = "Deletes watched Twitter account.", description = "Deletes watched Twitter account.", method = "DELETE")
  @ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Request fulfilled"),
      @ApiResponse(responseCode = "400", description = "Bad request"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public void deleteWatchedAccount(HttpServletRequest request,
                                   @Parameter(description = "Twitter account id", required = true)
                                   @PathVariable("accountId")
                                   long accountId) {
    try {
      twitterService.deleteTwitterAccount(accountId, request.getRemoteUser());
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    }
  }

  private Page<TwitterAccountRestEntity> getTwitterAccountRestEntities(String username,
                                                                       Pageable pageable) throws IllegalAccessException {
    Page<TwitterAccount> twitterAccounts = twitterService.getTwitterAccounts(username, pageable);
    return TwitterAccountBuilder.toRestEntities(twitterService, twitterConsumerService, twitterAccounts);
  }
}
