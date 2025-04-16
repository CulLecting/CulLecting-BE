package com.hambugi.cullecting.domain.archiving.repository;

import com.hambugi.cullecting.domain.archiving.util.TitleAndCodename;
import com.hambugi.cullecting.domain.archiving.entity.Archiving;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArchivingRepository extends JpaRepository<Archiving, Object> {

    List<Archiving> findByMemberEmail(String email);

    @Query("SELECT e.title AS title, e.category AS codename FROM Archiving e WHERE e.member.id = :userId")
    List<TitleAndCodename> findByTitleAndCodenameFromUserId(@Param("userId") String userId);

    int countByMemberEmail(String email);
}
