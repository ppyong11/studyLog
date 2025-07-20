package com.studylog.project.board;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.studylog.project.file.FileResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class BoardDetailResponse {
    private Long id;
    private String categoryName;
    private String title;
    private String content;
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yy-MM-dd HH:mm:ss")
    private LocalDateTime uploadAt;
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yy-MM-dd HH:mm:ss")
    private LocalDateTime updateAt;
    private List<FileResponse> files;

    public static BoardDetailResponse toDto(BoardEntity board) {
        List<FileResponse> files = board.getFiles().stream()
                .filter(f -> !f.isDraft()) //임시 파일은 제외
                .map(FileResponse::toDto)
                .collect(Collectors.toList());

        return new BoardDetailResponse(board.getId(), board.getCategory().getName(),
                board.getTitle(), board.getContent(), board.getUpload_at(), board.getUpdate_at(),
                files);
    }
}
