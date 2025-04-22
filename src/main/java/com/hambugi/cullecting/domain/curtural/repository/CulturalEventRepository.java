package com.hambugi.cullecting.domain.curtural.repository;

import com.hambugi.cullecting.domain.curtural.entity.CulturalEvent;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface CulturalEventRepository extends CrudRepository<CulturalEvent, Object> {
    CulturalEvent findById(Long id);
    List<CulturalEvent> findByTitleContainingIgnoreCase(String keyword);
    List<CulturalEvent> findTop3ByOrderByStartDateAsc();
    @Query(value = "SELECT * FROM culturalevent WHERE startDate <= NOW() ORDER BY startDate DESC LIMIT 3", nativeQuery = true)
    List<CulturalEvent> findTop3RecentEventsBeforeNow();

    @Query(value = "SELECT * FROM culturalevent WHERE codeName IN :codeNameList AND startDate <= NOW() ORDER BY startDate DESC LIMIT 3", nativeQuery = true)
    List<CulturalEvent> findTop3ByThemeCodeBeforeNow(@Param("codeNameList") List<String> codeNameList);

//    @Query(value = "SELECT * FROM culturalevent WHERE codeName IN :codeNameList AND startDate <= NOW() ORDER BY startDate DESC LIMIT 10", nativeQuery = true)
//    List<CulturalEvent> findTop10ByThemeCodeBeforeNow(@Param("codeNameList") List<String> codeNameList);

    @Query(value = "SELECT * FROM culturalevent WHERE startDate <= NOW() ORDER BY startDate DESC LIMIT 10", nativeQuery = true)
    List<CulturalEvent> findTop10Random();

    @Query(value = "SELECT * FROM culturalevent WHERE :targetDate BETWEEN startDate AND endDate", nativeQuery = true)
    List<CulturalEvent> findCulturalFromDate(@Param("date") LocalDate targetDate);
}
