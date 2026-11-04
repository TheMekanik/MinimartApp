package com.minimart.api.repository;

import com.minimart.api.entity.Stores;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StoreRepository extends JpaRepository<Stores, Long> {

    @Query("""
            SELECT s 
            FROM Stores s
            JOIN s.branch b
            JOIN b.province p
            WHERE s.deletedAt IS NULL
            AND (:isActive IS NULL OR s.isActive = :isActive)
            AND (
                :provinceName IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :provinceName, '%'))
            )
            """)
    Page<Stores> findActiveStores(Boolean isActive, String provinceName, Pageable pageable);


    @Query("""
            SELECT s 
            FROM Stores s
            WHERE s.deletedAt IS NULL
            AND s.isActive = true
            AND s.branch.id = :branchId
            """)
    List<Stores> findActiveStoresByBranchId(Long branchId);
}
