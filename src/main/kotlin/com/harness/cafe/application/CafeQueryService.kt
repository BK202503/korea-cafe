package com.harness.cafe.application

import com.harness.cafe.domain.Cafe
import com.harness.cafe.domain.CafeRepository
import com.harness.cafe.domain.Region
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CafeQueryService(
    private val cafeRepository: CafeRepository,
) {
    fun findAll(): List<Cafe> = cafeRepository.findAll()

    fun findByRegion(region: Region): List<Cafe> = cafeRepository.findAllByRegion(region)

    fun findById(id: Long): Cafe = cafeRepository.findById(id).orElseThrow { CafeNotFoundException(id) }
}

class CafeNotFoundException(
    id: Long,
) : RuntimeException("Cafe not found: id=$id")
