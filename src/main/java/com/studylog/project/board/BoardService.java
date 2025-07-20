package com.studylog.project.board;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studylog.project.category.CategoryEntity;
import com.studylog.project.category.CategoryRepository;
import com.studylog.project.file.FileEntity;
import com.studylog.project.file.FileRepository;
import com.studylog.project.global.exception.BadRequestException;
import com.studylog.project.global.exception.NotFoundException;
import com.studylog.project.user.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final CategoryRepository categoryRepository;
    private final JPAQueryFactory queryFactory;
    private final FileRepository fileRepository;

    public BoardDetailResponse getBoard(Long id, UserEntity user) {
        BoardEntity board = getBoardByUserAndId(user, id);
        return BoardDetailResponse.toDto(board);
    }

    public List<BoardResponse> searchBoards(List<Long> categoryList, String keyword, List<String> sort,
                                            UserEntity user) {
        log.info("메서드 진입");
        QBoardEntity boardEntity = QBoardEntity.boardEntity;
        BooleanBuilder builder = new BooleanBuilder();

        List<OrderSpecifier<?>> orders = new ArrayList<>();
        //sort에 이상한 값 있으면 에러
        for(String s : sort) {
            //s가 "title,asc/desc" or "category,asc/desc" 아니면 에러

            String[] arr= s.split(","); //arr[0]= title, arr[1]= asc
            if(arr.length != 2) {
                throw new BadRequestException("지원하지 않는 정렬입니다.");
            }
            String field = arr[0].trim().toLowerCase();
            String value = arr[1].trim().toLowerCase();
            if(!value.equals("asc") && !value.equals("desc"))
                throw new BadRequestException("지원하지 않는 정렬입니다.");

            switch (field) { //->: 자동 break 처리
                case "title" ->
                    orders.add(value.equals("desc")? boardEntity.title.desc() : boardEntity.title.asc());

                case "category" ->
                    orders.add(value.equals("desc")? boardEntity.category.name.desc() : boardEntity.category.name.asc());
                default -> throw new BadRequestException("지원하지 않는 정렬입니다."); //field 이상하면 여기서 걸림
            }
        }

        builder.and(boardEntity.user.eq(user));
        if(!categoryList.isEmpty()) {
            builder.and(boardEntity.category.id.in(categoryList));
        }
        if(keyword != null && !keyword.isEmpty())
            builder.and(boardEntity.title.like("%" + keyword + "%"));

        List<BoardEntity> boards= queryFactory.selectFrom(boardEntity)
                .where(builder)
                .orderBy(orders.toArray(new OrderSpecifier[0])) //orderBy 메서드는 OrderSpecifier "배열"만 받아서 리스트 to 배열하는 과정
                //길이가 0인 배열을 만들어서 리스트 요소를 그 배열에 넣겠다! -> 타입 맞춰주는 역할로, orders 크기만큼 배열이 새로 생김 (배열은 크기 수정 X)
                //위에서 list 말고 배열로 만들어도 되는데, 혹시 모를 에러에 방어하기 위해 동적 리스트 활용하나봄 (실무적임)
                .fetch();

        return boards.stream()
                .map(board -> BoardResponse.toDto(board)) //하나씩 들어가서 DTO로 변환됨
                .toList();
    }

    public BoardDetailResponse createBoard(BoardCreateRequest request, UserEntity user) {
        if(request.getDraftId() == null) throw new BadRequestException("게시글 고유 값이 없습니다.");
        //board의 category는 categoryEntity타입으로 조회하고 엔티티로 받기
        CategoryEntity category= categoryRepository.findByUserAndId(user, request.getCategoryId())
                .orElseThrow(() -> new BadRequestException("존재하지 않는 카테고리입니다."));

        BoardEntity board = request.toEntity(user, category);
        boardRepository.saveAndFlush(board); //이때는 board.id 있음
        List<FileEntity> files= fileRepository.findAllByUserAndDraftId(user, request.getDraftId());
        for (FileEntity file : files) {
            file.updateBoard(board);
            file.resetDraftIdAndDraftFalse(); //임시파일 게시글 연결됐으니까 초기화
            board.getFiles().add(file); //파일 반영 안 돼서 직접 추가
        }
        return BoardDetailResponse.toDto(board);
    }

    public BoardDetailResponse updateBoard(Long id, BoardUpdateRequest request, UserEntity user) {
        BoardEntity board = getBoardByUserAndId(user, id);
        CategoryEntity category= categoryRepository.findByUserAndId(user, request.getCategoryId())
                .orElseThrow(() -> new BadRequestException("존재하지 않는 카테고리입니다."));

        board.updateBoard(category, request);
        List<FileEntity> files= fileRepository.findAllByUserAndBoard(user, board);
        for(FileEntity file : files) {
            file.resetDraftIdAndDraftFalse();
        }
        return BoardDetailResponse.toDto(board);

    }

    public void deleteBoard(Long id, UserEntity user) {
        BoardEntity board= getBoardByUserAndId(user, id);
        boardRepository.delete(board);
        log.info("게시글 삭제 완료");
    }

    private BoardEntity getBoardByUserAndId(UserEntity user, Long id) {
        return boardRepository.findByUserAndId(user, id).orElseThrow(() -> new NotFoundException("존재하지 않는 게시글입니다."));
    }
}
