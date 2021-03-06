'use strict';
define([], function() {
  var dependencies = ['$scope', '$q', '$state', '$window', '$stateParams','currentAnalysis', 'currentProject', 'OutcomeResource',
   'InterventionResource', 'ModelResource', 'NetworkMetaAnalysisService', 'TrialverseTrialDataResource'];

  var NetworkMetaAnalysisContainerController = function($scope, $q,  $state, $window, $stateParams, currentAnalysis, currentProject,
   OutcomeResource, InterventionResource, ModelResource, NetworkMetaAnalysisService, TrialverseTrialDataResource) {

    $scope.isAnalysisLocked = true;
    $scope.isNetworkDisconnected = true;
    $scope.hasModel = true;
    $scope.tableHasAmbiguousArm = false;
    $scope.hasLessThanTwoInterventions = false;
    $scope.analysis = currentAnalysis;
    $scope.project = currentProject;
    $scope.networkGraph = {};
    $scope.trialData = {};
    $scope.loading = {
      loaded: true
    };

    $scope.models = ModelResource.query({
      projectId: $stateParams.projectId,
      analysisId: $stateParams.analysisId
    });

    $scope.outcomes = OutcomeResource.query({
      projectId: $stateParams.projectId
    });

    $scope.interventions = InterventionResource.query({
      projectId: $stateParams.projectId
    });

    $q
      .all([
        $scope.analysis.$promise,
        $scope.project.$promise,
        $scope.models.$promise,
        $scope.outcomes.$promise,
        $scope.interventions.$promise
      ])
      .then(function() {
        $scope.hasModel = $scope.models.length > 0;
        $scope.interventions = NetworkMetaAnalysisService.addInclusionsToInterventions($scope.interventions, $scope.analysis.includedInterventions);
        $scope.analysis.outcome = _.find($scope.outcomes, $scope.matchOutcome);
        if ($scope.analysis.outcome) {
          $scope.reloadModel();
        }
      });

    $scope.gotoCreateModel = function() {
      $state.go('createModel',
        {
          projectId: $stateParams.projectId,
          analysisId: $stateParams.analysisId
        });
    }

    $scope.lessThanTwoInterventionArms = function(dataRow) {
      var matchedAndIncludedRows = _.filter(dataRow.studyRows, function(studyRow) {
        return studyRow.intervention !== 'unmatched' && studyRow.included;
      });
      var matchedInterventions = _.uniq(_.pluck(matchedAndIncludedRows, 'intervention'));
      return matchedInterventions.length < 2;
    };

    $scope.editMode = {
      isUserOwner: $window.config.user.id === $scope.project.owner.id
    };
    $scope.editMode.disableEditing = !$scope.editMode.isUserOwner;

    $scope.matchOutcome = function matchOutcome(outcome) {
      return $scope.analysis.outcome && $scope.analysis.outcome.id === outcome.id;
    };

    $scope.doesInterventionHaveAmbiguousArms = function(drugId, studyUid) {
      return NetworkMetaAnalysisService.doesInterventionHaveAmbiguousArms(drugId, studyUid, $scope.trialverseData, $scope.analysis);
    };

    $scope.reloadModel = function reloadModel() {
      var includedInterventionUris = _.reduce($scope.interventions, addIncludedInterventionUri, []);
      TrialverseTrialDataResource
        .get({
          namespaceUid: $scope.project.namespaceUid,
          outcomeUri: $scope.analysis.outcome.semanticOutcomeUri,
          interventionUris: includedInterventionUris,
          version: $scope.project.datasetVersion
        })
        .$promise
        .then(function(trialverseData) {
          $scope.trialverseData = trialverseData;
          updateNetwork();
          var includedInterventions = getIncludedInterventions($scope.interventions);
          $scope.trialData = NetworkMetaAnalysisService.transformTrialDataToTableRows(trialverseData, includedInterventions, $scope.analysis.excludedArms);
          $scope.tableHasAmbiguousArm = NetworkMetaAnalysisService.doesModelHaveAmbiguousArms($scope.trialverseData, $scope.interventions, $scope.analysis);
          $scope.hasLessThanTwoInterventions = getIncludedInterventions($scope.interventions).length < 2;
          $scope.isModelCreationBlocked = checkCanNotCreateModel();
        });
    }

    $scope.changeInterventionInclusion = function(intervention) {
      $scope.analysis.includedInterventions = NetworkMetaAnalysisService.buildInterventionInclusions($scope.interventions, $scope.analysis);
      if ($scope.trialverseData && !intervention.isIncluded) {
        $scope.analysis.excludedArms = NetworkMetaAnalysisService.cleanUpExcludedArms(intervention, $scope.analysis, $scope.trialverseData);
      }
      $scope.analysis.$save(function() {
        $scope.analysis.outcome = _.find($scope.outcomes, $scope.matchOutcome);
        $scope.tableHasAmbiguousArm = NetworkMetaAnalysisService.doesModelHaveAmbiguousArms($scope.trialverseData, $scope.interventions, $scope.analysis);
        $scope.reloadModel();
      });
    };

    $scope.changeSelectedOutcome = function() {
      $scope.tableHasAmbiguousArm = false;
      $scope.analysis.excludedArms = [];
      $scope.analysis.$save(function() {
        $scope.analysis.outcome = _.find($scope.outcomes, $scope.matchOutcome);
        $scope.reloadModel();
      });
    };

    function addIncludedInterventionUri(memo, intervention) {
      if (intervention.isIncluded) {
        memo.push(intervention.semanticInterventionUri);
      }
      return memo;
    }

    function getIncludedInterventions(interventions) {
      return _.filter(interventions, function(intervention) {
        return intervention.isIncluded;
      });
    }

    function updateNetwork() {
      var includedInterventions = getIncludedInterventions($scope.interventions);
      $scope.networkGraph.network = NetworkMetaAnalysisService.transformTrialDataToNetwork($scope.trialverseData, includedInterventions, $scope.analysis.excludedArms);
      $scope.isNetworkDisconnected = NetworkMetaAnalysisService.isNetworkDisconnected($scope.networkGraph.network);
    }


    function checkCanNotCreateModel() {
      return ($scope.editMode && $scope.editMode.disableEditing) ||
        $scope.tableHasAmbiguousArm ||
        $scope.interventions.length < 2 ||
        $scope.isNetworkDisconnected ||
        $scope.hasLessThanTwoInterventions;
    }
    $scope.isModelCreationBlocked = checkCanNotCreateModel();

    $scope.changeArmExclusion = function(dataRow) {
      $scope.tableHasAmbiguousArm = false;
      $scope.analysis = NetworkMetaAnalysisService.changeArmExclusion(dataRow, $scope.analysis);
      updateNetwork();
      $scope.analysis.$save(function() {
        $scope.analysis.outcome = _.find($scope.outcomes, $scope.matchOutcome);
        $scope.tableHasAmbiguousArm = NetworkMetaAnalysisService.doesModelHaveAmbiguousArms($scope.trialverseData, $scope.interventions, $scope.analysis);
        $scope.isModelCreationBlocked = checkCanNotCreateModel();
      });
    };
  };

  return dependencies.concat(NetworkMetaAnalysisContainerController);
});
