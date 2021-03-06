package org.drugis.addis.remarks.repository.impl;

import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.remarks.Remarks;
import org.drugis.addis.remarks.repository.RemarksRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
public class RemarksRepositoryImplTest {

  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Inject
  RemarksRepository remarksRepository;

  @Test
  public void testGet() throws Exception {
    Integer analysisId = -1;
    Remarks expected = em.find(Remarks.class, analysisId);

    Remarks actual = remarksRepository.find(analysisId);
    assertEquals(expected, actual);
  }

  @Test
  public void testCreate() {
    Integer analysisId = -2;
    String remarksString = "remarks string";
    Remarks remarks = remarksRepository.create(analysisId, remarksString);
    assertEquals(analysisId, remarks.getAnalysisId());
    assertEquals(remarksString, remarks.getRemarks());
    assertNotNull(remarksRepository.find(analysisId));
  }

  @Test
  public void testUpdate() {

    String remarksStr = "remax string 2: the revenge";
    int analysisId = -1;
    Remarks update = new Remarks(analysisId, remarksStr);

    Remarks updated = remarksRepository.update(update);
    Remarks stored = em.find(Remarks.class, analysisId);

    assertEquals(stored.getAnalysisId(), updated.getAnalysisId());
    assertEquals(stored.getRemarks(), remarksStr);
  }
}