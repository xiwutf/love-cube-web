package com.lovecube.backend.repository;

import com.lovecube.backend.entity.NewcomerTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NewcomerTaskRepository extends JpaRepository<NewcomerTask, Long> {
    List<NewcomerTask> findByEnabledOrderBySortNoAsc(Integer enabled);

    Optional<NewcomerTask> findByCode(String code);
}
