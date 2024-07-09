<!--
This file is part of the Meeds project (https://meeds.io/).

Copyright (C) 2020 - 2023 Meeds Lab contact@meeds.io

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
  <div flat class="disabled-background">
    <div class="py-2 px-4 py-sm-3 d-flex align-center white mb-sm-4">
      <v-tooltip :disabled="$root.isMobile" bottom>
        <template #activator="{ on }">
          <v-card
            class="d-flex align-center"
            flat
            v-on="on"
            @click="backToSetting">
            <v-btn
              class="width-auto ms-n3"
              icon>
              <v-icon size="18" class="icon-default-color mx-2">fa-arrow-left</v-icon>
            </v-btn>
            <div class="text-header-title">{{ name }}</div>
          </v-card>
        </template>
        <span>{{ $t('gamification.connectors.settings.BackToDetail') }}</span>
      </v-tooltip>
    </div>
    <twitter-admin-event-list :account="account" />
  </div>
</template>
<script>

export default {
  props: {
    account: {
      type: Object,
      default: null
    },
  },
  data() {
    return {
      selectedTab: 'Events',
    };
  },
  computed: {
    name() {
      return this.account?.name;
    },
  },
  methods: {
    backToSetting() {
      this.$emit('close');
    },
  }
};
</script>