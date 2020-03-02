package ro.fortech.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ro.fortech.model.MyEntity;
import ro.fortech.repository.EntityRepository;

import java.util.Arrays;

@Service
public class EntityService {

  @Autowired private EntityRepository entityRepository;
  @Autowired private TransactionTemplate transactionTemplate;

  @Transactional(propagation = Propagation.SUPPORTS)
  public void saveWithSupports(int failAtSave, MyEntity... entities) throws Exception {
    saveAllWithFailAt(failAtSave, entities);
  }

  @Transactional
  public void saveWithDefault(int failAtSave, MyEntity... entities) throws Exception {
    saveAllWithFailAt(failAtSave, entities);
  }

  @Transactional
  public void saveWithCheckedException(int failAtSave, MyEntity... entities) throws Exception {
    saveAllWithFailAt(failAtSave, new Exception(), entities);
  }

  @Transactional(propagation = Propagation.NEVER)
  public void saveWithNever(int failAtSave, MyEntity... entities) throws Exception {
    saveAllWithFailAt(failAtSave, entities);
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void saveWithMandatory(int failAtSave, MyEntity... entities) throws Exception {
    saveAllWithFailAt(failAtSave, entities);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void saveWithRequiresNew(int failAtSave, MyEntity... entities) throws Exception {
    saveAllWithFailAt(failAtSave, entities);
  }

  @Transactional
  public void saveWithDefaultAndRequiresNew(int failAtSave, MyEntity... entities) throws Exception {
    saveWithRequiresNew(-1, entities[0]);
    saveAllWithFailAt(failAtSave, Arrays.copyOfRange(entities, 1, entities.length));
  }

  @Transactional
  public void saveWithDefaultAndTransactionTemplate(int failAtSave, MyEntity... entities)
      throws Exception {
    transactionTemplate.executeWithoutResult(
        (a) -> {
          try {
            saveWithDefault(-1, entities[0]);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
    saveAllWithFailAt(failAtSave, Arrays.copyOfRange(entities, 1, entities.length));
  }

  private void saveAllWithFailAt(int failAtSave, MyEntity... entities) throws Exception {
    saveAllWithFailAt(failAtSave, new AppException(), entities);
  }

  private void saveAllWithFailAt(int failAtSave, Exception exception, MyEntity... entities)
      throws Exception {
    for (int i = 0; i < entities.length; i++) {
      if (i == failAtSave) {
        throw exception;
      }
      entityRepository.saveAndFlush(entities[i]);
    }
  }
}
