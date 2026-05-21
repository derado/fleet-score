package com.fleetscore.regatta.repository;

import com.fleetscore.regatta.domain.Series;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeriesRepository extends JpaRepository<Series, Long> {

    List<Series> findByOwnerId(Long ownerId);
}
