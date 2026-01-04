package com.b.beep.domain.period.controller

import com.b.beep.domain.period.controller.docs.PeriodDocs
import com.b.beep.domain.period.controller.dto.request.CreatePeriodRequest
import com.b.beep.domain.period.controller.dto.request.UpdatePeriodRequest
import com.b.beep.domain.period.controller.dto.response.PeriodResponse
import com.b.beep.domain.period.service.PeriodService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/periods")
class PeriodController(
    private val periodService: PeriodService,
) : PeriodDocs {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun createPeriod(@RequestBody request: CreatePeriodRequest) {
        periodService.createPeriod(request)
    }

    @GetMapping
    override fun getPeriods(): List<PeriodResponse> {
        return periodService.getPeriods()
    }

    @GetMapping("/{periodId}")
    override fun getPeriod(@PathVariable periodId: Long): PeriodResponse {
        return periodService.getPeriod(periodId)
    }

    @PatchMapping("/{periodId}")
    override fun updatePeriod(
        @PathVariable periodId: Long,
        @RequestBody request: UpdatePeriodRequest
    ) {
        periodService.updatePeriod(periodId, request)
    }

    @DeleteMapping("/{periodId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun deletePeriod(@PathVariable periodId: Long) {
        periodService.deletePeriod(periodId)
    }

    @GetMapping("/current/attendance")
    override fun getCurrentAttendancePeriod(): Int {
        return periodService.getCurrentAttendancePeriod()
    }

    @GetMapping("/current")
    override fun getCurrentPeriod(): Int {
        return periodService.getCurrentPeriod()
    }
}
