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

import io.meeds.twitter.gamification.model.TokenStatus;
import io.meeds.twitter.gamification.service.TwitterConsumerService;
import io.meeds.twitter.gamification.service.TwitterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("settings")
@Tag(name = "twitter/settings", description = "Manage and access twitter settings") // NOSONAR
public class TwitterSettingsRest {

  @Autowired
  private TwitterService         twitterService;

  @Autowired
  private TwitterConsumerService twitterConsumerService;

  @PostMapping
  @Secured("rewarding")
  @Operation(summary = "Saves a Twitter bearer token.", description = "Saves a Twitter bearer token.", method = "POST")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public void saveBearerToken(HttpServletRequest request,
                              @Parameter(description = "Twitter bearer token", required = true)
                              @RequestParam("bearerToken")
                              String bearerToken) {

    if (StringUtils.isBlank(bearerToken)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'bearerToken' parameter is mandatory");
    }
    try {
      twitterService.saveTwitterBearerToken(bearerToken, request.getRemoteUser());
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
    }
  }

  @DeleteMapping
  @Secured("rewarding")
  @Operation(summary = "Deletes Twitter bearer token.", description = "Deletes Twitter bearer token.", method = "DELETE")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public void deleteTwitterBearerToken(HttpServletRequest request) {
    try {
      twitterService.deleteTwitterBearerToken(request.getRemoteUser());
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
    }
  }

  @GetMapping
  @Secured("rewarding")
  @Operation(summary = "Checks if a twitter bearer token is stored", description = "This returns if twitter bearer token is stored or not", method = "GET")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "404", description = "Resource not found"),
      @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public TokenStatus checkTwitterTokenStatus(HttpServletRequest request) {
    try {
      String bearerToken = twitterService.getTwitterBearerToken(request.getRemoteUser());
      return twitterConsumerService.checkTwitterTokenStatus(bearerToken);
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
    }
  }
}
