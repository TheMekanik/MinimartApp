package com.minimart.api.repository;

import com.minimart.api.entity.Stores;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Stores, Long> {

    @Query("""
            SELECT s 
            FROM Stores s
            JOIN s.branch b
            JOIN b.province p
            WHERE s.deletedAt IS NULL
            AND b.deletedAt IS NULL
            AND b.isActive = true
            AND (:isActive IS NULL OR s.isActive = :isActive)
            AND (
                :provinceName IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :provinceName, '%'))
            )
            """)
    Page<Stores> findActiveStores(Boolean isActive, String provinceName, Pageable pageable);


    @Query("""
            SELECT s 
            FROM Stores s
            JOIN s.branch b
            WHERE s.deletedAt IS NULL
            AND s.isActive = true
            AND b.deletedAt IS NULL
            AND b.isActive = true
            AND b.id = :branchId
            """)
    List<Stores> findActiveStoresByBranchId(Long branchId);

    @Query("""
            SELECT s
            FROM Stores s
            JOIN s.branch b
            WHERE s.id = :id
            AND s.deletedAt IS NULL
            AND s.isActive = true
            AND b.deletedAt IS NULL
            AND b.isActive = true
            """)
    Optional<Stores> findVisibleById(Long id);

    @Query("""
            SELECT s
            FROM Stores s
            WHERE s.id = :id
            AND s.deletedAt IS NULL
            """)
    Optional<Stores> findEditableById(Long id);
}
