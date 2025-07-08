package com.studylog.project.board;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class BoardResponse {
    private Long id;
    private String categoryName;
    private String title;
    private String content;
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yy-MM-dd HH:mm:ss")
    private LocalDateTime uploadAt;
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yy-MM-dd HH:mm:ss")
    private LocalDateTime updateAt;

    public static BoardResponse toDto(BoardEntity board) {
        return new BoardResponse(board.getId(), board.getCategory().getName(),
                board.getTitle(), board.getContent(), board.getUpload_at(), board.getUpdate_at());
    }
}
