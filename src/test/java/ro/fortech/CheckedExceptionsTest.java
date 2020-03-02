package ro.fortech;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ro.fortech.model.MyEntity;
import ro.fortech.repository.EntityRepository;
import ro.fortech.service.EntityService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class CheckedExceptionsTest {

  @Autowired private EntityRepository entityRepository;
  @Autowired private EntityService entityService;

  @AfterEach
  public void cleanUp() {
    entityRepository.deleteAll();
    entityRepository.flush();
  }

  @Test
  public void checkedExceptionsShouldNotRollBack() {
    assertThatThrownBy(
            () -> entityService.saveWithCheckedException(1, createEntity(), createEntity()))
        .isInstanceOf(Exception.class);

    assertThat(entityRepository.findAll()).hasSize(1);
  }

  @Test
  public void uncheckedExceptionsShouldRollBack() {
    assertThatThrownBy(
            () -> entityService.saveWithDefault(1, createEntity(), createEntity()))
        .isInstanceOf(RuntimeException.class);

    assertThat(entityRepository.findAll()).hasSize(0);
  }

  private MyEntity createEntity() {
    MyEntity entity = new MyEntity();
    entity.setValue("myValue");
    entity.setName("myName");
    return entity;
  }
}
