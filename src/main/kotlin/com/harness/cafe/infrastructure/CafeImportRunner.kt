package com.harness.cafe.infrastructure

import com.harness.cafe.application.CafeImportService
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class CafeImportRunner(
    private val cafeImportService: CafeImportService,
) : ApplicationRunner {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        val values = args.getOptionValues(ARG_NAME) ?: return
        var imported = 0
        var dup = 0
        var nonCafe = 0
        var regionUnknown = 0
        for (value in values) {
            for (query in value.split(',').map { it.trim() }.filter { it.isNotEmpty() }) {
                val result = cafeImportService.importFromNaver(query)
                imported += result.imported
                dup += result.duplicate
                nonCafe += result.nonCafe
                regionUnknown += result.regionUnknown
            }
        }
        log.info(
            "imported={} skipped(dup)={} skipped(non-cafe)={} skipped(region-unknown)={}",
            imported,
            dup,
            nonCafe,
            regionUnknown,
        )
    }

    companion object {
        private const val ARG_NAME = "import-naver"
    }
}
