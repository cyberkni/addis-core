'use strict';
define(['underscore'], function() {
  var dependencies = ['$scope', '$stateParams', '$state', '$q', '$window',
    'OutcomeResource', 'InterventionResource',
    'Select2UtilService', 'TrialverseStudyResource', 'ProblemResource', 'AnalysisService', 'DEFAULT_VIEW'
  ];
  var SingleStudyBenefitRiskAnalysisController = function($scope, $stateParams, $state, $q, $window,
    OutcomeResource, InterventionResource,
    Select2UtilService, TrialverseStudyResource, ProblemResource, AnalysisService, DEFAULT_VIEW) {

    var projectIdParam = {
      projectId: $stateParams.projectId
    };

    var outcomes = OutcomeResource.query(projectIdParam);
    var interventions = InterventionResource.query(projectIdParam);
    var userIsOwner;

    var initialiseOutcomes = function(outcomes) {
      $scope.outcomes = outcomes;
      $scope.selectedOutcomeIds = Select2UtilService.objectsToIds($scope.analysis.selectedOutcomes);
      $scope.$watchCollection('selectedOutcomeIds', function(newValue) {
        if (newValue.length !== $scope.analysis.selectedOutcomes.length) {
          $scope.analysis.selectedOutcomes = Select2UtilService.idsToObjects($scope.selectedOutcomeIds, $scope.outcomes);
          $scope.isValidAnalysis = AnalysisService.validateAnalysis($scope.analysis);
          $scope.errorMessage = {};
          $scope.analysis.$save();
        }
      });
    };

    var initialiseInterventions = function(interventions) {
      $scope.interventions = interventions;
      $scope.selectedInterventionIds = Select2UtilService.objectsToIds($scope.analysis.selectedInterventions);
      $scope.$watchCollection('selectedInterventionIds', function(newValue) {
        if (newValue.length !== $scope.analysis.selectedInterventions.length) {
          $scope.analysis.selectedInterventions = Select2UtilService.idsToObjects($scope.selectedInterventionIds, $scope.interventions);
          $scope.isValidAnalysis = AnalysisService.validateAnalysis($scope.analysis);
          $scope.errorMessage = {};
          $scope.analysis.$save();
        }
      });
    };

    $scope.analysis = $scope.$parent.analysis;
    $scope.project = $scope.$parent.project;
    $scope.$parent.loading = {
      loaded: false
    };
    $scope.editMode = {
      disableEditing: false
    };
    $scope.isProblemDefined = false;
    $scope.isValidAnalysis = false;
    $scope.errorMessage = {};

    $q.all($scope.analysis, $scope.project).then(function() {

      $scope.$parent.loading.loaded = true;
      $scope.isValidAnalysis = AnalysisService.validateAnalysis($scope.analysis);
      userIsOwner = $window.config.user.id === $scope.project.owner.id;
      if ($scope.analysis.problem) {
        $scope.isProblemDefined = true;
      }
      $scope.editMode.disableEditing = !userIsOwner || $scope.isProblemDefined;

      $scope.select2Options = {
        'readonly': $scope.editMode.disableEditing
      };

      //  angular ui bug work-around, select2-ui does not properly watch for changes in the select2-options 
      $('#criteriaSelect').select2('readonly', $scope.editMode.disableEditing);
      $('#interventionsSelect').select2('readonly', $scope.editMode.disableEditing);

      $scope.studies = TrialverseStudyResource.query({
        id: $scope.project.trialverseId
      });

      outcomes.$promise.then(initialiseOutcomes);
      interventions.$promise.then(initialiseInterventions);

      $scope.$watch('analysis.studyId', function(newValue, oldValue) {
        if (oldValue !== newValue) {
          $scope.isValidAnalysis = AnalysisService.validateAnalysis($scope.analysis);
          $scope.errorMessage = {};
          $scope.analysis.$save();
        }
      });

      $scope.goToDefaultScenarioView = function() {
        AnalysisService
          .getDefaultScenario()
          .then(function(scenario) {
            $state.go(DEFAULT_VIEW, {
              scenarioId: scenario.id
            });
          });
      };
      $scope.createProblem = function() {
        AnalysisService.getProblem($scope.analysis)
          .then(function(problem) {
            if (AnalysisService.validateProblem($scope.analysis, problem)) {
              $scope.analysis.problem = problem;
              $scope.analysis.$save()
                .then(AnalysisService.getDefaultScenario)
                .then(function(scenario) {
                  $state.go(DEFAULT_VIEW, {
                    scenarioId: scenario.id
                  });
                });
            } else {
              $scope.errorMessage = {
                text: 'The selected study and the selected citeria/alternatives do not match.'
              };
            }
          });
      };
    });


  };
  return dependencies.concat(SingleStudyBenefitRiskAnalysisController);
});