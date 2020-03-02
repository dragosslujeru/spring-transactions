package ro.fortech.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.fortech.model.MyEntity;

@Service
public class OuterService {

  @Autowired private EntityService entityService;

  @Transactional
  public void saveInnerAndFail(MyEntity... entities) throws Exception {
    entityService.saveWithRequiresNew(-1, entities);
    throw new AppException();
  }

  @Transactional
  public void saveInnerIgnoringExceptions(int failInnerAt, MyEntity... entities) throws Exception {
    try {
      entityService.saveWithDefault(failInnerAt, entities);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
