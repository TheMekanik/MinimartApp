package com.minimart.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "branches")
public class Branches extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "branch_code")
    private String branchCode;

    @Column(name = "branch_name")
    private String branchName;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id", nullable = false)
    private Provinces province;

    @OneToMany(
            mappedBy = "branch",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Stores> stores;
}
