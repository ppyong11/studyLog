package com.studylog.project.Main;

import com.studylog.project.jwt.CustomUserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/main")
public class MainController {
    private final MainService mainService;

    @GetMapping({"", "/"})
    public ResponseEntity<MainResponse> getMain(@AuthenticationPrincipal CustomUserDetail user){
        return ResponseEntity.ok(mainService.buildMainPage(user.getUser()));
    }
}
