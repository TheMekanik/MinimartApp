package com.minimart.api.repository;

import com.minimart.api.entity.Branches;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BranchRepository extends JpaRepository<Branches, Long> {

    @Query("""
            SELECT b 
            FROM Branches b 
            WHERE b.deletedAt IS NULL
            AND (:isActive IS NULL OR b.isActive = :isActive)
            """)
    Page<Branches> findActiveBranches(Boolean isActive, Pageable pageable);
}
