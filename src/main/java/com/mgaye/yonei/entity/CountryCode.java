package com.mgaye.yonei.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "country_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CountryCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String prefix;

    @Column(nullable = false, length = 5)
    private String code; // ISO 3166-1 Alpha-2 (e.g., "CI")

    @Column(length = 100)
    private String name;

    @Column(length = 10)
    private String currency;

    @Column(length = 1)
    private String flag;
}
