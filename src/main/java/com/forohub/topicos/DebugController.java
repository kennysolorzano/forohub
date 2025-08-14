package com.forohub.topicos;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class DebugController {

  @GetMapping("/debug/whoami")
  public ResponseEntity<?> whoami(Authentication auth) {
    if (auth == null) {
      return ResponseEntity.ok(Map.of(
          "authenticated", false,
          "principal", null,
          "authorities", null
      ));
    }
    return ResponseEntity.ok(Map.of(
        "authenticated", auth.isAuthenticated(),
        "principal", String.valueOf(auth.getPrincipal()),
        "authorities", auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority).collect(Collectors.toList())
    ));
  }
}
