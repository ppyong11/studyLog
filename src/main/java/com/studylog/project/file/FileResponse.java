package com.studylog.project.file;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class FileResponse {
    private Long id;
    private String fileName;
    private Long fileSize;
    private String url;

    public static FileResponse toDto(FileEntity file) {
        String url= "/files/"+file.getId();
        return new FileResponse(file.getId(), file.getOriginalName(),
                file.getSize(), url);
    }
}
