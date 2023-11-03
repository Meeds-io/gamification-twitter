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
  <v-app>
    <template>
      <div class="px-4 py-2 py-sm-5 d-flex align-center">
        <v-tooltip :disabled="$root.isMobile" bottom>
          <template #activator="{ on }">
            <v-card
              class="d-flex align-center"
              flat
              v-on="on"
              @click="backToConnectorDetail">
              <v-btn
                class="width-auto ms-n3"
                icon>
                <v-icon size="18" class="icon-default-color mx-2">fa-arrow-left</v-icon>
              </v-btn>
              <div class="text-header-title">{{ $t('twitterConnector.admin.label.configuration') }}</div>
            </v-card>
          </template>
          <span>{{ $t('gamification.connectors.settings.BackToDetail') }}</span>
        </v-tooltip>
        <v-spacer />
        <v-btn
          class="width-auto ms-n3"
          icon
          @click="forceUpdateAccounts">
          <v-icon size="18" class="icon-default-color mx-2">fas fa-redo-alt</v-icon>
        </v-btn>
      </div>
      <div class="d-flex flex-row px-4">
        <div>
          <v-card-text class="px-0 py-0 dark-grey-color font-weight-bold">
            {{ $t('twitterConnector.admin.label.connect') }}
          </v-card-text>
          <v-card-text class="dark-grey-color px-0 pt-0">
            {{ $t('twitterConnector.admin.label.allow.connect') }}
          </v-card-text>
        </div>
        <template v-if="connectionSettingStored">
          <v-spacer />
          <div class="d-flex flex-row">
            <v-switch
              v-model="enabled"
              color="primary"
              class="ma-auto"
              hide-details
              @change="saveConnectorSetting(enabled)" />
            <div class="d-flex align-center px-2">
              <v-btn
                icon
                @click="openConnectionSetting">
                <v-icon size="20">fas fa-edit</v-icon>
              </v-btn>
            </div>
            <div class="d-flex align-center">
              <v-btn
                class="ma-auto"
                icon
                @click="deleteConfirmDialog">
                <v-icon class="error-color" size="18">fas fa-trash-alt</v-icon>
              </v-btn>
            </div>
          </div>
        </template>
      </div>
      <template v-if="displayEnableButton">
        <div class="d-flex flex-column align-center py-5 px-4">
          <span class="subtitle-1 dark-grey-color">
            {{ $t('twitterConnector.admin.label.accountToWatch') }}
          </span>
          <span class="mb-2 subtitle-1 dark-grey-color">
            {{ $t('twitterConnector.admin.label.letEnableConnection') }}
          </span>
          <v-btn
            class="btn btn-primary"
            small
            @click="saveConnectorSetting(true)">
            {{ $t('twitterConnector.admin.label.enableConnection') }}
          </v-btn>
        </div>
      </template>
    </template>
    <template v-if="!connectionSettingStored">
      <div class="d-flex flex-column align-center py-5 px-4">
        <v-btn
          class="btn btn-primary ma-auto"
          small
          @click="openConnectionSetting">
          <v-icon size="14" dark>
            fas fa-cogs
          </v-icon>
          <span class="ms-2 subtitle-2 font-weight-bold">
            {{ $t('twitterConnector.admin.label.allowConnection') }}
          </span>
        </v-btn>
      </div>
    </template>
    <twitter-admin-watched-account-list :force-update="forceUpdate" />
    <twitter-admin-connection-setting-drawer ref="connectionSettingDrawer" />
    <twitter-admin-token-form-drawer />
    <twitter-admin-account-form-drawer />
    <exo-confirm-dialog
      ref="deleteConfirmDialog"
      :message="$t('gamification.connectors.message.confirmDeleteConnectorSetting')"
      :title="$t('gamification.connectors.title.confirmDeleteConnectorSetting')"
      :ok-label="$t('confirm.yes')"
      :cancel-label="$t('confirm.no')"
      @ok="deleteSettings" />
  </v-app>
</template>

<script>
export default {
  props: {
    apiKey: {
      type: String,
      default: ''
    },
    secretKey: {
      type: String,
      default: ''
    },
    redirectUrl: {
      type: String,
      default: ''
    },
    enabled: {
      type: Boolean,
      default: false
    },
  },
  data() {
    return {
      editing: false,
      displayHookDetail: false,
      twitterConnectorLinkBasePath: '/portal/g/:platform:rewarding/gamificationConnectorsAdministration#twitter',
      forceUpdate: false,
    };
  },
  computed: {
    disabledSave() {
      return !this.apiKey
          || !this.secretKey
          || !this.redirectUrl;
    },
    connectionSettingStored() {
      return this.apiKey && this.secretKey && this.redirectUrl;
    },
    displayEnableButton() {
      return this.connectionSettingStored && !this.enabled;
    },
  },
  created() {
    this.$root.$on('connector-settings-updated', (apiKey, secretKey, redirectUrl) => {
      if (!this.apiKey && !this.secretKey && !this.redirectUrl) {
        this.enabled = true;
      }
      this.apiKey = apiKey;
      this.secretKey = secretKey;
      this.redirectUrl = redirectUrl;
      this.saveConnectorSetting(this.enabled);
    });
  },
  methods: {
    saveConnectorSetting(status) {
      const settings = {
        name: 'twitter',
        apiKey: this.apiKey,
        secretKey: this.secretKey,
        redirectUrl: this.redirectUrl,
        enabled: status
      };
      this.editing = false;
      document.dispatchEvent(new CustomEvent('save-connector-settings', {detail: settings}));
    },
    openConnectionSetting() {
      this.$refs.connectionSettingDrawer.open(this.apiKey, this.secretKey, this.redirectUrl);
    },
    backToConnectorDetail() {
      document.dispatchEvent(new CustomEvent('close-connector-settings'));
    },
    deleteConfirmDialog() {
      this.$refs.deleteConfirmDialog.open();
    },
    deleteSettings() {
      this.apiKey = null;
      this.secretKey = null;
      this.redirectUrl = null;
      this.editing = true;
      document.dispatchEvent(new CustomEvent('delete-connector-settings', {detail: 'twitter'}));
    },
    forceUpdateAccounts() {
      this.forceUpdate = true;
    }
  }
};
</script>