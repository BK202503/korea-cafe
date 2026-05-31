package com.harness.cafe.interfaces

import com.harness.cafe.application.CafeNotFoundException
import com.harness.cafe.application.CafeQueryService
import com.harness.cafe.domain.Region
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus

@Controller
@RequestMapping("/cafes")
class CafeController(
    private val cafeQueryService: CafeQueryService,
) {
    @GetMapping
    fun list(
        @RequestParam(required = false) region: Region?,
        model: Model,
    ): String {
        val cafes =
            if (region != null) {
                cafeQueryService.findByRegion(region)
            } else {
                cafeQueryService.findAll()
            }
        model.addAttribute("cafes", cafes)
        model.addAttribute("regions", Region.entries)
        model.addAttribute("selectedRegion", region)
        return "cafe/list"
    }

    @GetMapping("/{id}")
    fun detail(
        @PathVariable id: Long,
        model: Model,
    ): String {
        model.addAttribute("cafe", cafeQueryService.findById(id))
        return "cafe/detail"
    }

    @ExceptionHandler(CafeNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(
        e: CafeNotFoundException,
        model: Model,
    ): String {
        model.addAttribute("message", e.message)
        return "error/404"
    }
}
