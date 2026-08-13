package com.andr1chkol.taskapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "Paginated API response")
public class PageResponse<T> {
    @Schema(description = "Items contained in the current page")
    private final List<T> content;

    @Schema(
            description = "Current zero-based page number",
            example = "0"
    )
    private final int page;

    @Schema(
            description = "Maximum number of items per page",
            example = "10"
    )
    private final int size;

    @Schema(
            description = "Total number of matching items",
            example = "25"
    )
    private final long totalElements;

    @Schema(
            description = "Total number of available pages",
            example = "3"
    )
    private final int totalPages;

    @Schema(
            description = "Indicates whether this is the first page",
            example = "true"
    )
    private final boolean first;

    @Schema(
            description = "Indicates whether this is the last page",
            example = "false"
    )
    private final boolean last;

    public PageResponse(List<T> content, int page, int size, long totalElements, int totalPages, boolean first, boolean last) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = first;
        this.last = last;
    }

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    public List<T> getContent() {
        return content;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isFirst() {
        return first;
    }

    public boolean isLast() {
        return last;
    }


}
