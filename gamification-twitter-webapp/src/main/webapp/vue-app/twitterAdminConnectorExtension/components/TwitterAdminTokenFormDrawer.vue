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
    ref="twitterTokenFormDrawer"
    v-model="drawer"
    right
    @closed="clear">
    <template #title>
      {{ drawerTitle }}
    </template>
    <template v-if="drawer" #content>
      <v-form
        ref="twitterTokenForm"
        v-model="isValidForm"
        class="form-horizontal pa-6"
        flat
        @submit.prevent="saveAccountsSetting">
        <div class="pb-4 d-flex flex-column dark-grey-color">
          <v-card-text class="ps-0 py-0 dark-grey-color text-subtitle-2">
            {{ $t('twitterConnector.admin.label.form.noteOne') }}
            <a href="https://developer.twitter.com/portal" target="_blank">{{ $t('twitterConnector.admin.label.twitterAccessLevels') }}
              <v-icon size="14" class="pb-1 pe-1">fas fa-external-link-alt</v-icon>
            </a>
          </v-card-text>
          <v-card-text class="ps-0 py-0 pt-2 dark-grey-color text-subtitle-2" v-sanitized-html="$t('twitterConnector.admin.label.form.noteTwo')" />
          <v-card-text class="ps-0 py-0 pt-6 dark-grey-color font-weight-bold">
            {{ $t('twitterConnector.admin.label.tokenDrawer.stepOne') }}
          </v-card-text>
          <v-card-text class="ps-0 pb-0 dark-grey-color text-subtitle-2">
            {{ $t('twitterConnector.admin.label.tokenDrawer.stepOne.instructionsOne') }}
          </v-card-text>
          <v-card-text class="ps-0 py-0 dark-grey-color text-subtitle-2">
            {{ $t('twitterConnector.admin.label.tokenDrawer.stepOne.instructionsTwo') }}
            <a href="https://developer.twitter.com/portal" target="_blank">{{ $t('twitterConnector.admin.label.twitterDeveloperPortal') }}
              <v-icon size="14" class="pb-1 pe-1">fas fa-external-link-alt</v-icon>
            </a>
          </v-card-text>
          <v-card-text class="ps-0 pt-0 dark-grey-color text-subtitle-2">
            {{ $t('twitterConnector.admin.label.tokenDrawer.stepOne.instructionsThree') }}
          </v-card-text>
          <img
            class="align-self-center"
            src="/gamification-twitter/images/bearerToken.png"
            alt="SignUpFreeAccount"
            width="180">
          <v-card-text class="ps-0 dark-grey-color text-subtitle-2">
            {{ $t('twitterConnector.admin.label.tokenDrawer.stepOne.instructionsFour') }}
          </v-card-text>
          <v-text-field
            ref="bearerTokenInput"
            v-model="bearerTokenInput"
            :readonly="!isTokenEditing"
            :placeholder="$t('twitterConnector.admin.label.bearerToken.placeholder')"
            class="pa-0"
            type="text"
            outlined
            required
            dense
            @keyup.enter="handleToken">
            <template #append-outer>
              <v-slide-x-reverse-transition mode="out-in">
                <v-icon
                  :key="`icon-${isTokenEditing}`"
                  :color="isTokenEditing ? 'success' : 'info'"
                  :disabled="!bearerTokenInput && isTokenEditing"
                  class="text-header-title"
                  @click="editBearerToken"
                  v-text="isTokenEditing ? 'fas fa-check' : 'fas fa fa-edit'" />
              </v-slide-x-reverse-transition>
            </template>
          </v-text-field>
          <span v-if="isTokenEditing && bearerTokenInput" class="text-caption dark-grey-color">{{ $t('twitterConnector.admin.message.confirmBeforeProceeding') }} ↵</span>
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
          @click="saveAccountsSetting"
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
    isTokenEditing: false,
    bearerTokenInput: null,
    bearerTokenStored: false,
  }),
  created() {
    this.$root.$on('twitter-accounts-setting-drawer', this.open);
  },
  computed: {
    drawerTitle() {
      return this.bearerTokenStored ? this.$t('twitterConnector.admin.label.updateToken') : this.$t('twitterConnector.admin.label.addToken');
    },
    disabledSave() {
      return !this.bearerToken;
    },
  },
  methods: {
    open(bearerTokenStored) {
      if (bearerTokenStored) {
        this.bearerTokenInput = '*'.repeat(16);
        this.bearerTokenStored = true;
      } else {
        this.bearerTokenInput = null;
        this.isTokenEditing = true;
      }
      if (this.$refs.twitterTokenFormDrawer) {
        this.$refs.twitterTokenFormDrawer.open();
      }
    },
    close() {
      if (this.$refs.twitterTokenFormDrawer) {
        this.$refs.twitterTokenFormDrawer.close();
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
    saveAccountsSetting() {
      return this.$twitterConnectorService.saveBearerToken(this.bearerToken).then(() => {
        this.$root.$emit('twitter-bearer-token-updated');
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