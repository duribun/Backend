package duribun.be.domain.auth.controller;

import duribun.be.domain.auth.dto.LoginRequest;
import duribun.be.domain.auth.dto.LoginResponse;
import duribun.be.domain.auth.dto.ReissueRequest;
import duribun.be.domain.auth.dto.TokenResponse;
import duribun.be.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login/{provider}")
    public ResponseEntity<LoginResponse> login(@PathVariable String provider, @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(provider, request.token()));
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(@Valid @RequestBody ReissueRequest request) {
        return ResponseEntity.ok(authService.reissue(request.refreshToken()));
    }
}
