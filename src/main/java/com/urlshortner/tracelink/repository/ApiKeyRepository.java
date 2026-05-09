package com.urlshortner.tracelink.repository;

import com.urlshortner.tracelink.models.ApiKey;
import com.urlshortner.tracelink.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    List<ApiKey> findByUserOrderByIdDesc(User user);
    Optional<ApiKey> findByKeyPrefixAndIsActiveTrue(String keyPrefix);
    Optional<ApiKey> findByIdAndUser(Long id, User user);
}
