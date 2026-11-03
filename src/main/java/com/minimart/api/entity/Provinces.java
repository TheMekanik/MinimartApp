package com.minimart.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "provinces")
public class Provinces {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToMany(
            mappedBy = "province",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Branches> branches;
}
