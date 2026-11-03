package com.minimart.api.repository;

import com.minimart.api.entity.Branches;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<Branches, Long> {
}
