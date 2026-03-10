package com.studylog.project.category;

import com.studylog.project.board.BoardService;
import com.studylog.project.global.exception.*;
import com.studylog.project.global.response.ScrollResponse;
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
    private final CategoryRepositoryImpl categoryRepositoryImpl;
    private final PlanService planService;
    private final BoardService boardService;
    private final TimerService timerService;

    public void defaultCategory(UserEntity user) {
        CategoryEntity category = CategoryEntity.builder()
                .user_id(user) //알아서 long타입으로 들어감
                .name("기타")
                .bgColor("#F7F7F7")
                .textColor("#484848")
                .build();
        categoryRepository.save(category);
        log.info("기본 카테고리 저장 완료");
    }

    public List<CategoryResponse> getAllCategories(UserEntity user){
        return categoryRepositoryImpl.findAllCategories(user);
    }

    //카테고리 전체&키워드 조회
    public ScrollResponse<CategoryResponse> searchCategories(String keyword, int page, UserEntity user) {

        List<CategoryResponse> responses = categoryRepositoryImpl.searchCategoriesByFilter(user, keyword, page);

        Long totalItems= categoryRepositoryImpl.totalItems(user, keyword);

        long pageSize= 10;
        boolean hasNext= page * pageSize < totalItems;

        //빈 리스트여도 문제 없이 controller에 빈 리스트로 반환돼서 위에서 에러 터뜨림
        return new ScrollResponse<>(responses, page, totalItems, hasNext);
    }

    //카테고리 단일 조회
    public CategoryResponse getCategory(Long id, UserEntity user) {
        CategoryEntity category = getCategory(user, id);

        return CategoryResponse.toDto(category);
    }

    public CategoryResponse addCategory(CategoryRequest request, UserEntity user) {
        if (categoryRepository.existsByUserAndName(user, request.name())){
            throw new CustomException(ErrorCode.CATEGORY_NAME_DUPLICATE);
        }

        CategoryEntity category= CategoryEntity.builder()
                .name(request.name())
                .user_id(user)
                .bgColor(request.bgColor())
                .textColor(request.textColor())
                .build();
        categoryRepository.save(category);

        return CategoryResponse.toDto(category);
    }

    public CategoryResponse updateCategory(Long id, CategoryRequest request, UserEntity user) {
        //카테고리 엔티티 가져옴
        CategoryEntity category= getCategory(user, id);

        if(category.getName().equals("기타")) {
            log.info("기타 카테고리 변경 요청 - 불가");
            throw new CustomException(ErrorCode.CATEGORY_NOT_MODIFIABLE);
        }

        if (categoryRepository.existsByUserAndName(user, request.name().trim())){
            if(!category.getName().equals(request.name().trim()))
                throw new CustomException(ErrorCode.CATEGORY_NAME_DUPLICATE);
        }

        category.updateCategory(request);
        return CategoryResponse.toDto(category);
    }

    public void delCategory(Long id, UserEntity user) {
        //카테고리 엔티티 가져옴
        CategoryEntity deleteCategory= getCategory(user, id);

        if (deleteCategory.getName().equals("기타")) {
            log.info("기타 카테고리 삭제 요청 - 불가");
            throw new CustomException(ErrorCode.CATEGORY_NOT_MODIFIABLE);
        }
        //유저의 기본 카테고리 조회 후 적용
        CategoryEntity defaultCategory= categoryRepository.findByUserAndName(user, "기타")
                .orElseThrow(()-> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        planService.updateCategory(deleteCategory,defaultCategory);
        boardService.updateCategory(deleteCategory, defaultCategory);
        timerService.updateCategory(deleteCategory, defaultCategory);

        categoryRepository.delete(deleteCategory);
    }

    private CategoryEntity getCategory(UserEntity user, Long id) {
        return categoryRepository.findByUserAndId(user, id)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
    }
}

