package com.studylog.project.category;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studylog.project.board.BoardEntity;
import com.studylog.project.board.BoardRepository;
import com.studylog.project.global.exception.BadRequestException;
import com.studylog.project.global.exception.DuplicateException;
import com.studylog.project.global.exception.NotFoundException;
import com.studylog.project.plan.PlanEntity;
import com.studylog.project.plan.PlanRepository;
import com.studylog.project.timer.TimerEntity;
import com.studylog.project.timer.TimerRepository;
import com.studylog.project.user.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final PlanRepository planRepository;
    private final BoardRepository boardRepository;
    private final JPAQueryFactory queryFactory;
    private final TimerRepository timerRepository;

    public void defaultCategory(UserEntity user) {
        CategoryEntity category = CategoryEntity.builder()
                .user_id(user) //알아서 long타입으로 들어감
                .category_name("기타")
                .build();
        categoryRepository.save(category);
        log.info("기본 카테고리 저장 완료");
    }

    //카테고리 전체&키워드 조회
    public List<CategoryResponse> searchCategories(String keyword, String sort,UserEntity user) {
        QCategoryEntity categoryEntity = QCategoryEntity.categoryEntity;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(categoryEntity.user.eq(user)); //이거만 해도 전체 카테고리 나옴

        if (keyword != null && !keyword.isEmpty()) {
            builder.and(categoryEntity.name.like('%' + keyword + '%'));
        }

        OrderSpecifier<?> order= sort.equals("desc")? categoryEntity.name.desc():categoryEntity.name.asc();

        List<CategoryEntity> categories = queryFactory.selectFrom(categoryEntity)
                .where(builder)
                .orderBy(order)
                .fetch();

        if (categories.isEmpty()) {
            throw new NotFoundException("카테고리가 존재하지 않습니다."); //기타 카테고리는 필수라서 에러 나감
        }
        //빈 리스트여도 문제 없이 controller에 빈 리스트로 반환돼서 위에서 에러 터뜨림
        return categories.stream()
                .map(category -> CategoryResponse.toDto(category))
                .toList();
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
        CategoryEntity category= categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 카테고리입니다."));
        //카테고리 소유자가 아니라면
        if (!category.getUser().getId().equals(user.getId()))
            throw new NotFoundException("존재하지 않는 카테고리입니다.");
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
        CategoryEntity category= categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 카테고리입니다."));
        //카테고리 소유자가 아니라면
        if (!category.getUser().getId().equals(user.getId()))
            throw new NotFoundException("존재하지 않는 카테고리입니다.");
        if (category.getName().equals("기타"))
            throw new BadRequestException("해당 카테고리는 삭제할 수 없습니다.");

        //유저의 기본 카테고리 조회 후 적용
        CategoryEntity defaultCategory= categoryRepository.findByUserAndName(user, "기타")
                .orElseThrow(()-> new NotFoundException("기타 카테고리가 없습니다."));

        List<PlanEntity> plans= planRepository.findByCategory(category);
        for (PlanEntity plan : plans) {
            //pk로 알아서 지정됨
            plan.updateCategory(defaultCategory);
        }

        List<BoardEntity> boards= boardRepository.findByCategory(category);
        for (BoardEntity board : boards) {
            board.updateCategory(defaultCategory);
        }

        List<TimerEntity> timers= timerRepository.findAllByCategory(category);
        for (TimerEntity timer : timers) {
            timer.updateCategory(defaultCategory); //생성할 때 플랜.카테고리로 만든 카테고리라도 동기화 필요함 (이어져있지 X)
        }
        categoryRepository.delete(category);
    }
}

