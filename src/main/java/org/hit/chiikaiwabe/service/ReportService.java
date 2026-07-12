package org.hit.chiikaiwabe.service;

import org.hit.chiikaiwabe.domain.dto.request.ReportRequestDto;

public interface ReportService {
    void createReport(String reporterId, ReportRequestDto requestDto);
}
