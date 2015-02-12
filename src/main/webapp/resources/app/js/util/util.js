'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.util', [])
    // services
    .factory('UUIDService', require('util/uuidService'))
    .factory('JsonLdService', require('util/jsonLdService'))
    .factory('RdfStoreService', require('util/rdfstoreService'))
    .factory('RemoteRdfStoreService', require('util/remoteRdfStoreService'))
    .factory('DurationService', require('util/durationService'))
    .factory('SubsetSelectService', require('util/directives/subsetSelect/subsetSelectService'))

    // resources
    .factory('SparqlResource', require('util/sparqlResource'))

    // filters
    .filter('ontologyFilter', require('util/filters/ontologyFilter'))
    .filter('durationFilter', require('util/filters/durationFilter'))
    .filter('stripFrontFilter', require('util/filters/stripFrontFilter'))
    .filter('anchorFilter', require('util/filters/anchorFilter'))

    //directives
    .directive('navbarDirective', require('util/directives/navbar/navbarDirective'))
    .directive('subsetSelect', require('util/directives/subsetSelect/subsetSelectDirective'))
    .directive('durationInput', require('util/directives/durationInput/durationInputDirective'))
    ;
});
