package ro.fortech.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.fortech.model.MyEntity;

@Repository
public interface EntityRepository extends JpaRepository<MyEntity, Integer> {}
