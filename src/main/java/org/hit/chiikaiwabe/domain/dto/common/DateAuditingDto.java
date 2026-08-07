package org.hit.chiikaiwabe.domain.dto.common;

import org.hit.chiikaiwabe.constant.CommonConstant;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public abstract class DateAuditingDto {

  private LocalDateTime createdDate;

  private LocalDateTime lastModifiedDate;

}
