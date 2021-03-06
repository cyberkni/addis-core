package org.drugis.addis.models.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.drugis.addis.TestUtils;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.config.TestConfig;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.controller.command.*;
import org.drugis.addis.models.exceptions.InvalidHeterogeneityTypeException;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.models.service.ModelService;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.util.WebConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import java.security.Principal;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@WebAppConfiguration
public class ModelControllerTest {
  private MockMvc mockMvc;

  @Mock
  private AnalysisService analysisService;

  @Mock
  private ProjectService projectService;

  @Mock
  private ModelService modelService;

  @Mock
  private ModelRepository modelRepository;

  @Mock
  private PataviTaskRepository pataviTaskRepository;

  @Inject
  private WebApplicationContext webApplicationContext;

  @InjectMocks
  private ModelController modelController;

  @InjectMocks
  private AbstractAddisCoreController abstractAddisCoreController;

  private Principal user;
  private Model.ModelBuilder modelBuilder;

  @Before
  public void setUp() {
    abstractAddisCoreController = new AbstractAddisCoreController();
    modelController = new ModelController();

    MockitoAnnotations.initMocks(this);

    mockMvc = MockMvcBuilders.standaloneSetup(abstractAddisCoreController, modelController).build();
    user = mock(Principal.class);
    when(user.getName()).thenReturn("gert");


    Integer analysisId = 55;
    String modelTitle = "model title";
    String linearModel = Model.LINEAR_MODEL_FIXED;
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;

    modelBuilder = new Model.ModelBuilder()
            .id(1)
            .analysisId(analysisId)
            .title(modelTitle)
            .linearModel(linearModel)
            .modelType(Model.NETWORK_MODEL_TYPE)
            .heterogeneityPriorType(Model.AUTOMATIC_HETEROGENEITY_PRIOR_TYPE)
            .burnInIterations(burnInIterations)
            .inferenceIterations(inferenceIterations)
            .thinningFactor(thinningFactor);

  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(analysisService, projectService, modelService);
  }

