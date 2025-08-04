package com.studylog.project.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class FileResponse {
    @Schema(description = "파일 id", example = "1")
    private Long id;
    @Schema(description = "파일명", example = "명세.png")
    private String fileName;
    @Schema(description = "파일 크기", example = "652KB")
    private String fileSize;
    @Schema(description = "파일 경로", example = "/files/1")
    private String url;

    public static FileResponse toDto(FileEntity file) {
        String url= "/files/"+file.getId();
        return new FileResponse(file.getId(), file.getOriginalName(),
                readableFileSize(file.getSize()), url);
    }

    //long to 파일 크기 포맷 변환
    private static String readableFileSize(long size){
        if(size <= 0) return "0B";
        final String[] units= new String[]{"B","KB", "MB", "GB", "TB"};
        int digitGroups= (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f%s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}
