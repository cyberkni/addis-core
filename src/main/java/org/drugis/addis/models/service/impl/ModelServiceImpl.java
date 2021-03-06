package org.drugis.addis.models.service.impl;

import org.drugis.addis.analyses.AbstractAnalysis;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.controller.command.*;
import org.drugis.addis.models.exceptions.InvalidHeterogeneityTypeException;
import org.drugis.addis.models.exceptions.InvalidModelTypeException;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.models.service.ModelService;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.projects.service.ProjectService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.security.Principal;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by daan on 22-5-14.
 */
@Service
public class ModelServiceImpl implements ModelService {
  @Inject
  ModelRepository modelRepository;

  @Inject
  AnalysisRepository analysisRepository;

  @Inject
  ProjectService projectService;

  @Inject
  PataviTaskRepository pataviTaskRepository;

  @Override

  public Model createModel(Integer analysisId, CreateModelCommand command) throws ResourceDoesNotExistException, InvalidModelTypeException, InvalidHeterogeneityTypeException {
    ModelTypeCommand modelTypeCommand = command.getModelType();
    HeterogeneityPriorCommand heterogeneityPrior = command.getHeterogeneityPrior();
    String heterogeneityPriorType = determineHeterogeneityPriorType(heterogeneityPrior);
    Model.ModelBuilder builder = new Model.ModelBuilder()
            .analysisId(analysisId)
            .title(command.getTitle())
            .linearModel(command.getLinearModel())
            .modelType(modelTypeCommand.getType())
            .heterogeneityPriorType(heterogeneityPriorType)
            .burnInIterations(command.getBurnInIterations())
            .inferenceIterations(command.getInferenceIterations())
            .thinningFactor(command.getThinningFactor())
            .likelihood(command.getLikelihood())
            .link(command.getLink())
            .outcomeScale(command.getOutcomeScale());

    if (Model.STD_DEV_HETEROGENEITY_PRIOR_TYPE.equals(heterogeneityPriorType)) {
      StdDevValuesCommand heterogeneityValuesCommand = ((StdDevHeterogeneityPriorCommand) heterogeneityPrior).getValues();
      builder = builder
              .lower(heterogeneityValuesCommand.getLower())
              .upper(heterogeneityValuesCommand.getUpper());
    } else if (Model.VARIANCE_HETEROGENEITY_PRIOR_TYPE.equals(heterogeneityPriorType)) {
      VarianceValuesCommand heterogeneityValuesCommand = ((VarianceHeterogeneityPriorCommand) heterogeneityPrior).getValues();
      builder = builder
              .mean(heterogeneityValuesCommand.getMean())
              .stdDev(heterogeneityValuesCommand.getStdDev());
    } else if (Model.PRECISION_HETEROGENEITY_PRIOR_TYPE.equals(heterogeneityPriorType)) {
      PrecisionValuesCommand heterogeneityValuesCommand = ((PrecisionHeterogeneityPriorCommand) heterogeneityPrior).getValues();
      builder = builder
              .rate(heterogeneityValuesCommand.getRate())
              .shape(heterogeneityValuesCommand.getShape());
    }

    DetailsCommand details = modelTypeCommand.getDetails();
    if (details != null) {
      builder = builder
              .from(new Model.DetailNode(details.getFrom().getId(), details.getFrom().getName()))
              .to(new Model.DetailNode(details.getTo().getId(), details.getTo().getName()));
    }

    Model model = builder.build();
    return modelRepository.persist(model);
  }

  private String determineHeterogeneityPriorType(HeterogeneityPriorCommand heterogeneityPriorCommand) {
    if (heterogeneityPriorCommand instanceof StdDevHeterogeneityPriorCommand) {
      return Model.STD_DEV_HETEROGENEITY_PRIOR_TYPE;
    } else if (heterogeneityPriorCommand instanceof VarianceHeterogeneityPriorCommand) {
      return Model.VARIANCE_HETEROGENEITY_PRIOR_TYPE;
    } else if (heterogeneityPriorCommand instanceof PrecisionHeterogeneityPriorCommand) {
      return Model.PRECISION_HETEROGENEITY_PRIOR_TYPE;
    } else {
      return Model.AUTOMATIC_HETEROGENEITY_PRIOR_TYPE;
    }
  }

  @Override
  public List<Model> query(Integer analysisId) throws SQLException {
    return modelRepository.findByAnalysis(analysisId);
  }

  @Override
  public void checkOwnership(Integer modelId, Principal principal) throws ResourceDoesNotExistException, MethodNotAllowedException {
    Model model = modelRepository.get(modelId);
    AbstractAnalysis analysis = analysisRepository.get(model.getAnalysisId());

    projectService.checkOwnership(analysis.getProjectId(), principal);
  }

  private void checkIncrease(Model persistendModel, UpdateModelCommand updateModelCommand) throws MethodNotAllowedException {
    if(persistendModel.getBurnInIterations() > updateModelCommand.getBurnInIterations() ||
            persistendModel.getInferenceIterations() > updateModelCommand.getInferenceIterations()) {
      throw new MethodNotAllowedException();
    }
  }

  @Override
  public void increaseRunLength(UpdateModelCommand updateModelCommand) throws MethodNotAllowedException, InvalidModelTypeException {
    Model oldModel = modelRepository.get(updateModelCommand.getId());

    // check that increase is not a decrease
    checkIncrease(oldModel, updateModelCommand);

    oldModel.setBurnInIterations(updateModelCommand.getBurnInIterations());
    oldModel.setInferenceIterations(updateModelCommand.getInferenceIterations());
    oldModel.setThinningFactor(updateModelCommand.getThinningFactor());
    pataviTaskRepository.delete(oldModel.getTaskId());
    oldModel.setTaskId(null);

    modelRepository.persist(oldModel);

  }
}
