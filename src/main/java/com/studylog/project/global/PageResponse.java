package com.studylog.project.global;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class PageResponse<T> {
    private List<T> content; //카테고리, 게시글 목록 등
    private long totalItems;
    private int totalPages; //전체 페이지
    private int currentPage; //현재 페이지 번호

    public static <T> PageResponse<T> toDto(List<T> content, long totalItems, int totalPages, int currentPage){
        return new PageResponse<>(content, totalItems, totalPages, currentPage);
    }
}
