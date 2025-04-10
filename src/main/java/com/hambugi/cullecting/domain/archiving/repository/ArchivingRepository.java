package com.hambugi.cullecting.domain.archiving.repository;

import com.hambugi.cullecting.domain.archiving.entity.Archiving;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArchivingRepository extends JpaRepository<Archiving, Object> {

    List<Archiving> findByMemberEmail(String email);
}
