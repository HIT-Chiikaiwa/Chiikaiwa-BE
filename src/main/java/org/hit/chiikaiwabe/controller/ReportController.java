package org.hit.chiikaiwabe.controller;

import org.hit.chiikaiwabe.base.RestApiV1;
import org.hit.chiikaiwabe.base.VsResponseUtil;
import org.hit.chiikaiwabe.domain.dto.request.ReportRequestDto;
import org.hit.chiikaiwabe.security.CurrentUser;
import org.hit.chiikaiwabe.security.UserPrincipal;
import org.hit.chiikaiwabe.service.ReportService;
import org.hit.chiikaiwabe.constant.SuccessMessage;
import org.hit.chiikaiwabe.constant.UrlConstant;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;
import org.hit.chiikaiwabe.annotation.RateLimit;

@RequiredArgsConstructor
@RestApiV1
public class ReportController {

    private final ReportService reportService;

    @RateLimit(capacity = 3, durationInSeconds = 60)
    @PostMapping(UrlConstant.BlockReport.REPORT)
    public ResponseEntity<?> createReport(@RequestBody @Valid ReportRequestDto requestDto,
                                          @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        reportService.createReport(principal.getId(), requestDto);
        return VsResponseUtil.success(SuccessMessage.Chat.REPORT_SUBMITTED_SUCCESS);
    }
}
