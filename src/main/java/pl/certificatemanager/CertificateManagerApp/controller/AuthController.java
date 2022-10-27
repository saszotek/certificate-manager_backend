package pl.certificatemanager.CertificateManagerApp.controller;

import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.certificatemanager.CertificateManagerApp.dto.AuthCredentialsRequest;
import pl.certificatemanager.CertificateManagerApp.model.User;
import pl.certificatemanager.CertificateManagerApp.util.JwtUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthCredentialsRequest request) {
        try {
            Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            User user = (User) authenticate.getPrincipal();

            return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, jwtUtil.generateToken(user)).body(user);
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam String token, @AuthenticationPrincipal User user) {
        try {
            Boolean isTokenValid = jwtUtil.validateToken(token, user);
            return ResponseEntity.ok(isTokenValid);
        } catch (ExpiredJwtException e) {
            return ResponseEntity.ok(false);
        }
    }
}
