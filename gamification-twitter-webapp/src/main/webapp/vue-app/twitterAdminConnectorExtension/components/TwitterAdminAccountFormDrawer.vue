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
  <exo-drawer
    ref="twitterAccountFormDrawer"
    v-model="drawer"
    right
    @closed="clear">
    <template #title>
      {{ $t('twitterConnector.admin.label.accountDrawer.title') }}
    </template>
    <template v-if="drawer" #content>
      <v-form
        ref="twitterTokenForm"
        v-model="isValidForm"
        class="form-horizontal pa-6"
        flat
        @submit.prevent="addAccountToWatch">
        <div class="pb-4 d-flex flex-column dark-grey-color">
          <v-card-text class="ps-0 py-0 dark-grey-color text-subtitle-2">
            {{ $t('twitterConnector.admin.label.form.noteOne') }}
            <a href="https://developer.twitter.com/portal" target="_blank">{{ $t('twitterConnector.admin.label.twitterAccessLevels') }}
              <v-icon size="14" class="pb-1 pe-1">fas fa-external-link-alt</v-icon>
            </a>
          </v-card-text>
          <v-card-text class="ps-0 py-0 pt-2 dark-grey-color text-subtitle-2" v-sanitized-html="$t('twitterConnector.admin.label.form.noteTwo')" />
          <v-card-text class="ps-0 dark-grey-color text-subtitle-2">
            {{ $t('twitterConnector.admin.label.accountToWatch') }}
          </v-card-text>
          <v-card-text class="d-flex py-0 ps-0">
            <input
              ref="organizationNameInput"
              v-model="twitterUsername"
              :placeholder="$t('twitterConnector.admin.label.accountToWatch.placeholder')"
              type="text"
              class="ignore-vuetify-classes flex-grow-1"
              maxlength="2000"
              required>
          </v-card-text>
        </div>
      </v-form>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          class="btn me-2"
          @click="close">
          {{ $t('twitterConnector.form.label.button.cancel') }}
        </v-btn>
        <v-btn
          :disabled="disabledSave"
          @click="addAccountToWatch"
          class="btn btn-primary">
          {{ $t('twitterConnector.form.label.button.save') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>

export default {
  data: () => ({
    bearerToken: null,
    isValidForm: false,
    drawer: false,
    twitterUsername: null,
  }),
  created() {
    this.$root.$on('twitter-account-form-drawer', this.open);
  },
  computed: {
    drawerTitle() {
      return this.bearerTokenStored ? this.$t('twitterConnector.admin.label.updateToken') : this.$t('twitterConnector.admin.label.addToken');
    },
    disabledSave() {
      return !this.twitterUsername;
    },
  },
  methods: {
    open() {
      if (this.$refs.twitterAccountFormDrawer) {
        this.$refs.twitterAccountFormDrawer.open();
      }
    },
    close() {
      if (this.$refs.twitterAccountFormDrawer) {
        this.$refs.twitterAccountFormDrawer.close();
      }
    },
    editBearerToken() {
      if (this.isTokenEditing) {
        this.bearerToken = this.bearerTokenInput;
        this.bearerTokenInput = '*'.repeat(16);
        this.isTokenEditing = false;
      } else {
        this.bearerTokenInput = null;
        this.bearerToken = null;
        this.isTokenEditing = true;
        this.$nextTick(() => {
          const $input = this.$refs['bearerTokenInput'];
          if ($input) {
            $input.focus();
          }
        });
      }
    },
    handleToken() {
      if (this.bearerTokenInput) {
        this.bearerToken = this.bearerTokenInput;
        this.bearerTokenInput = '*'.repeat(16);
        this.isTokenEditing = false;
      }
    },
    addAccountToWatch() {
      return this.$twitterConnectorService.addAccountToWatch(this.twitterUsername).then(() => {
        this.$root.$emit('twitter-accounts-updated');
        this.close();
      });
    },
    clear() {
      this.bearerToken = null;
      this.isTokenEditing = false;
    },
  }
};
</script>