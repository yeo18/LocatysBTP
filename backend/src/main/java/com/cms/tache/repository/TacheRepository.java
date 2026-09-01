package com.cms.tache.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.cms.tache.entity.enums.Priorite;
import com.cms.tache.entity.Tache;
import com.cms.tache.entity.enums.TacheStatus;

/**
 * AccÃ¨s aux donnÃ©es des tÃ¢ches.
 */
public interface TacheRepository extends JpaRepository<Tache, Long> {

    Page<Tache> findByChantierId(Long chantierId, Pageable pageable);

    List<Tache> findByStatus(TacheStatus status);

    List<Tache> findByPriorite(Priorite priorite);
}
