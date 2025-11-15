package com.studylog.project.board;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studylog.project.category.CategoryEntity;
import com.studylog.project.category.CategoryRepository;
import com.studylog.project.file.FileService;
import com.studylog.project.global.CommonThrow;
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
    private final JPAQueryFactory queryFactory;
    private final FileService fileService;

    public BoardDetailResponse getBoard(Long id, UserEntity user) {
        BoardEntity board = getBoardByUserAndId(user, id);
        return BoardDetailResponse.toDto(BoardResponse.toDto(board), board);
    }

    public PageResponse<BoardResponse> searchBoards(List<Long> categoryList, String keyword, List<String> sort, int page,
                                            UserEntity user) {
        QBoardEntity boardEntity = QBoardEntity.boardEntity;
        BooleanBuilder builder = new BooleanBuilder();

        OrderSpecifier<?>[] orders = new OrderSpecifier[3];

        for(String s : sort) {
            String[] arr= s.split(","); //arr[0]= title, arr[1]= asc
            if(arr.length != 2) {
                CommonThrow.invalidRequest("지원하지 않는 정렬 값: " + sort);
            }
            String field = arr[0].trim().toLowerCase();
            String value = arr[1].trim().toLowerCase();

            if(!value.equals("asc") && !value.equals("desc")) {
                CommonThrow.invalidRequest("지원하지 않는 정렬 값: " + sort);
            }

            switch (field) { //->: 자동 break 처리
                case "date" ->
                        orders[0]= value.equals("desc")? boardEntity.upload_at.desc() : boardEntity.upload_at.asc();
                case "category" ->
                        orders[1]= value.equals("desc")? boardEntity.category.name.desc() : boardEntity.category.name.asc();
                case "title" ->
                    orders[2]= value.equals("desc")? boardEntity.title.desc() : boardEntity.title.asc();
                default -> CommonThrow.invalidRequest("지원하지 않는 정렬 값: " + sort);

            }
        }

        if (orders[0] == null || orders[1] == null || orders[2] == null) {
            CommonThrow.invalidRequest("지원하지 않는 정렬 값: " + sort);
        }

        builder.and(boardEntity.user.eq(user));
        if(!categoryList.isEmpty()) {
            builder.and(boardEntity.category.id.in(categoryList));
        }
        if(keyword != null && !keyword.isEmpty())
            builder.and(boardEntity.title.like("%" + keyword + "%"));

        long pageSize= 30;
        long offset= (page - 1) * pageSize; //페이지당 30건 반환

        List<BoardResponse> boardResponses = queryFactory
                .select(
                        Projections.constructor(
                        BoardResponse.class,
                        boardEntity.id,
                        boardEntity.category.id,
                        boardEntity.title,
                        boardEntity.content,
                        boardEntity.update_at,
                        boardEntity.update_at
                ))
                .from(boardEntity)
                .where(builder)
                .orderBy(orders)
                .offset(offset) //이 Index부터 데이터 조회
                .limit(pageSize) //페이지 사이즈
                .fetch();

        Long totalItems= queryFactory
                .select(boardEntity.count())
                .from(boardEntity)
                .where(builder)
                .fetchOne(); //count()는 항상 row가 하나씩 있어서 0 이상 반환
        log.info("{}", totalItems);

        long totalPages= (totalItems + pageSize - 1) / pageSize;

        return new PageResponse<>(boardResponses, totalItems, totalPages, page, pageSize);
    }

    public BoardDetailResponse createBoard(BoardCreateRequest request, String draftId, UserEntity user) {
        //board의 category는 categoryEntity타입으로 조회하고 엔티티로 받기
        CategoryEntity category= categoryRepository.findByUserAndId(user, request.getCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        BoardEntity board = request.toEntity(user, category);
        boardRepository.saveAndFlush(board); // 이때는 board.id 있음

        fileService.attachDraftFilesToBoard(user, board, draftId);
        return BoardDetailResponse.toDto(BoardResponse.toDto(board), board);
    }

    public BoardDetailResponse updateBoard(Long id, BoardUpdateRequest request, String draftId, UserEntity user) {
        BoardEntity board = getBoardByUserAndId(user, id);
        CategoryEntity category= categoryRepository.findByUserAndId(user, request.getCategoryId())
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