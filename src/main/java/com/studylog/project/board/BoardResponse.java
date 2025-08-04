package com.studylog.project.board;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class BoardResponse {
    @Schema(description = "게시글 id", example = "1")
    private Long id;
    @Schema(description = "카테고리명", example = "기타")
    private String categoryName;
    @Schema(description = "게시글명", example = "swagger 공부")
    private String title;
    @Schema(description = "게시글 내용", example = "swagger는 API 설계와 문서화에 좋은 도구입니다.")
    private String content;
    @Schema(description = "게시글 작성일", example = "2025-07-18 15:37:07", type = "string")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadAt;
    @Schema(description = "게시글 수정일", example = "2025-07-18 15:47:00", type = "string")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateAt;

    public static BoardResponse toDto(BoardEntity board) {
        return new BoardResponse(board.getId(), board.getCategory().getName(),
                board.getTitle(), board.getContent(), board.getUpload_at(), board.getUpdate_at());
    }
}
