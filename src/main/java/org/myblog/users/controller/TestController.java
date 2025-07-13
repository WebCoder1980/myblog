package org.myblog.users.controller;

import org.myblog.users.dto.AppResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
    @GetMapping("/all")
    public ResponseEntity<AppResponse<String>> allAccess() {
        return ResponseEntity.ok().body(new AppResponse<>("Public Content."));
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<AppResponse<String>> userAccess() {
      return ResponseEntity.ok().body(new AppResponse<>("User Content."));
    }

    @GetMapping("/mod")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<AppResponse<String>> moderatorAccess() {
      return ResponseEntity.ok().body(new AppResponse<>("Moderator Content."));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppResponse<String>> adminAccess() {
      return ResponseEntity.ok().body(new AppResponse<>("Admin Content."));
    }
}
