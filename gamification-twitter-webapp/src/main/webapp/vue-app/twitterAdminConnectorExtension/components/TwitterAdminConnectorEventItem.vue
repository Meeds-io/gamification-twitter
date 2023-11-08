<template>
  <tr>
    <td class="ps-4 no-border-bottom">
      <gamification-admin-connector-event
        :event="event"
        class="py-2" />
    </td>
    <td class="no-border-bottom d-flex justify-center py-2">
      <div class="d-flex flex-column align-center">
        <v-switch
          v-model="enabled"
          :ripple="false"
          color="primary"
          class="connectorSwitcher my-auto"
          @change="enableDisableEvent" />
      </div>
    </td>
  </tr>
</template>

<script>
export default {
  props: {
    event: {
      type: Object,
      default: null
    },
    accountId: {
      type: String,
      default: null
    },
  },
  computed: {
    id() {
      return this.event?.id;
    },
    title() {
      return this.event?.title;
    },
    enabled() {
      const eventProperties = this.event?.properties;
      if (eventProperties && eventProperties[`${this.accountId}.enabled`]) {
        return eventProperties[`${this.accountId}.enabled`].toLowerCase() === 'true';
      }
      return true;
    },
  },
  methods: {
    enableDisableEvent() {
      this.$twitterConnectorService.saveEventStatus(this.id, this.accountId, !this.enabled);
    },
  }
};
</script>