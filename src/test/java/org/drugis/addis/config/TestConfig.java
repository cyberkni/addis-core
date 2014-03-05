package org.drugis.addis.config;

import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.repository.TrialverseRepository;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"org.drugis.addis.error", "org.drugis.addis.projects.controller", "org.drugis.addis.outcomes.controller", "org.drugis.addis.trialverse.controller"}, excludeFilters = {@ComponentScan.Filter(Configuration.class)})
public class TestConfig {
  @Bean
  public AccountRepository mockAccountRepository() {
    return Mockito.mock(AccountRepository.class);
  }

  @Bean
  public ProjectRepository mockProjectsRepository() {
    return Mockito.mock(ProjectRepository.class);
  }

  @Bean
  public TrialverseRepository mockTrialverseRepository() {
    return Mockito.mock(TrialverseRepository.class);
  }

  @Bean
  public TriplestoreService mockTriplestoreService() {
    return Mockito.mock(TriplestoreService.class);
  }

  @Bean
  public OutcomeRepository mockOutcomeRepository() {
    return Mockito.mock(OutcomeRepository.class);
  }
}

