package com.studylog.project.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor //toDto없어도 됨
@Getter
public class PageResponse<T> {
    private List<T> content; //카테고리, 게시글 목록 등
    private long totalItems;
    private long totalPages; //전체 페이지
    private int currentPage; //현재 페이지 번호
    private long pageSize; //페이지 사이즈
}
