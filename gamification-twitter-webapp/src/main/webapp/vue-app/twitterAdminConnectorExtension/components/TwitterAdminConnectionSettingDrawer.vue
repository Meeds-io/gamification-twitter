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
    ref="twitterConnectionSettingDrawer"
    v-model="drawer"
    right
    @opened="stepper = 1"
    @closed="clear">
    <template #title>
      {{ $t('twitterConnector.admin.label.enableConnectProfile') }}
    </template>
    <template v-if="drawer" #content>
      <v-form
        ref="OrganizationForm"
        v-model="isValidForm"
        class="form-horizontal pt-0 pb-4"
        flat
        @submit="saveConnectorSetting">
        <v-stepper
          v-model="stepper"
          class="ma-0 py-0 d-flex flex-column"
          vertical
          flat>
          <div class="flex-grow-1 flex-shrink-0">
            <v-stepper-step
              step="1"
              class="ma-0">
              <span class="font-weight-bold dark-grey-color text-subtitle-1">{{ $t('twitterConnector.admin.label.stepOne') }}</span>
            </v-stepper-step>
            <v-slide-y-transition>
              <div v-show="stepper === 1" class="px-6">
                <div class="pb-4 d-flex flex-column dark-grey-color">
                  <v-card-text class="ps-0 py-0 dark-grey-color">
                    {{ $t('twitterConnector.admin.label.stepOne.noteOne') }}
                  </v-card-text>
                  <v-card-text class="ps-0 pb-0 dark-grey-color">
                    {{ $t('twitterConnector.admin.label.stepOne.instructionsOne') }}
                  </v-card-text>
                  <v-card-text class="ps-0 py-0 dark-grey-color">
                    {{ $t('twitterConnector.admin.label.stepOne.instructionsTwo') }}
                    <a href="https://developer.twitter.com/portal" target="_blank">{{ $t('twitterConnector.admin.label.twitterDeveloperPortal') }}
                      <v-icon size="14" class="pb-1 pe-1">fas fa-external-link-alt</v-icon>
                    </a>
                  </v-card-text>
                  <v-card-text class="ps-0 pt-0 dark-grey-color">
                    {{ $t('twitterConnector.admin.label.stepOne.instructionsThree') }}
                  </v-card-text>
                  <img
                    class="align-self-center"
                    src="/gamification-twitter/images/SignUpFreeAccount.png"
                    alt="SignUpFreeAccount"
                    width="160">
                  <v-card-text class="ps-0 dark-grey-color">
                    {{ $t('twitterConnector.admin.label.stepOne.instructionsFour') }}
                  </v-card-text>
                  <img
                    class="align-self-center"
                    src="/gamification-twitter/images/signUpAgreement.png"
                    alt="SignUpAgreement"
                    width="160">
                </div>
              </div>
            </v-slide-y-transition>
          </div>
          <div class="flex-grow-1 flex-shrink-0">
            <v-stepper-step
              :complete="stepper > 2"
              step="2"
              class="ma-0">
              <span class="font-weight-bold dark-grey-color text-subtitle-1">{{ $t('twitterConnector.admin.label.stepTwo') }}</span>
            </v-stepper-step>
            <v-slide-y-transition>
              <div v-show="stepper === 2" class="px-6">
                <div class="pb-4 d-flex flex-column dark-grey-color">
                  <v-card-text class="ps-0 py-0 dark-grey-color">
                    {{ $t('twitterConnector.admin.label.stepTwo.instructionsOne') }}
                    <a href="https://developer.twitter.com/portal/dashboard" target="_blank">{{ $t('twitterConnector.admin.label.projectDashboard') }}
                      <v-icon size="14" class="pb-1 pe-1">fas fa-external-link-alt</v-icon>
                    </a>
                  </v-card-text>
                  <v-card-text class="ps-0 pt-0 dark-grey-color">
                    {{ $t('twitterConnector.admin.label.stepTwo.instructionsTwo') }}
                  </v-card-text>
                  <img
                    class="align-self-center"
                    src="/gamification-twitter/images/projectApp.png"
                    alt="ProjectApp"
                    width="250">
                  <v-card-text class="ps-0 dark-grey-color">
                    {{ $t('twitterConnector.admin.label.stepTwo.instructionsThree') }}
                  </v-card-text>
                  <img
                    class="align-self-center"
                    src="/gamification-twitter/images/userAuthenticationSettings.png"
                    alt="UserAuthenticationSettings"
                    width="250">
                  <v-card-text class="ps-0 pb-0 dark-grey-color" v-sanitized-html="$t('twitterConnector.admin.label.stepTwo.instructionsFour')" />
                  <v-card-text class="ps-0 py-0 dark-grey-color" v-sanitized-html="$t('twitterConnector.admin.label.stepTwo.instructionsFive')" />
                  <v-card-text class="ps-0 py-0 dark-grey-color">
                    {{ $t('twitterConnector.admin.label.stepTwo.instructionsSix') }}:
                  </v-card-text>
                  <v-card-text class="dark-grey-color pb-1">
                    {{ $t('twitterConnector.admin.label.redirectUrl') }}:
                  </v-card-text>
                  <div class="d-flex flex-row">
                    <v-text-field
                      :value="redirectUrl"
                      class="px-4 pt-0"
                      type="text"
                      outlined
                      disabled
                      dense />
                    <v-btn icon @click="copyText(redirectUrl)">
                      <v-icon>fas fa-copy</v-icon>
                    </v-btn>
                  </div>
                  <v-card-text class="dark-grey-color pb-1">
                    {{ $t('twitterConnector.admin.label.websiteUrl') }}:
                  </v-card-text>
                  <div class="d-flex flex-row">
                    <v-text-field
                      :value="currentUrl"
                      class="px-4 pt-0"
                      type="text"
                      outlined
                      disabled
                      dense />
                    <v-btn icon @click="copyText(currentUrl)">
                      <v-icon>fas fa-copy</v-icon>
                    </v-btn>
                  </div>
                </div>
              </div>
            </v-slide-y-transition>
          </div>
          <div class="flex-grow-1 flex-shrink-0">
            <v-stepper-step
              :complete="stepper > 3"
              step="3"
              class="ma-0">
              <span class="font-weight-bold dark-grey-color text-subtitle-1">{{ $t('twitterConnector.admin.label.stepThree') }}</span>
            </v-stepper-step>
            <v-slide-y-transition>
              <div v-show="stepper === 3" class="px-6">
                <div class="pb-4 d-flex flex-column dark-grey-color">
                  <v-card-text class="ps-0 py-0 pb-4 dark-grey-color">
                    {{ $t('twitterConnector.admin.label.stepThree.instructionsOne') }}
                  </v-card-text>
                  <v-card-text class="text-left ps-0 py-0 dark-grey-color">
                    {{ $t('twitterConnector.admin.label.clientId') }}
                  </v-card-text>
                  <v-card-text class="ps-0 pt-2">
                    <input
                      ref="connectorApiKey"
                      v-model="apiKey"
                      :placeholder="$t('twitterConnector.admin.label.clientId.placeholder')"
                      type="text"
                      class="ignore-vuetify-classes full-width"
                      required
                      @input="disabled = false"
                      @change="disabled = false">
                  </v-card-text>
                  <v-card-text class="text-left ps-0 py-0 dark-grey-color">
                    {{ $t('twitterConnector.admin.label.clientSecret') }}
                  </v-card-text>
                  <v-card-text class="ps-0 pt-2">
                    <input
                      ref="connectorSecretKey"
                      v-model="secretKey"
                      :placeholder="$t('twitterConnector.admin.label.clientSecret.placeholder')"
                      type="text"
                      class="ignore-vuetify-classes full-width"
                      required
                      @input="disabled = false"
                      @change="disabled = false">
                  </v-card-text>
                </div>
              </div>
            </v-slide-y-transition>
          </div>
        </v-stepper>
      </v-form>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          v-if="stepper === 2 || stepper === 3"
          class="btn me-2"
          @click="previousStep">
          {{ $t('twitterConnector.form.label.button.back') }}
        </v-btn>
        <v-btn
          v-else
          class="btn me-2"
          @click="close">
          {{ $t('twitterConnector.form.label.button.cancel') }}
        </v-btn>
        <v-btn
          v-if="stepper === 1 || stepper === 2"
          class="btn btn-primary"
          @click="nextStep">
          {{ $t('twitterConnector.form.label.button.next') }}
        </v-btn>
        <v-btn
          v-else
          :disabled="disabledSave"
          @click="saveConnectorSetting"
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
    apiKey: null,
    secretKey: null,
    isValidForm: false,
    drawer: false,
    disabled: true,
    stepper: 0,
    currentUrl: window.location.origin,
  }),
  created() {
    this.$root.$on('twitter-connection-setting-drawer', this.open);
  },
  computed: {
    disabledSave() {
      return this.disabled || !this.secretKey || !this.apiKey || !this.redirectUrl;
    },
    redirectUrl() {
      return `${window.location.origin}/portal/rest/gamification/connectors/oauthCallback/twitter`;
    }
  },
  methods: {
    open(apiKey, secretKey, redirectUrl) {
      this.apiKey = apiKey;
      this.secretKey = secretKey;
      this.redirectUrl = redirectUrl;
      if (this.$refs.twitterConnectionSettingDrawer) {
        this.$refs.twitterConnectionSettingDrawer.open();
      }
    },
    close() {
      if (this.$refs.twitterConnectionSettingDrawer) {
        this.$refs.twitterConnectionSettingDrawer.close();
      }
    },
    saveConnectorSetting() {
      this.$root.$emit('connector-settings-updated', this.apiKey, this.secretKey, this.redirectUrl);
      this.close();
    },
    clear() {
      this.apiKey = null;
      this.secretKey = null;
      this.disabled = true;
      this.stepper = 0;
    },
    previousStep() {
      this.stepper--;
      this.$forceUpdate();
    },
    nextStep(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      this.stepper++;
    },
    copyText(textToCopy) {
      const textArea = document.createElement('textarea');
      textArea.value = textToCopy;
      document.body.appendChild(textArea);
      textArea.select();
      document.execCommand('copy');
      document.body.removeChild(textArea);
      this.$root.$emit('alert-message', this.$t('rules.menu.linkCopied'), 'info');
    },
  }
};
</script>