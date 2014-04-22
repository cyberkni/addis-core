'use strict';
define(['angular'], function() {
  var dependencies = ['$location', '$stateParams', '$q', 'ProblemResource', 'ScenarioResource'];
  var AnalysisService = function($location, $stateParams, $q, ProblemResource, ScenarioResource) {

    var analysisCache;

    var getDefaultScenario = function() {
      return ScenarioResource
        .query($stateParams)
        .$promise
        .then(function(scenarios) {
          return scenarios[0];
        });
    };

    var saveAnalysis = function(problem) {
      analysisCache.problem = problem;
      return analysisCache.$save();
    }

    var createProblem = function(analysis) {
      analysisCache = analysis;
      return ProblemResource.get($stateParams).$promise
        .then(saveAnalysis)
        .then(getDefaultScenario)
    };

    return {
      createProblem: createProblem,
      getDefaultScenario: getDefaultScenario
    };
  };
  return dependencies.concat(AnalysisService);
});