package com.harness.cafe.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "cafe")
class Cafe(
    @Column(nullable = false, length = 100)
    val name: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    val region: Region,
    @Column(nullable = false, length = 200)
    val address: String,
    @Column(nullable = false, length = 1000)
    val description: String,
    @Column(length = 500)
    val imageUrl: String? = null,
    @Column(length = 100)
    val signatureMenu: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val externalSource: CafeSource = CafeSource.SEED,
    @Column(length = 500)
    val externalId: String? = null,
    @Column(length = 500)
    val externalUrl: String? = null,
    @Column
    val mapX: Long? = null,
    @Column
    val mapY: Long? = null,
    @Column(length = 50)
    val phone: String? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
)
