package com.studylog.project.category;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studylog.project.board.BoardService;
import com.studylog.project.global.PageResponse;
import com.studylog.project.global.ScrollResponse;
import com.studylog.project.global.exception.BadRequestException;
import com.studylog.project.global.exception.DuplicateException;
import com.studylog.project.global.exception.NotFoundException;
import com.studylog.project.plan.PlanService;
import com.studylog.project.timer.TimerService;
import com.studylog.project.user.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final JPAQueryFactory queryFactory;
    private final PlanService planService;
    private final BoardService boardService;
    private final TimerService timerService;

    public void defaultCategory(UserEntity user) {
        CategoryEntity category = CategoryEntity.builder()
                .user_id(user) //알아서 long타입으로 들어감
                .category_name("기타")
                .build();
        categoryRepository.save(category);
        log.info("기본 카테고리 저장 완료");
    }

    //카테고리 전체&키워드 조회
    public ScrollResponse<CategoryResponse> searchCategories(String keyword, int page, UserEntity user) {
        QCategoryEntity categoryEntity = QCategoryEntity.categoryEntity;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(categoryEntity.user.eq(user)); //이거만 해도 전체 카테고리 나옴

        if (keyword != null && !keyword.isEmpty()) {
            builder.and(categoryEntity.name.like('%' + keyword + '%'));
        }

        long pageSize= 10;
        long offset= (page-1) * pageSize; //페이지당 10건씩 반환 (0~9, 10~19)
        List<CategoryResponse> responses = queryFactory
                .select(Projections.constructor(
                        CategoryResponse.class,
                        categoryEntity.id,
                        categoryEntity.name
                ))
                .from(categoryEntity)
                .where(builder)
                .orderBy(categoryEntity.name.asc())
                .offset(offset)
                .limit(pageSize)
                .fetch();

        Long totalItems= queryFactory
                .select(categoryEntity.count())
                .from(categoryEntity)
                .where(builder)
                .fetchOne();

        boolean hasNext= page * pageSize < totalItems;

        //빈 리스트여도 문제 없이 controller에 빈 리스트로 반환돼서 위에서 에러 터뜨림
        return new ScrollResponse<>(responses, totalItems, page, pageSize, hasNext);
    }

    //카테고리 단일 조회
    public CategoryResponse getCategory(Long id, UserEntity user) {
        CategoryEntity category = categoryRepository.findByUserAndId(user, id)
                .orElseThrow(()-> new NotFoundException("존재하지 않는 카테고리입니다."));
        return new CategoryResponse(category.getId(), category.getName());
    }

    public void addCategory(CategoryRequest request, UserEntity user) {
        log.info("{}",user.getUser_id());
        if (categoryRepository.existsByUserAndName(user, request.getName())){
            throw new DuplicateException("동일한 카테고리가 있습니다.");
        }
        CategoryEntity category= CategoryEntity.builder()
                .category_name(request.getName())
                .user_id(user)
                .build();
        categoryRepository.save(category);
        log.info("새 카테고리 저장 완료");
    }

    public void updateCategory(Long id, CategoryRequest request, UserEntity user) {
        //카테고리 엔티티 가져옴
        CategoryEntity category= categoryRepository.findByUserAndId(user, id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 카테고리입니다."));

        if(category.getName().equals("기타"))
            throw new BadRequestException("해당 카테고리는 수정할 수 없습니다.");

        if (categoryRepository.existsByUserAndName(user, request.getName().trim())){
            if(!category.getName().equals(request.getName().trim()))
                throw new DuplicateException("동일한 카테고리가 있습니다.");
        }
        category.setCategory_name(request.getName());
    }

    public void delCategory(Long id, UserEntity user) {
        //카테고리 엔티티 가져옴
        CategoryEntity deleteCategory= categoryRepository.findByUserAndId(user, id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 카테고리입니다."));
        if (deleteCategory.getName().equals("기타"))
            throw new BadRequestException("해당 카테고리는 삭제할 수 없습니다.");

        //유저의 기본 카테고리 조회 후 적용
        CategoryEntity defaultCategory= categoryRepository.findByUserAndName(user, "기타")
                .orElseThrow(()-> new NotFoundException("기타 카테고리가 없습니다."));

        planService.updateCategory(deleteCategory,defaultCategory);
        boardService.updateCategory(deleteCategory, defaultCategory);
        timerService.updateCategory(deleteCategory, defaultCategory);

        categoryRepository.delete(deleteCategory);
    }
}

