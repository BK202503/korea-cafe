package com.harness.cafe.domain

import org.springframework.data.jpa.repository.JpaRepository

interface CafeRepository : JpaRepository<Cafe, Long> {
    fun findAllByRegion(region: Region): List<Cafe>
}
