package com.minimart.api.repository;

import com.minimart.api.entity.Stores;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Stores, Long> {
}
