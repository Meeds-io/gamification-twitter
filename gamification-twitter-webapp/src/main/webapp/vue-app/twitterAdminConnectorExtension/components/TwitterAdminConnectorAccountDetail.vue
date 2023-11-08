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