package com.minimart.api.repository;

import com.minimart.api.entity.Provinces;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProvinceRepository extends JpaRepository<Provinces, Long> {
}
