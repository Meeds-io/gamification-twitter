const path = require('path');
const { merge } = require('webpack-merge');
const webpackCommonConfig = require('./webpack.common.js');

const config = merge(webpackCommonConfig, {
  mode: 'production',
  entry: {
    engagementCenterExtensions: './src/main/webapp/vue-app/engagementCenterExtensions/extensions.js',
    connectorExtensions: './src/main/webapp/vue-app/connectorExtensions/extensions.js',
    connectorEventExtensions: './src/main/webapp/vue-app/connectorEventExtensions/extensions.js',
    twitterUserConnectorExtension: './src/main/webapp/vue-app/twitterUserConnectorExtension/extension.js',
    twitterAdminConnectorExtension: './src/main/webapp/vue-app/twitterAdminConnectorExtension/extension.js'
  },
  output: {
    path: path.join(__dirname, 'target/gamification-twitter/'),
    filename: 'js/[name].bundle.js',
    libraryTarget: 'amd'
  }
});

module.exports = config;
