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
    private val periodService: PeriodService
) : PeriodDocs {
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    override fun findAll(): List<PeriodResponse> {
        return periodService.findAll().map { PeriodResponse.of(it) }
    }

    @GetMapping("/{period}")
    @ResponseStatus(HttpStatus.OK)
    override fun findByPeriod(@PathVariable period: Int): PeriodResponse {
        return PeriodResponse.of(periodService.getByPeriod(period))
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun create(@RequestBody request: CreatePeriodRequest) {
        periodService.create(request)
    }

    @PutMapping("/{period}")
    @ResponseStatus(HttpStatus.OK)
    override fun update(@PathVariable period: Int, @RequestBody request: UpdatePeriodRequest) {
        periodService.update(period, request)
    }

    @DeleteMapping("/{period}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun delete(@PathVariable period: Int) {
        periodService.delete(period)
    }
}
