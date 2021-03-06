package ro.fortech;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ro.fortech.model.MyEntity;
import ro.fortech.repository.EntityRepository;
import ro.fortech.service.AppException;
import ro.fortech.service.EntityService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ro.fortech.ThrowableRunnable.unchecked;

@SpringBootTest
public class InnerTransactionsTest {

  @Autowired private EntityRepository entityRepository;
  @Autowired private EntityService entityService;
  @Autowired private TransactionTemplate transactionTemplate;

  @AfterEach
  public void cleanUp() {
    entityRepository.deleteAll();
    entityRepository.flush();
  }

  @Test
  public void requiresNewShouldNotSaveFirstEntityWhenCalledFromSameClass() {
    assertThatThrownBy(
            () -> entityService.saveWithDefaultAndRequiresNew(0, createEntity(), createEntity()))
        .isInstanceOf(AppException.class);

    assertThat(entityRepository.findAll()).isEmpty();
  }

  @Test
  public void transactionTemplateHasRequiredPropagationLevel() {
      
    assertThatThrownBy(
            () ->
                entityService.saveWithDefaultAndTransactionTemplate(
                    0, createEntity(), createEntity()))
        .isInstanceOf(AppException.class);

    assertThat(entityRepository.findAll()).isEmpty();
  }

  private MyEntity createEntity() {
    MyEntity entity = new MyEntity();
    entity.setValue("myValue");
    entity.setName("myName");
    return entity;
  }
}
