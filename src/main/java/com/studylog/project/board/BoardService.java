package com.studylog.project.board;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.category.CategoryRepository;
import com.studylog.project.file.FileEntity;
import com.studylog.project.file.FileService;
import com.studylog.project.global.exception.CustomException;
import com.studylog.project.global.exception.ErrorCode;
import com.studylog.project.global.response.PageResponse;
import com.studylog.project.user.UserEntity;
import com.studylog.project.user.UserRepository;
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
    private final UserRepository userRepository;
    private final BoardRepositoryImpl boardRepositoryImpl;

    // 게시글 단일 조회
    public BoardDetailResponse getBoard(Long id, Long userId) {
        BoardEntity board = getBoardByUserAndId(userId, id);
        return BoardDetailResponse.toDto(BoardResponse.toDto(board), board);
    }

    // 게시글 목록 조회
    public PageResponse<BoardResponse> searchBoards(List<Long> categoryList, String keyword, List<String> sort, int page,
                                            Long userId) {
        UserEntity proxyUser = userRepository.getReferenceById(userId);

        // QueryDSL 조회
        List<BoardResponse> boardResponses = boardRepositoryImpl.searchBoardsByFilter(proxyUser, categoryList, keyword, sort, page);

        // 총 요소 개수 반환, count()는 항상 row가 하나씩 있어서 0 이상 반환
        Long totalItems= boardRepositoryImpl.getTotalItems(proxyUser, categoryList, keyword);

        long pageSize= 20;
        long totalPages= (totalItems + pageSize - 1) / pageSize;

        return new PageResponse<>(boardResponses, totalItems, totalPages, page, pageSize);
    }

    public BoardDetailResponse createBoard(BoardRequest request, String draftId, Long userId) {
        UserEntity proxyUser = userRepository.getReferenceById(userId);

        //board의 category는 categoryEntity타입으로 조회하고 엔티티로 받기
        CategoryEntity category= categoryRepository.findByUserAndId(proxyUser, request.categoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        BoardEntity board = request.toEntity(proxyUser, category);
        boardRepository.saveAndFlush(board); // 이때는 board.id 있음

        fileService.attachDraftFilesToBoard(proxyUser, board, draftId);

        return BoardDetailResponse.toDto(BoardResponse.toDto(board), board);
    }

    public BoardDetailResponse updateBoard(Long id, BoardRequest request, String draftId, Long userId) {
        UserEntity proxyUser = userRepository.getReferenceById(userId);

        BoardEntity board = getBoardByUserAndId(userId, id);
        CategoryEntity category= categoryRepository.findByUserAndId(proxyUser, request.categoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        board.updateBoard(category, request);

        fileService.attachDraftFilesToBoard(proxyUser, board, draftId);
        return BoardDetailResponse.toDto(BoardResponse.toDto(board), board);
    }

    public void deleteBoard(Long boardId, Long userId) {
        BoardEntity board = getBoardByUserAndId(userId, boardId);

        // 게시글 삭제 전 연결된 파일들의 물리적 경로를 찾아 디스크에서 지움
        if (board.getFiles() != null && !board.getFiles().isEmpty()) {
            for (FileEntity file : board.getFiles()) {
                fileService.deletePhysicalFile(file.getPath());
            }
        }

        // 게시글 DB 삭제 (파일 DB에서 알아서 삭제됨)
        boardRepository.delete(board);
    }

    private BoardEntity getBoardByUserAndId(Long userId, Long id) {
        UserEntity proxyUser = userRepository.getReferenceById(userId);

        return boardRepository.findByUserAndId(proxyUser, id).orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));
    }

    public void updateCategory(CategoryEntity deleteCategory, CategoryEntity defaultCategory){
        boardRepository.updateCategory(deleteCategory, defaultCategory);
    }
}