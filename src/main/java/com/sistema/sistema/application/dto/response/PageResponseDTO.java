package com.sistema.sistema.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter @AllArgsConstructor
public class PageResponseDTO<T> {
    private final List<T> items;
    private final long totalElements;
    private final int page;
    private final int size;
    private final boolean hasMore;
}
