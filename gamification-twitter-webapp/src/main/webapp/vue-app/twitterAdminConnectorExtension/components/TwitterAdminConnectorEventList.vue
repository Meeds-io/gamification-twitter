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
  <v-card flat>
    <v-subheader class="px-0">
      <v-card-text class="text-color text-subtitle-1 ps-4 py-2">{{ $t('gamification.label.events.placeholder') }}</v-card-text>
      <v-spacer />
      <v-card
        width="220"
        max-width="100%"
        flat>
        <v-text-field
          v-model="keyword"
          :placeholder="$t('gamification.label.filter.filterEvents')"
          prepend-inner-icon="fa-filter icon-default-color"
          clear-icon="fa-times fa-1x"
          class="pa-0 me-3 my-auto"
          clearable
          hide-details />
      </v-card>
    </v-subheader>
    <v-data-table
      :headers="eventsHeaders"
      :items="eventToDisplay"
      :options.sync="options"
      :server-items-length="pageSize"
      :show-rows-border="false"
      :loading="loading"
      mobile-breakpoint="0"
      hide-default-footer
      disable-sort>
      <template slot="item" slot-scope="props">
        <twitter-admin-event-item :event="props.item" :account-id="accountRemoteId" />
      </template>
    </v-data-table>
    <div v-if="hasMoreEvents" class="d-flex justify-center py-4">
      <v-btn
        :loading="loading"
        min-width="95%"
        class="btn"
        text
        @click="loadMore">
        {{ $t('rules.loadMore') }}
      </v-btn>
    </div>
  </v-card>
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
      options: {
        page: 1,
        itemsPerPage: 10,
      },
      events: [],
      eventsSize: 0,
      pageSize: 10,
      loading: true,
      keyword: ''
    };
  },
  computed: {
    accountRemoteId() {
      return this.account?.remoteId;
    },
    eventsHeaders() {
      return [
        {text: this.$t('twitterConnector.label.event'), align: 'start', width: '80%' , class: 'dark-grey-color text-font-size-0'},
        {text: this.$t('twitterConnector.label.status'), align: 'center', width: '20%', class: 'dark-grey-color text-font-size'},];
    },
    hasMoreEvents() {
      return this.keyword ? this.sortedEvent.length > this.pageSize : this.eventsSize > this.pageSize;
    },
    sortedEvent() {
      let filteredEvent = this.events;
      if (this.keyword) {
        filteredEvent = this.events.filter(item =>
          this.getEventLabel(item).toLowerCase().includes(this.keyword.toLowerCase())
        );
      }
      return filteredEvent.sort((a, b) => this.getEventLabel(a).localeCompare(b.title));
    },
    eventToDisplay() {
      return this.sortedEvent.slice(0, this.pageSize);
    },
  },
  created() {
    this.retrieveAccountEvents();
  },
  methods: {
    retrieveAccountEvents() {
      this.$gamificationConnectorService.getEvents('twitter')
        .then(data => {
          this.events = data.entities;
          this.eventsSize = data.size;
        })
        .finally(() => this.loading = false);
    },
    loadMore() {
      this.pageSize += this.pageSize;
      this.retrieveAccountEvents();
    },
    getEventLabel(event) {
      return this.$t(`gamification.event.title.${event.title}`);
    }
  }
};
</script>