define(['angular', 'angular-mocks', 'controllers'],
  function() {
    describe("The studyController", function() {
      var ctrl, $scope, $q, $stateParams, TrialverseResource, StudyDetailsResource,
        StudyTreatmentActivityResource, StudyArmResource, StudyEpochResource,
        StudyPopulationCharacteristicsResource, StudyEndpointsResource, StudyAdverseEventsResource,
        treatmentActivity;

      beforeEach(module('addis.controllers'));

      beforeEach(inject(function($controller, $q) {
        treatmentActivity = {
          activityApplications: [{
            epochUid: 'epcohUid',
            armUid: 'armUid'
          }]
        };
        var treatmentActivities = [treatmentActivity];

        TrialverseResource = jasmine.createSpyObj('TrialverseResource', ['get']);
        StudyDetailsResource = jasmine.createSpyObj('StudyDetailsResource', ['get']);
        StudyArmResource = jasmine.createSpyObj('StudyArmResource', ['query']);
        StudyEpochResource = jasmine.createSpyObj('StudyEpochResource', ['query']);

        StudyPopulationCharacteristicsResource = jasmine.createSpyObj('StudyEpochResource', ['get']);
        StudyEndpointsResource = jasmine.createSpyObj('StudyEpochResource', ['get']);
        StudyAdverseEventsResource = jasmine.createSpyObj('StudyEpochResource', ['get']);


        StudyTreatmentActivityResource = jasmine.createSpyObj('StudyTreatmentActivityResource', ['query']);
        StudyTreatmentActivityResource.query.and.returnValue(treatmentActivities);

        scope = {};

        ctrl = $controller('StudyController', {
          $scope: scope,
          $q: $q,
          $stateParams: {},
          TrialverseResource: TrialverseResource,
          StudyDetailsResource: StudyDetailsResource,
          StudyTreatmentActivityResource: StudyTreatmentActivityResource,
          StudyArmResource: StudyArmResource,
          StudyEpochResource: StudyEpochResource,
          StudyPopulationCharacteristicsResource: StudyPopulationCharacteristicsResource,
          StudyEndpointsResource: StudyEndpointsResource,
          StudyAdverseEventsResource: StudyAdverseEventsResource
        });

      }));

      

    });
  });