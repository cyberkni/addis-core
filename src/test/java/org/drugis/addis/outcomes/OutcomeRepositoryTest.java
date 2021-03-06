package org.drugis.addis.outcomes;

import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.security.Account;
import org.drugis.addis.trialverse.model.SemanticOutcome;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by daan on 3/7/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
public class OutcomeRepositoryTest {
  @Inject
  private OutcomeRepository outcomeRepository;

  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Test
  public void testQuery() {
    Collection<Outcome> outcomes = outcomeRepository.query(1);
    assertEquals(2, outcomes.size());
    outcomes = outcomeRepository.query(2);
    assertEquals(1, outcomes.size());
  }

  @Test
  public void testGet() throws ResourceDoesNotExistException {
    Outcome outcome = outcomeRepository.get(1, 1);
    assertEquals(em.find(Outcome.class, 1), outcome);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testGetFromWrongProjectFails() throws ResourceDoesNotExistException {
    outcomeRepository.get(2, 1);
  }

  @Test
  public void testCreateOutcome() throws Exception {
    OutcomeCommand outcomeCommand = new OutcomeCommand(1, "newName 1", "newMotivation", new SemanticOutcome("http://semantic.com", "labelnew"));
    Account user = em.find(Account.class, 1);
    Outcome result = outcomeRepository.create(user, outcomeCommand);
    assertTrue(outcomeRepository.query(1).contains(result));
  }


  @Test(expected = MethodNotAllowedException.class)
  public void testCannotCreateOutcomeInNotOwnedProject() throws Exception {
    Account account = em.find(Account.class, 2);
    OutcomeCommand outcomeCommand = new OutcomeCommand(1, "newName 2", "newMotivation", new SemanticOutcome("http://semantic.com", "labelnew"));
    outcomeRepository.create(account, outcomeCommand);
  }


  @Test(expected = ResourceDoesNotExistException.class)
  public void testCannotCreateOutcomeInNonexistentProject() throws Exception {
    Account account = em.find(Account.class, 2);
    OutcomeCommand outcomeCommand = new OutcomeCommand(13221, "newName 3", "newMotivation", new SemanticOutcome("http://semantic.com", "labelnew"));
    outcomeRepository.create(account, outcomeCommand);
  }

  @Test(expected = InvalidDataAccessApiUsageException.class)
  public void testCannotCreateOutcomeWithDuplicateName() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Account account = em.find(Account.class, 1);
    OutcomeCommand outcomeCommand = new OutcomeCommand(1, "outcome 1", "newMotivation", new SemanticOutcome("http://semantic.com", "labelnew"));
    outcomeRepository.create(account, outcomeCommand);
  }

}
