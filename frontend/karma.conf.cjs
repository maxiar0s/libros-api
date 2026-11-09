module.exports = function (config) {
  config.set({
    frameworks: ['jasmine', '@angular/build'],
    plugins: [require('karma-jasmine'), require('karma-chrome-launcher'), require('@angular/build/plugins/karma')],
    browsers: ['ChromeHeadless'],
    customLaunchers: {
      ChromeHeadlessContainer: {
        base: 'ChromeHeadless',
        flags: ['--no-sandbox', '--disable-dev-shm-usage']
      }
    }
  });
};
