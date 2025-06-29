package com.studylog.project.category;

import com.studylog.project.board.BoardEntity;
import com.studylog.project.board.BoardRepository;
import com.studylog.project.global.exception.AccessDeniedException;
import com.studylog.project.global.exception.BadRequestException;
import com.studylog.project.global.exception.DuplicateException;
import com.studylog.project.global.exception.NotFoundException;
import com.studylog.project.plan.PlanEntity;
import com.studylog.project.plan.PlanRepository;
import com.studylog.project.user.UserEntity;
import com.studylog.project.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final PlanRepository planRepository;
    private final BoardRepository boardRepository;

    public void defaultCategory(UserEntity user) {
        CategoryEntity category = CategoryEntity.builder()
                .user_id(user) //알아서 long타입으로 들어감
                .category_name("기타")
                .build();
        categoryRepository.save(category);
        log.info("기본 카테고리 저장 완료");
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
                .orElseThrow(() -> new NotFoundException("해당 카테고리 없음"));
        //카테고리 소유자가 아니라면
        if (!category.getUser().getId().equals(user.getId()))
            throw new AccessDeniedException("요청 권한이 없습니다.");
        if(category.getName().equals("기타"))
            throw new BadRequestException("해당 카테고리는 수정할 수 없습니다.");
        if (categoryRepository.existsByUserAndName(user, request.getName()))
            throw new DuplicateException("동일한 카테고리가 있습니다.");
        category.setCategory_name(request.getName());
    }

    public void delCategory(Long id, UserEntity user) {
        //카테고리 엔티티 가져옴
        CategoryEntity category= categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 카테고리 없음"));
        //카테고리 소유자가 아니라면
        if (!category.getUser().getId().equals(user.getId()))
            throw new AccessDeniedException("요청 권한이 없습니다.");
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
            board.updateCategory(defaultCategory, LocalDateTime.now());
        }
        categoryRepository.delete(category);
    }
}

