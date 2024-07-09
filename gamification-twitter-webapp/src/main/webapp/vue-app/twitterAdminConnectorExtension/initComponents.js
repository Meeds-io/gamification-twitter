/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Lab contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
import TwitterAdminConnectorItem from './components/TwitterAdminConnectorItem.vue';
import TwitterAdminConnectionSettingDrawer from './components/TwitterAdminConnectionSettingDrawer.vue';
import TwitterAdminAccountFormDrawer from './components/TwitterAdminAccountFormDrawer.vue';
import TwitterAdminWatchedAccountList from './components/TwitterAdminWatchedAccountList.vue';
import TwitterAdminWatchedAccount from './components/TwitterAdminWatchedAccount.vue';
import TwitterAdminTokenFormDrawer from './components/TwitterAdminTokenFormDrawer.vue';
import TwitterAdminConnectorAccountDetail from './components/TwitterAdminConnectorAccountDetail.vue';
import TwitterAdminConnectorEventList from './components/TwitterAdminConnectorEventList.vue';
import TwitterAdminConnectorEventItem from './components/TwitterAdminConnectorEventItem.vue';

const components = {
  'twitter-admin-connector-item': TwitterAdminConnectorItem,
  'twitter-admin-connection-setting-drawer': TwitterAdminConnectionSettingDrawer,
  'twitter-admin-account-form-drawer': TwitterAdminAccountFormDrawer,
  'twitter-admin-token-form-drawer': TwitterAdminTokenFormDrawer,
  'twitter-admin-watched-account-list': TwitterAdminWatchedAccountList,
  'twitter-admin-watched-account': TwitterAdminWatchedAccount,
  'twitter-admin-watched-account-detail': TwitterAdminConnectorAccountDetail,
  'twitter-admin-event-list': TwitterAdminConnectorEventList,
  'twitter-admin-event-item': TwitterAdminConnectorEventItem,
};

for (const key in components) {
  Vue.component(key, components[key]);
}