package com.hotel.infrastructure.security.config;

import com.hotel.domain.exception.EmailAlreadyExistsException;
import com.hotel.domain.exception.UnauthorizedException;
import com.hotel.domain.model.User;
import com.hotel.domain.repository.UserRepository;
import com.hotel.domain.service.UserDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * INFRASTRUCTURE LAYER — Domain service implementation.
 * Lives in infrastructure because it uses Spring's PasswordEncoder.
 */
@Service
@RequiredArgsConstructor
public class UserDomainServiceImpl implements UserDomainService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Override
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean passwordMatches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public void ensureEmailIsUnique(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
    }

    @Override
    public void ensureUserIsActive(User user) {
        if (!user.isActive()) {
            throw UnauthorizedException.accountInactive();
        }
    }
}