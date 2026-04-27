package com.lovecube.backend.repository;

import com.lovecube.backend.entity.HomeConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface HomeConfigRepository extends JpaRepository<HomeConfig, Long> {
    Optional<HomeConfig> findByConfigGroupAndConfigKey(String configGroup, String configKey);

    List<HomeConfig> findByConfigGroupInOrderByConfigGroupAscSortOrderAscIdAsc(Collection<String> groups);

    List<HomeConfig> findByConfigGroupInAndEnabledTrueOrderByConfigGroupAscSortOrderAscIdAsc(Collection<String> groups);

    long countByConfigGroup(String configGroup);

    long countByConfigGroupAndEnabledTrue(String configGroup);
}
