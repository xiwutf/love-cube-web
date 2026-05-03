package com.lovecube.backend.repository;

import com.lovecube.backend.entity.AccountTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountTaskRepository extends JpaRepository<AccountTask, Long> {
    List<AccountTask> findByEnabledOrderBySortNoAsc(Integer enabled);

    Optional<AccountTask> findByCode(String code);
}
