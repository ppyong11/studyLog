package com.studylog.project.board;

import com.studylog.project.file.FileResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class BoardDetailResponse {
    private BoardResponse board;
    @Schema(description = "게시글 파일 목록")
    private List<FileResponse> files;

    public static BoardDetailResponse toDto(BoardResponse boardResponse, BoardEntity board) {
        List<FileResponse> files = board.getFiles().stream()
                .filter(f -> !f.isDraft()) //임시 파일은 제외
                .map(FileResponse::toDto)
                .collect(Collectors.toList());

        return new BoardDetailResponse(boardResponse, files);
    }
}
