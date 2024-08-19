<!--
This file is part of the Meeds project (https://meeds.io/).

Copyright (C) 2020 - 2023 Meeds Lab contact@meedslab.com

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
  <v-card
    flat
    v-on="isValidToken && !rateLimitReached ? { click: openAccountDetail } : {}">
    <div class="d-flex flex-row" :class="!isValidToken && 'filter-blur-3'">
      <div class="d-flex">
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
          <div class="d-flex flex-row">
            <span class="text-truncate d-flex caption d-content"> {{ watchedByLabel }} </span>
            <exo-user-avatar
              :profile-id="watchedBy"
              extra-class="ms-1 align-self-center"
              fullname
              popover />
          </div>
        </v-list>
      </div>
      <v-spacer />
      <div class="d-flex align-center">
        <v-btn
          icon
          @click="deleteConfirmDialog">
          <v-icon class="error-color mx-2" size="20">fas fa-trash-alt</v-icon>
        </v-btn>
      </div>
    </div>
    <v-overlay
      :value="!isValidToken"
      absolute
      opacity="0.7"
      class="d-flex position-absolute height-auto width-auto">
      <div class="d-flex flex-row">
        <div class="d-flex flex-column me-5">
          <span class="text-h6">{{ $t('twitterConnector.label.tokenExpiredOrInvalid') }}</span>
          <span class="text-h6">{{ $t('twitterConnector.label.regenerateAnotherToken') }}</span>
        </div>
      </div>
    </v-overlay>
    <v-overlay
      v-if="isValidToken && rateLimitReached"
      :value="isValidToken && rateLimitReached"
      absolute
      opacity="0.7"
      class="d-flex position-absolute height-auto width-auto">
      <div class="d-flex flex-row">
        <div class="d-flex flex-column me-5">
          <span class="text-h6">{{ $t('twitterConnector.label.tokenRateLimitReached') }}</span>
          <span class="text-h6">{{ $t('twitterConnector.label.youNeedToWait') }} {{ formatTime(timeUntilReset) }}</span>
        </div>
      </div>
    </v-overlay>
    <exo-confirm-dialog
      ref="deleteAccountConfirmDialog"
      :message="$t('twitterConnector.admin.message.confirmDeleteAccount')"
      :title="$t('twitterConnector.admin.title.confirmDeleteAccount')"
      :ok-label="$t('confirm.yes')"
      :cancel-label="$t('confirm.no')"
      @ok="deleteAccountToWatch" />
  </v-card>
</template>

<script>

export default {
  props: {
    account: {
      type: Object,
      default: null
    },
    tokenStatus: {
      type: Object,
      default: null
    },
  },
  data() {
    return {
      loading: true,
      timeUntilReset: 0
    };
  },
  computed: {
    name() {
      return this.account?.name;
    },
    identifier() {
      return this.account?.identifier;
    },
    accountId() {
      return this.account?.id;
    },
    description() {
      return this.account?.description;
    },
    watchedDate() {
      return this.account?.watchedDate && new Date(this.account.watchedDate);
    },
    avatarUrl() {
      return this.account?.avatarUrl;
    },
    watchedByLabel() {
      return this.$t('twitterConnector.admin.label.watchedBy', {0: this.$dateUtil.formatDateObjectToDisplay(this.watchedDate, {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
      }, eXo.env.portal.language)});
    },
    watchedBy() {
      return this.account?.watchedBy;
    },
    isValidToken() {
      return this.tokenStatus?.isValid;
    },
    tokenRemaining() {
      return this.tokenStatus?.remaining;
    },
    tokenResetTime() {
      return this.tokenStatus?.reset;
    },
    rateLimitReached() {
      return this.tokenRemaining < 0;
    }
  },
  created() {
    this.timeUntilReset = this.tokenResetTime - Math.floor(Date.now() / 1000); // Initialize the timer
    setInterval(() => {
      if (this.timeUntilReset > 0) {
        this.timeUntilReset--;
      }
    }, 1000);
  },
  methods: {
    deleteConfirmDialog(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      this.$refs.deleteAccountConfirmDialog.open();
    },
    deleteAccountToWatch() {
      return this.$twitterConnectorService.deleteAccountToWatch(this.accountId).then(() => {
        this.$root.$emit('twitter-accounts-updated');
      });
    },
    formatTime(seconds) {
      const hours = Math.floor(seconds / 3600);
      const minutes = Math.floor((seconds % 3600) / 60);
      const remainingSeconds = seconds % 60;
      return `${hours}:${minutes}:${remainingSeconds}`;
    },
    openAccountDetail() {
      this.$root.$emit('twitter-account-detail', this.account);
    },
  }
};
</script>