  @Test
  public void testCreateFixedEffectNetwork() throws Exception, InvalidHeterogeneityTypeException {
    Integer projectId = 45;
    Integer analysisId = 55;
    String modelTitle = "model title";
    String linearModel = Model.LINEAR_MODEL_FIXED;
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;

    Model model = modelBuilder.build();
    ModelTypeCommand modelTypeCommand = new ModelTypeCommand("network", null);

    HeterogeneityPriorCommand heterogeneityPriorCommand = null;
    CreateModelCommand createModelCommand = new CreateModelCommand(modelTitle, linearModel, modelTypeCommand, heterogeneityPriorCommand, burnInIterations, inferenceIterations, thinningFactor, likelihood, link);
    String body = TestUtils.createJson(createModelCommand);

    when(modelService.createModel(analysisId, createModelCommand)).thenReturn(model);
    mockMvc.perform(post("/projects/45/analyses/55/models")
            .content(body)
            .principal(user)
            .contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.analysisId", notNullValue()));

    verify(analysisService).checkCoordinates(projectId, analysisId);
    verify(projectService).checkOwnership(projectId, user);

    verify(modelService).createModel(analysisId, createModelCommand);

  }

  @Test
  public void testCreateNetworkWithStdDevHetPrior() throws Exception, InvalidHeterogeneityTypeException {
    Integer projectId = 45;
    Integer analysisId = 55;
    String modelTitle = "model title";
    String linearModel = Model.LINEAR_MODEL_RANDOM;
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;

    Double lower = 0.4;
    Double upper = 1.4;
    Model model = modelBuilder
            .heterogeneityPriorType(Model.STD_DEV_HETEROGENEITY_PRIOR_TYPE)
            .lower(lower)
            .upper(upper)
            .build();
    ModelTypeCommand modelTypeCommand = new ModelTypeCommand("network", null);

    HeterogeneityPriorCommand heterogeneityPriorCommand = new StdDevHeterogeneityPriorCommand(new StdDevValuesCommand(lower, upper));
    CreateModelCommand createModelCommand = new CreateModelCommand(modelTitle, linearModel, modelTypeCommand, heterogeneityPriorCommand, burnInIterations, inferenceIterations, thinningFactor, likelihood, link);
    String body = TestUtils.createJson(createModelCommand);

    when(modelService.createModel(analysisId, createModelCommand)).thenReturn(model);
    mockMvc.perform(post("/projects/45/analyses/55/models")
            .content(body)
            .principal(user)
            .contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.analysisId", notNullValue()));

    verify(analysisService).checkCoordinates(projectId, analysisId);
    verify(projectService).checkOwnership(projectId, user);
    verify(modelService).createModel(analysisId, createModelCommand);
  }

  @Test
  public void testCreateNetworkWithVarianceHetPrior() throws Exception, InvalidHeterogeneityTypeException {
    Integer projectId = 45;
    Integer analysisId = 55;
    String modelTitle = "model title";
    String linearModel = Model.LINEAR_MODEL_FIXED;
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;

    Double mean = 0.4;
    Double stdDev = 1.4;
    Model model = modelBuilder
            .heterogeneityPriorType(Model.VARIANCE_HETEROGENEITY_PRIOR_TYPE)
            .mean(mean)
            .stdDev(stdDev)
            .build();

    ModelTypeCommand modelTypeCommand = new ModelTypeCommand("network", null);
    HeterogeneityPriorCommand heterogeneityPriorCommand = new VarianceHeterogeneityPriorCommand(new VarianceValuesCommand(mean, stdDev));

    CreateModelCommand createModelCommand = new CreateModelCommand(modelTitle, linearModel, modelTypeCommand, burnInIterations, inferenceIterations, thinningFactor, likelihood, link);
    String body = TestUtils.createJson(createModelCommand);


    when(modelService.createModel(analysisId, createModelCommand)).thenReturn(model);
    mockMvc.perform(post("/projects/45/analyses/55/models")
            .content(body)
            .principal(user)
            .contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.analysisId", notNullValue()));

    verify(analysisService).checkCoordinates(projectId, analysisId);
    verify(projectService).checkOwnership(projectId, user);
    verify(modelService).createModel(analysisId, createModelCommand);

  }

  @Test
  public void testCreateNetworkWithPrecisionHetPrior() throws Exception, InvalidHeterogeneityTypeException {
    Integer projectId = 45;
    Integer analysisId = 55;
    String modelTitle = "model title";
    String linearModel = Model.LINEAR_MODEL_FIXED;
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;

    Double rate = 0.4;
    Double shape = 1.4;
    Model model = modelBuilder
            .heterogeneityPriorType(Model.PRECISION_HETEROGENEITY_PRIOR_TYPE)
            .rate(rate)
            .shape(shape)
            .build();
    ModelTypeCommand modelTypeCommand = new ModelTypeCommand("network", null);
    HeterogeneityPriorCommand heterogeneityPriorCommand = new PrecisionHeterogeneityPriorCommand(new PrecisionValuesCommand(rate, shape));
    CreateModelCommand modelCommand = new CreateModelCommand(modelTitle, linearModel, modelTypeCommand, heterogeneityPriorCommand, burnInIterations, inferenceIterations, thinningFactor, likelihood, link);
    String body = TestUtils.createJson(modelCommand);


    when(modelService.createModel(analysisId, modelCommand)).thenReturn(model);
    mockMvc.perform(post("/projects/45/analyses/55/models")
            .content(body)
            .principal(user)
            .contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.analysisId", notNullValue()));

    verify(analysisService).checkCoordinates(projectId, analysisId);
    verify(projectService).checkOwnership(projectId, user);
    verify(modelService).createModel(analysisId, modelCommand);

  }

  @Test
  public void testCreateModelWithFixedOutcomeScale() throws Exception, InvalidHeterogeneityTypeException {
    Integer projectId = 45;
    Integer analysisId = 55;
    String modelTitle = "model title";
    String linearModel = Model.LINEAR_MODEL_FIXED;
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;
    Double outcomeScale = 2.2;

    Model model = modelBuilder.build();
    ModelTypeCommand modelTypeCommand = new ModelTypeCommand("network", null);
    HeterogeneityPriorCommand heterogeneityPriorCommand = null;

    CreateModelCommand createModelCommand = new CreateModelCommand(modelTitle, linearModel, modelTypeCommand, heterogeneityPriorCommand, burnInIterations, inferenceIterations, thinningFactor, likelihood, link, outcomeScale);
    String body = TestUtils.createJson(createModelCommand);

    when(modelService.createModel(analysisId, createModelCommand)).thenReturn(model);
    mockMvc.perform(post("/projects/45/analyses/55/models")
            .content(body)
            .principal(user)
            .contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(status().isCreated());

    verify(analysisService).checkCoordinates(projectId, analysisId);
    verify(projectService).checkOwnership(projectId, user);
    verify(modelService).createModel(analysisId, createModelCommand);
  }

  @Test
  public void testCreatePairwise() throws Exception, InvalidHeterogeneityTypeException {
    Integer projectId = 45;
    Integer analysisId = 55;
    String modelTitle = "model title";
    String linearModel = Model.LINEAR_MODEL_FIXED;
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;

    Model model = modelBuilder.build();
    ModelTypeCommand modelTypeCommand = new ModelTypeCommand(Model.PAIRWISE_MODEL_TYPE, new DetailsCommand(new NodeCommand(-1, "t1"), new NodeCommand(-2, "t2")));
    HeterogeneityPriorCommand heterogeneityPriorCommand = null;

    CreateModelCommand createModelCommand = new CreateModelCommand(modelTitle, linearModel, modelTypeCommand, heterogeneityPriorCommand, burnInIterations, inferenceIterations, thinningFactor, likelihood, link);
    String body = TestUtils.createJson(createModelCommand);

    when(modelService.createModel(analysisId, createModelCommand)).thenReturn(model);
    mockMvc.perform(post("/projects/45/analyses/55/models")
            .content(body)
            .principal(user)
            .contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.analysisId", notNullValue()));

    verify(analysisService).checkCoordinates(projectId, analysisId);
    verify(projectService).checkOwnership(projectId, user);

    verify(modelService).createModel(analysisId, createModelCommand);
  }

  @Test
  public void testGet() throws Exception, InvalidHeterogeneityTypeException {
    Integer analysisId = 55;
    Model model = modelBuilder.build();
    when(modelRepository.get(model.getId())).thenReturn(model);

    mockMvc.perform(get("/projects/45/analyses/55/models/1").principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.id", is(model.getId())))
            .andExpect(jsonPath("$.analysisId", is(analysisId)));

    verify(modelRepository).get(model.getId());
  }

  @Test
  public void testQueryWithModelResult() throws Exception, InvalidHeterogeneityTypeException {
    Integer analysisId = 55;
    Model model = modelBuilder.build();
    List<Model> models = Collections.singletonList(model);
    when(modelService.query(analysisId)).thenReturn(models);

    mockMvc.perform(get("/projects/45/analyses/55/models").principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$[0].id", notNullValue()));

    verify(modelService).query(analysisId);
  }

  @Test
  public void testQueryWithNoModelResult() throws Exception {
    Integer analysisId = 55;
    when(modelService.query(analysisId)).thenReturn(Collections.<Model>emptyList());
    ResultActions resultActions = mockMvc.perform(get("/projects/45/analyses/55/models").principal(user));
    resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", empty()));
    verify(modelService).query(analysisId);
  }

  @Test
  public void testUpdate() throws Exception {

    String modelTitle = "model title";
    ModelTypeCommand modelTypeCommand = new ModelTypeCommand("network", null);
    String linearModel = Model.LINEAR_MODEL_FIXED;
    Integer burnInIterations = 2;
    Integer inferenceIterations = 2;
    Integer thinningFactor = 2;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;
    Integer modelId = 1;

    UpdateModelCommand updateModelCommand = new UpdateModelCommand(modelId, modelTitle, linearModel, modelTypeCommand,
            burnInIterations, inferenceIterations, thinningFactor, likelihood, link);
    String postBodyStr = TestUtils.createJson(updateModelCommand);

    mockMvc.perform(post("/projects/45/analyses/55/models/1")
            .content(postBodyStr)
            .principal(user)
            .contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

    verify(modelService).checkOwnership(modelId, user);
    verify(modelService).increaseRunLength(updateModelCommand);
  }

  @Test
  public void testGetResult() throws Exception {
    Integer taskId = 2;
    Model model = modelBuilder.taskId(taskId).build();
    Integer modelID = 1;
    JsonNode jsonNode = new ObjectMapper().readTree("{}");
    when(modelRepository.get(modelID)).thenReturn(model);
    when(pataviTaskRepository.getResult(model.getTaskId())).thenReturn(jsonNode);
    ResultActions resultActions = mockMvc.perform(get("/projects/45/analyses/55/models/1/result").principal(user));
    resultActions
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$", notNullValue()));
    verify(modelRepository).get(model.getId());
    verify(pataviTaskRepository).getResult(model.getTaskId());
  }

  @Test
  public void testGetResultNoTaskId() throws Exception {
    Model model = modelBuilder.build();
    Integer modelID = 1;
    when(modelRepository.get(modelID)).thenReturn(model);
    ResultActions resultActions = mockMvc.perform(get("/projects/45/analyses/55/models/1/result").principal(user));
    resultActions
            .andExpect(status().isNotFound());
    verify(modelRepository).get(model.getId());
  }

}