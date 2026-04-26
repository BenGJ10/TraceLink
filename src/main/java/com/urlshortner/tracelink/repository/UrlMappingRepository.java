package com.urlshortner.tracelink.repository;

import java.util.List;
import com.urlshortner.tracelink.models.User;
import com.urlshortner.tracelink.models.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
    Repository interface for UrlMapping entity, extending JpaRepository to provide CRUD operations.
    It includes custom methods to find UrlMapping by short URL and to find all UrlMappings associated with a specific user.
*/
@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {
    UrlMapping findByShortUrl(String shortUrl);
    List<UrlMapping> findByUser(User user);
}
