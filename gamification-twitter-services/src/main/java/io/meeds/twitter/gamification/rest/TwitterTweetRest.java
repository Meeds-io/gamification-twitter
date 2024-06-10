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

import io.meeds.twitter.gamification.model.Tweet;
import io.meeds.twitter.gamification.service.TwitterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("tweets")
@Tag(name = "twitter/tweets", description = "Manage and access twitter watched tweets") // NOSONAR
public class TwitterTweetRest {

  @Autowired
  private TwitterService twitterService;

  @GetMapping
  @Secured("users")
  @Operation(summary = "Retrieves the list of twitter watched tweet", method = "GET")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "401", description = "Unauthorized operation"), })
  public PagedModel<EntityModel<Tweet>> getWatchedTweets(Pageable pageable, PagedResourcesAssembler<Tweet> assembler) {
    Page<Tweet> tweets = twitterService.getTweets(pageable);
    return assembler.toModel(tweets);
  }
}
