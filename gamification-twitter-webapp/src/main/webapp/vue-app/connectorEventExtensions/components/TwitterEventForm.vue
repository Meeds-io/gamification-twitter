<!--
 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

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
  <v-app>
    <template v-if="isTriggerForAccount">
      <v-card-text class="px-0 dark-grey-color font-weight-bold">
        {{ $t('gamification.event.form.account') }}
      </v-card-text>
      <v-progress-circular
        v-if="loadingAccounts"
        indeterminate
        color="primary"
        size="20"
        class="ms-3 my-auto" />
      <v-chip-group
        v-model="value"
        :show-arrows="false"
        active-class="primary white--text"
        @change="selectAccount($event)">
        <twitter-connector-account-item
          v-for="account in accounts"
          :key="account.id"
          :account="account" />
      </v-chip-group>
    </template>
    <template v-else>
      <v-card-text class="px-0 dark-grey-color font-weight-bold">
        {{ $t('gamification.event.form.tweet') }}
      </v-card-text>
      <v-card-text class="ps-0 py-0">
        <input
          ref="tweetLink"
          v-model="tweetLink"
          placeholder="Enter the tweet link"
          type="text"
          class="ignore-vuetify-classes full-width"
          required
          @input="handleTweet"
          @change="checkTweetLink(tweetLink)">
      </v-card-text>
      <v-list-item-action-text  v-if="!isValidLink" class="d-flex py-0 me-0 me-sm-8">
        <span class="error--text">{{ $t('gamification.event.detail.invalidLink.error') }}</span>
      </v-list-item-action-text>
    </template>
  </v-app>
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
    }
  },
  data() {
    return {
      accounts: [],
      selected: null,
      value: null,
      loadingAccounts: true,
      tweetLink: null,
      startTypingKeywordTimeout: 0,
      startSearchAfterInMilliseconds: 300,
      endTypingKeywordTimeout: 50,
      isValidLink: true
    };
  },
  computed: {
    isTriggerForAccount() {
      return this.trigger === 'mentionAccount';
    }
  },
  watch: {
    trigger() {
      if (this.isTriggerForAccount) {
        this.retrieveAccounts();
      }
      this.tweetLink = null;
      this.isValidLink = true;
      this.selected = null;
      this.value = null;
      document.dispatchEvent(new CustomEvent('event-form-unfilled'));
    },
    isValidLink() {
      if (!this.isValidLink) {
        document.dispatchEvent(new CustomEvent('event-form-unfilled'));
      }
    },
  },
  created() {
    if (this.isTriggerForAccount) {
      this.retrieveAccounts();
    } else if (this.properties?.tweetLink){
      this.tweetLink = this.properties?.tweetLink;
    } else {
      document.dispatchEvent(new CustomEvent('event-form-unfilled'));
    }
  },
  methods: {
    retrieveAccounts() {
      this.loadingAccounts = true;
      return this.$twitterConnectorService.getWatchedAccounts()
        .then(data => {
          this.accounts = data.twitterAccountRestEntities;
        }).finally(() => {
          if (this.properties) {
            this.selected = this.accounts.find(a => a.remoteId === this.properties.accountId);
            this.value = this.accounts.indexOf(this.selected);
          } else if (this.accounts.length === 1) {
            this.selected = this.accounts[0];
            const eventProperties = {
              accountId: this.selected?.remoteId.toString()
            };
            document.dispatchEvent(new CustomEvent('event-form-filled', {detail: eventProperties}));
            this.value = this.accounts.indexOf(this.selected);
          } else {
            document.dispatchEvent(new CustomEvent('event-form-unfilled'));
          }
          this.loadingAccounts = false;
        });
    },
    selectAccount(value) {
      this.selected = this.accounts[value];
      if (this.selected) {
        const eventProperties = {
          accountId: this.selected?.remoteId.toString()
        };
        document.dispatchEvent(new CustomEvent('event-form-filled', {detail: eventProperties}));
      } else {
        document.dispatchEvent(new CustomEvent('event-form-unfilled'));
      }
    },
    handleTweet() {
      if (this.tweetLink) {
        this.startTypingKeywordTimeout = Date.now() + this.startSearchAfterInMilliseconds;
        if (!this.typing) {
          this.typing = true;
          this.waitForEndTyping();
        }
      }
    },
    waitForEndTyping() {
      window.setTimeout(() => {
        if (Date.now() > this.startTypingKeywordTimeout) {
          this.typing = false;
          if (this.checkTweetLink(this.tweetLink) && this.tweetLink !== this.properties?.tweetLink) {
            const eventProperties = {
              tweetLink: this.tweetLink
            };
            document.dispatchEvent(new CustomEvent('event-form-filled', {detail: eventProperties}));
          } else {
            document.dispatchEvent(new CustomEvent('event-form-unfilled'));
          }
        } else {
          this.waitForEndTyping();
        }
      }, this.endTypingKeywordTimeout);
    },
    checkTweetLink(tweetLink) {
      const tweetUrlRegex = /^https:\/\/twitter\.com\/[^/]+\/status\/\d+$/;
      this.isValidLink = tweetUrlRegex.test(tweetLink);
      return this.isValidLink;
    },
  }
};
</script>