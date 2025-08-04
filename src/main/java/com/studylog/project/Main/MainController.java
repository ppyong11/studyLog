package com.studylog.project.Main;

import com.studylog.project.jwt.CustomUserDetail;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/main")
@Tag(name="Main", description = "메인화면 API, 모든 요청 access token 필요")
public class MainController {
    private final MainService mainService;

    @GetMapping({"", "/"})
    public ResponseEntity<MainResponse> getMain(@AuthenticationPrincipal CustomUserDetail user){
        return ResponseEntity.ok(mainService.buildMainPage(user.getUser()));
    }
}
