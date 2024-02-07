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
export default {
  name: 'twitter',
  title: 'twitterConnector.label.profile',
  description: 'twitterConnector.label.description',
  image: '/gamification-twitter/images/TwitterX.svg',
  imageCard: '/gamification-twitter/images/TwitterXWhite.svg',
  initialized: true,
  identifier: '',
  user: '',
  rank: 20,
  PROFILE_BASER_URL: 'https://twitter.com',
  openOauthPopup() {
    // Construct the Twitter OAuth URL with the oauth_token
    const authUrl =`${window.location.origin}/portal/twitterOauth`;
    const width = 600;
    const height = 600;
    const left = window.innerWidth / 2 - width / 2;
    const top = window.innerHeight / 2 - height / 2;
    return window.open(authUrl, 'Twitter OAuth', `width=${width}, height=${height}, left=${left}, top=${top}`);
  },
};
