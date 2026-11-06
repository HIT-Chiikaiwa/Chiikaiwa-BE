package org.hit.chiikaiwabe.domain.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hit.chiikaiwabe.domain.dto.pagination.PaginationFullRequestDto;

@Getter
@Setter
public class AdminUserSearchRequestDto {
    @NotNull @Min(0) @Max(2147483646L)
    private Integer page = 0;
    @NotNull @Min(1) @Max(100)
    private Integer size = 20;
    @Size(max = 200)
    private String keyword = "";
    private String sortBy = "createdDate";
    @NotNull
    private Boolean isAscending = false;

    public PaginationFullRequestDto toPaginationRequest() {
        PaginationFullRequestDto request = new PaginationFullRequestDto();
        request.setPageNum(page + 1);
        request.setPageSize(size);
        request.setKeyword(keyword == null ? "" : keyword);
        request.setSortBy(sortBy == null ? "createdDate" : sortBy);
        request.setIsAscending(isAscending);
        return request;
    }
}
