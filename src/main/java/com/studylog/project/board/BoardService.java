package com.studylog.project.board;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studylog.project.category.CategoryEntity;
import com.studylog.project.category.CategoryRepository;
import com.studylog.project.file.FileService;
import com.studylog.project.global.exception.CustomException;
import com.studylog.project.global.exception.ErrorCode;
import com.studylog.project.global.response.PageResponse;
import com.studylog.project.user.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final CategoryRepository categoryRepository;
    private final FileService fileService;
    private final BoardRepositoryImpl boardRepositoryImpl;

    // 게시글 단일 조회
    public BoardDetailResponse getBoard(Long id, UserEntity user) {
        BoardEntity board = getBoardByUserAndId(user, id);
        return BoardDetailResponse.toDto(BoardResponse.toDto(board), board);
    }

    // 게시글 목록 조회
    public PageResponse<BoardResponse> searchBoards(List<Long> categoryList, String keyword, List<String> sort, int page,
                                            UserEntity user) {

        // QueryDSL 조회
        List<BoardResponse> boardResponses = boardRepositoryImpl.searchBoardsByFilter(user, categoryList, keyword, sort, page);

        // 총 요소 개수 반환, count()는 항상 row가 하나씩 있어서 0 이상 반환
        Long totalItems= boardRepositoryImpl.getTotalItems(user, categoryList, keyword);

        long pageSize= 30;
        long totalPages= (totalItems + pageSize - 1) / pageSize;

        return new PageResponse<>(boardResponses, totalItems, totalPages, page, pageSize);
    }

    public BoardDetailResponse createBoard(BoardRequest request, String draftId, UserEntity user) {
        //board의 category는 categoryEntity타입으로 조회하고 엔티티로 받기
        CategoryEntity category= categoryRepository.findByUserAndId(user, request.categoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        BoardEntity board = request.toEntity(user, category);
        boardRepository.saveAndFlush(board); // 이때는 board.id 있음

        fileService.attachDraftFilesToBoard(user, board, draftId);

        return BoardDetailResponse.toDto(BoardResponse.toDto(board), board);
    }

    public BoardDetailResponse updateBoard(Long id, BoardRequest request, String draftId, UserEntity user) {
        BoardEntity board = getBoardByUserAndId(user, id);
        CategoryEntity category= categoryRepository.findByUserAndId(user, request.categoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        board.updateBoard(category, request);

        fileService.attachDraftFilesToBoard(user, board, draftId);
        return BoardDetailResponse.toDto(BoardResponse.toDto(board), board);
    }

    public void deleteBoard(Long id, UserEntity user) {
        BoardEntity board= getBoardByUserAndId(user, id);
        boardRepository.delete(board);
        log.info("게시글 삭제 완료");
    }

    private BoardEntity getBoardByUserAndId(UserEntity user, Long id) {
        return boardRepository.findByUserAndId(user, id).orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));
    }

    public void updateCategory(CategoryEntity deleteCategory, CategoryEntity defaultCategory){
        boardRepository.updateCategory(deleteCategory, defaultCategory);
    }
}