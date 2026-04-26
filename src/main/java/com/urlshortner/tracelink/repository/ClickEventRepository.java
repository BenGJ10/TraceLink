package com.urlshortner.tracelink.repository;

import java.util.List;
import java.time.LocalDateTime;
import com.urlshortner.tracelink.models.ClickEvent;
import com.urlshortner.tracelink.models.UrlMapping;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

/*
    Repository interface for ClickEvent entity, extending JpaRepository to provide CRUD operations.
    It includes custom methods to find ClickEvents by UrlMapping and date range, as well as to find ClickEvents for a list of UrlMappings within a date range.
*/
@Repository
public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {
    List<ClickEvent> findByUrlMappingAndClickDateBetween(UrlMapping mapping, LocalDateTime startDate, LocalDateTime endDate);
    List<ClickEvent> findByUrlMappingInAndClickDateBetween(List<UrlMapping> urlMappings, LocalDateTime startDate, LocalDateTime endDate);
}
