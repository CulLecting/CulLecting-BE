package com.hambugi.cullecting.domain.curtural.repository;

import com.hambugi.cullecting.domain.curtural.entity.CulturalEvent;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CulturalEventRepository extends CrudRepository<CulturalEvent, Object> {
    List<CulturalEvent> findByTitleContainingIgnoreCase(String keyword);
}
