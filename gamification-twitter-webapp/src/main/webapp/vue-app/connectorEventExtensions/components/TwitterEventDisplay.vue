<!--
 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Lab contact@meedslab.com

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->
<template>
  <div>
    <div class="subtitle-1 font-weight-bold mb-2">
      {{ header }}
    </div>
    <v-progress-linear
      v-if="loading"
      indeterminate
      color="primary"
      class="ms-3 my-auto" />
    <div v-if="widgetHtml" v-html="widgetHtml"></div>
    <div v-else class="d-flex">
      <div class="d-flex align-center">
        <v-img
          :src="avatarUrl"
          :key="avatarUrl"
          :alt="name"
          height="60"
          width="60"
          class="rounded" />
      </div>
      <v-list class="d-flex flex-column ms-3 py-0">
        <v-list-item-title class="align-self-start">
          {{ name }} ({{ identifier }})
        </v-list-item-title>
        <v-list-item-subtitle v-if="description" class="text-truncate d-flex caption mt-1">{{ description }}</v-list-item-subtitle>
      </v-list>
    </div>
  </div>
</template>


<script>
export default {
  props: {
    properties: {
      type: Object,
      default: null
    },
    trigger: {
      type: String,
      default: null
    },
  },
  data() {
    return {
      widgetHtml: '',
      loading: false,
      account: null,
    };
  },
  computed: {
    tweetLink() {
      return this.convertXtoTwitter(this.properties?.tweetLink);
    },
    accountId() {
      return this.properties?.accountId;
    },
    header() {
      return this.$t(`gamification.event.detail.display.${this.trigger}`);
    },
    avatarUrl() {
      return this.account?.avatarUrl;
    },
    name() {
      return this.account?.name;
    },
    identifier() {
      return this.account?.identifier;
    },
    description() {
      return this.account?.description;
    },
  },
  mounted() {
    if (this.trigger === 'mentionAccount') {
      this.getTwitterAccount();
    } else {
      this.loadTwitterWidget();
    }
  },

  methods: {
    loadTwitterWidget() {
      this.loading = true;
      // Check the trigger and set the appropriate HTML
      if (this.trigger !== 'mentionAccount') {
        this.widgetHtml = `<blockquote class="twitter-tweet"><a href="${this.tweetLink}"></a></blockquote>`;
        if (typeof twttr !== 'undefined') {
          /* eslint-disable no-undef */
          twttr.widgets.load();
          twttr.events.bind('loaded', () => {
            this.loading = false;
          });
        } else {
          setTimeout(() => {
            this.loadTwitterWidget();
          }, 500);
        }
      }
    },
    getTwitterAccount() {
      this.loading = true;
      return this.$twitterConnectorService.getWatchedAccounts({
        page: 0,
        size: 5,
      }).then(data => {
        this.account = data?._embedded?.twitterAccountRestEntityList.find(a => a.remoteId === this.accountId);
      })        .finally(() => this.loading = false);

    },
    convertXtoTwitter(url) {
      const xComRegex = /^https:\/\/x\.com\//;
      return url.replace(xComRegex, 'https://twitter.com/');
    }
  }
};
</script>
