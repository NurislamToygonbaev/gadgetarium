package gadgetarium.config.jwt;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.google.common.net.HttpHeaders;
import gadgetarium.entities.User;
import gadgetarium.repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepo;
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String headerToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        String bearer = "Bearer ";
        if (headerToken != null && headerToken.startsWith(bearer)) {
            String token = headerToken.substring(bearer.length());

            try {
                String email = jwtService.verifyToken(token);
                User user = userRepo.getByEmail(email);
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                user.getUsername(),
                                null,
                                user.getAuthorities()
                        )
                );
            } catch (JWTVerificationException jwtVerificationException) {
                logger.error("Both Firebase and JWT authentication failed: " + jwtVerificationException.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                        "Invalid JWT token");
            }
        }
        chain.doFilter(request, response);
    }
}

