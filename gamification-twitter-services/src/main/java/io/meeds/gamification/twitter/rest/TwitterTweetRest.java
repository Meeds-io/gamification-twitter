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

import java.util.List;
import java.util.Optional;


import io.meeds.gamification.twitter.rest.model.EntityList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import io.meeds.gamification.twitter.model.Tweet;
import io.meeds.gamification.twitter.service.TwitterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("twitter/tweets")
@Tag(name = "twitter/tweets", description = "Manage and access twitter watched tweets") // NOSONAR
public class TwitterTweetRest {

  @Autowired
  private TwitterService         twitterService;

  @GetMapping
  @Secured("users")
  @Operation(summary = "Retrieves the list of twitter watched tweet", method = "GET")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "401", description = "Unauthorized operation"), })
  public EntityList<Tweet> getWatchedTweets(@Parameter(description = "Query Offset", required = true) @RequestParam("offset") Optional<Integer> offset,
                                            @Parameter(description = "Query results limit", required = true) @RequestParam("limit") Optional<Integer> limit,
                                            @Parameter(description = "Watched tweet total size") @Schema(defaultValue = "false") @RequestParam("returnSize") boolean returnSize) {
    List<Tweet> tweet = twitterService.getTweets(offset.orElse(0), limit.orElse(0));
    EntityList<Tweet> tweetEntityList = new EntityList<>();
    tweetEntityList.setEntities(tweet);
    tweetEntityList.setOffset(offset.orElse(0));
    tweetEntityList.setLimit(limit.orElse(0));
    if (returnSize) {
      tweetEntityList.setSize(twitterService.countTweets());
    }
    return tweetEntityList;
  }
}
