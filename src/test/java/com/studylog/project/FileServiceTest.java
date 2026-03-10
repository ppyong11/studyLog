package com.studylog.project;

import com.studylog.project.file.FileEntity;
import com.studylog.project.file.FileRepository;
import com.studylog.project.file.FileService;
import com.studylog.project.user.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileServiceTest {
    @Mock
    private FileRepository fileRepository;

    @Spy
    @InjectMocks
    private FileService fileService; // fileRepository가 주입된 실제 Service

    @Test
    void 파일_업로드_성공() {
        // given
        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                "hello.png",
                "image/png",
                new byte[10]
        );

        UserEntity user = UserEntity.testBuilder().user_id(1L).build();
        String draftId = "draft-123";

        // savedName 값이 됨
        doReturn("fake.jpg").when(fileService).saveFile(any());

        fileService.uploadTempFile(multipartFile, draftId, user);

        // then
        Mockito.verify(fileRepository).save(Mockito.argThat(saved -> {
            // 여기서 저장된 FileEntity 값 검증
            return saved.getUser().equals(user)
                    && saved.getBoard() == null
                    && saved.getDraftId().equals(draftId)
                    && saved.getSize() == multipartFile.getSize()
                    && saved.getPath().endsWith("/home/ubuntu/app-data/uploads/fake.jpg")
                    && saved.getOriginalName().equals("hello.png")
                    && saved.getName().equals("fake.jpg") // saveFile 스텁 결과
                    && saved.getType().equals("image/png");
        }));
    }

    @Test
    void 임시_파일_삭제_성공() {
        UserEntity user = UserEntity.testBuilder().user_id(1L).build();
        FileEntity file = FileEntity.testBuilder()
                .id(1L)
                .user(user)
                .board(null)
                .draftId("test-123")
                .build();

        when(fileRepository.findByUserAndIdAndDraftId(user, 1L, "test-123"))
                .thenReturn(Optional.of(file));

        fileService.deleteTempMeta(1L, "test-123", user);

        verify(fileRepository).delete(file);
    }
}
