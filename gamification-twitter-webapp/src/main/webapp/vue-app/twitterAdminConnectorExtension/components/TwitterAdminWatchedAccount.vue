<!--
This file is part of the Meeds project (https://meeds.io/).

Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io

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
    flat>
    <div class="d-flex flex-row">
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
            <span class="text-truncate d-flex caption d-content pt-2px"> {{ watchedByLabel }} </span>
            <exo-user-avatar
              :profile-id="watchedBy"
              extra-class="ms-1"
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
    accountsLoaded: {
      type: Boolean,
      default: false
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
  }
};
</script>