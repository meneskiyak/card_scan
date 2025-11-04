package com.cardscan.cardscanbackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "tag_id")
    private UUID tagId;

    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @ManyToMany(mappedBy = "tags")
    private Set<Contact> contacts = new HashSet<>();

    public Tag() {}

    public Tag(String name) {
        this.name = name;
    }
}