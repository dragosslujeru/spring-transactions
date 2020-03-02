package ro.fortech;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ro.fortech.model.MyEntity;
import ro.fortech.repository.EntityRepository;
import ro.fortech.service.AppException;
import ro.fortech.service.EntityService;
import ro.fortech.service.OuterService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ro.fortech.ThrowableRunnable.unchecked;

@SpringBootTest
public class PropagationLevelTest {

  @Autowired private EntityRepository entityRepository;
  @Autowired private EntityService entityService;
  @Autowired private TransactionTemplate transactionTemplate;
  @Autowired private OuterService outerService;

  @AfterEach
  public void cleanUp() {
    entityRepository.deleteAll();
    entityRepository.flush();
  }

  @Test
  public void supportsShouldBeTransactionalWithOuterTransaction() {
    assertThatThrownBy(
            () ->
                doInTransaction(
                    () -> entityService.saveWithSupports(1, createEntity(), createEntity())))
        .isInstanceOf(AppException.class);

    assertThat(entityRepository.findAll()).hasSize(0);
  }

  @Test
  public void supportsShouldNotBeTransactionalWithoutOuterTransaction() {
    assertThatThrownBy(() -> entityService.saveWithSupports(1, createEntity(), createEntity()))
        .isInstanceOf(AppException.class);

    assertThat(entityRepository.findAll()).hasSize(1);
  }

  @Test
  public void requiredShouldBeTransactionalWithoutOuterTransaction() {
    assertThatThrownBy(() -> entityService.saveWithDefault(1, createEntity(), createEntity()))
        .isInstanceOf(AppException.class);

    assertThat(entityRepository.findAll()).hasSize(0);
  }

  @Test
  public void neverShouldNotBeTransactional() {
    assertThatThrownBy(() -> entityService.saveWithNever(1, createEntity(), createEntity()))
        .isInstanceOf(AppException.class);

    assertThat(entityRepository.findAll()).hasSize(1);
  }

  @Test
  public void neverShouldThrowExceptionWhenOuterTransaction() {
    assertThatThrownBy(
            () ->
                doInTransaction(
                    () -> entityService.saveWithNever(1, createEntity(), createEntity())))
        .isInstanceOf(IllegalTransactionStateException.class);

    assertThat(entityRepository.findAll()).hasSize(0);
  }

  @Test
  public void mandatoryShouldThrowExceptionWithoutOuterTransaction() {
    assertThatThrownBy(() -> entityService.saveWithMandatory(5, createEntity(), createEntity()))
        .isInstanceOf(IllegalTransactionStateException.class);

    assertThat(entityRepository.findAll()).hasSize(0);
  }

  @Test
  public void mandatoryShouldNotThrowExceptionWithOuterTransaction() {
    doInTransaction(() -> entityService.saveWithMandatory(5, createEntity(), createEntity()));

    assertThat(entityRepository.findAll()).hasSize(2);
  }

  @Test
  public void
      requiresNewShouldBeTransactionalEvenIfOuterTransactionFailsAfterInnerTransactionCompletes() {
    assertThatThrownBy(() -> outerService.saveInnerAndFail(createEntity(), createEntity()))
        .isInstanceOf(AppException.class);

    assertThat(entityRepository.findAll()).hasSize(2);
  }
  

  void doInTransaction(ThrowableRunnable<Exception> runnable) {
    transactionTemplate.executeWithoutResult((a) -> unchecked(runnable).run());
  }

  private MyEntity createEntity() {
    MyEntity entity = new MyEntity();
    entity.setValue("myValue");
    entity.setName("myName");
    return entity;
  }
}
