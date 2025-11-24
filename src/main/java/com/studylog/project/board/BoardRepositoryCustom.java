package com.studylog.project.board;

import com.studylog.project.user.UserEntity;

import java.util.List;

public interface BoardRepositoryCustom {

    List<BoardResponse> searchBoardsByFilter(UserEntity user, List<Long> categoryIds, String keyword,
                                         List<String> sortList, int page);

    Long getTotalItems(UserEntity user, List<Long> categoryIds, String keyword);

}
