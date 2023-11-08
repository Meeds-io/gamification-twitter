<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2020 - 2023 Meeds Association
contact@meeds.io
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
    <v-card class="pt-5 px-4" flat>
      <div class="d-flex flex-row">
        <div>
          <v-card-text class="px-0 py-0 dark-grey-color font-weight-bold">
            {{ $t('twitterConnector.admin.label.connectTwitter') }}
          </v-card-text>
          <v-card-text class="dark-grey-color px-0 pt-0">
            {{ $t('twitterConnector.admin.label.linkYourAccount') }}
          </v-card-text>
        </div>
        <v-spacer />
        <v-btn
          v-if="!emptyWatchedAccountList && bearerTokenStored"
          class="ma-auto"
          icon
          @click="addWatchedAccount">
          <v-icon class="mx-2 primary--text" size="20">fas fa-plus</v-icon>
        </v-btn>
        <v-tooltip
          v-if="bearerTokenStored"
          :disabled="$root.isMobile"
          bottom>
          <template #activator="{ on }">
            <v-btn
              v-if="bearerTokenStored"
              class="ma-auto"
              icon
              v-on="on"
              @click="addAccountsSetting">
              <v-icon class="mx-2 primary--text" size="20">fas fa-key</v-icon>
            </v-btn>
          </template>
          <span>{{ $t('twitterConnector.admin.label.bearerToken.tooltip') }}</span>
        </v-tooltip>
      </div>
      <div v-if="emptyWatchedAccountList && bearerTokenStored" class="d-flex align-center py-5">
        <v-btn
          class="btn btn-primary ma-auto"
          small
          @click="addWatchedAccount">
          <v-icon size="14" dark>
            fas fa-plus
          </v-icon>
          <span class="ms-2 subtitle-2 font-weight-bold">
            Add account
          </span>
        </v-btn>
      </div>
      <div v-if="!bearerTokenStored" class="d-flex align-center py-5">
        <v-btn
          class="btn btn-primary ma-auto"
          small
          @click="addAccountsSetting">
          <v-icon size="14" dark>
            fas fa-key
          </v-icon>
          <span class="ms-2 subtitle-2 font-weight-bold">
            {{ $t('twitterConnector.admin.label.addToken') }}
          </span>
        </v-btn>
      </div>
      <v-progress-linear
        v-show="loading"
        color="primary"
        height="2"
        indeterminate />
      <div
        v-for="account in watchedAccounts"
        :key="account.id"
        class="py-4 d-flex flex-column">
        <twitter-admin-watched-account
          class="full-height"
          :account="account"
          :token-status="tokenStatus" />
      </div>
      <template v-if="hasMore">
        <v-btn
          :loading="loading"
          class="btn pa-0 mb-5"
          text
          block
          @click="loadMore">
          {{ $t('twitterConnector.admin.label.loadMore') }}
        </v-btn>
      </template>
    </v-card>
  </div>
</template>
<script>

export default {
  props: {
    forceUpdate: {
      type: Boolean,
      default: false
    },
  },
  data() {
    return {
      showLoadMoreButton: false,
      watchedAccountsCount: 0,
      pageSize: 5,
      limit: 5,
      offset: 0,
      loading: true,
      watchedAccounts: [],
      bearerTokenStored: false,
      tokenStatus: {}
    };
  },
  computed: {
    hasMore() {
      return this.watchedAccountsCount > this.limit;
    },
    emptyWatchedAccountList() {
      return this.watchedAccounts?.length === 0;
    }
  },
  watch: {
    forceUpdate() {
      if (this.forceUpdate) {
        this.refreshWatchedAccount();
      }
    }
  },
  created() {
    this.$root.$on('twitter-accounts-updated', this.refreshWatchedAccount);
    this.$root.$on('twitter-bearer-token-updated', () => {
      this.bearerTokenStored = true;
      this.checkTwitterTokenStatus();
    });
    this.checkTwitterTokenStatus();
    this.refreshWatchedAccount();
  },
  methods: {
    checkTwitterTokenStatus() {
      this.loading = true;
      return this.$twitterConnectorService.checkTwitterTokenStatus()
        .then(data => {
          if (data?.isValid !== null) {
            this.$set(this.tokenStatus, 'isValid', data.isValid);
            this.$set(this.tokenStatus, 'reset', data.reset);
            this.$set(this.tokenStatus, 'remaining', data.remaining);
            this.bearerTokenStored = true;
          } else {
            this.$set(this.tokenStatus, 'isValid', null);
            this.$set(this.tokenStatus, 'reset', null);
            this.$set(this.tokenStatus, 'remaining', null);
            this.bearerTokenStored = false;
          }
        }).finally(() => this.loading = false);
    },
    refreshWatchedAccount() {
      this.loading = true;
      return this.$twitterConnectorService.getWatchedAccounts(this.offset, this.limit, this.forceUpdate)
        .then(data => {
          this.watchedAccounts = data.twitterAccountRestEntities;
          this.watchedAccountsCount = data.size || 0;
          return this.$nextTick()
            .then(() => {
              this.$emit('updated', this.watchedAccounts);
            });
        }).finally(() => this.loading = false);
    },
    addWatchedAccount() {
      this.$root.$emit('twitter-account-form-drawer');
    },
    addAccountsSetting() {
      this.$root.$emit('twitter-accounts-setting-drawer', this.bearerTokenStored);
    },
    loadMore() {
      this.limit += this.pageSize;
      this.refreshWatchedAccount();
    },
  }
};
</script>