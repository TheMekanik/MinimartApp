package com.minimart.api.repository;

import com.minimart.api.entity.StoreWhitelists;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StoreWhitelistRepository extends JpaRepository<StoreWhitelists, Long> {

    @Query("""
            SELECT w
            FROM StoreWhitelists w
            WHERE w.store.id = :storeId
            """)
    Optional<StoreWhitelists> findByStoreId(@Param("storeId") Long storeId);

    @Query("""
            SELECT w
            FROM StoreWhitelists w
            JOIN w.store s
            JOIN s.branch b
            JOIN b.province p
            WHERE s.isActive = true
            AND s.deletedAt IS NULL
            AND b.isActive = true
            AND b.deletedAt IS NULL
            AND (:provinceName IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :provinceName, '%')))
            """)
    Page<StoreWhitelists> findVisibleStores(
            Pageable pageable,
            @Param("provinceName") String provinceName
    );
}
