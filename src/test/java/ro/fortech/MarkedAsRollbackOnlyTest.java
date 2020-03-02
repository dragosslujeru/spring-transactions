package ro.fortech;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.TransactionTemplate;
import ro.fortech.model.MyEntity;
import ro.fortech.repository.EntityRepository;
import ro.fortech.service.EntityService;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ro.fortech.ThrowableRunnable.unchecked;

@SpringBootTest
public class MarkedAsRollbackOnlyTest {

  @Autowired private EntityRepository entityRepository;
  @Autowired private EntityService entityService;
  @Autowired private TransactionTemplate transactionTemplate;

  @AfterEach
  public void cleanUp() {
    entityRepository.deleteAll();
    entityRepository.flush();
  }

  @Test
  public void shouldNotSetAsRollbackOnlyWhenOnlyOneTransaction() {
    ignoreException(() -> entityService.saveWithDefault(1, createEntity(), createEntity()));
  }

  @Test
  public void shouldSetAsRollbackOnlyWhenOnlyTwoTransaction() {
    assertThatThrownBy(
            () ->
                doInTransaction(
                    () ->
                        ignoreException(
                            () ->
                                entityService.saveWithDefault(1, createEntity(), createEntity()))))
        .isInstanceOf(UnexpectedRollbackException.class)
        .hasMessage("Transaction silently rolled back because it has been marked as rollback-only");
  }

  private MyEntity createEntity() {
    MyEntity entity = new MyEntity();
    entity.setValue("myValue");
    entity.setName("myName");
    return entity;
  }

  void doInTransaction(ThrowableRunnable<Exception> runnable) {
    transactionTemplate.executeWithoutResult((a) -> unchecked(runnable).run());
  }

  void ignoreException(ThrowableRunnable<Exception> runnable) {
    try {
      runnable.run();
    } catch (Exception e) {
      System.out.println("Excepion " + e + " ignored");
    }
  }
}
