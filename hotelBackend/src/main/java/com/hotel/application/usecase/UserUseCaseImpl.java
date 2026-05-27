package com.hotel.application.usecase;

import com.hotel.application.dto.request.ChangePasswordRequest;
import com.hotel.application.dto.request.UpdateUserRequest;
import com.hotel.application.dto.response.UserResponse;
import com.hotel.domain.exception.UnauthorizedException;
import com.hotel.domain.exception.UserNotFoundException;
import com.hotel.domain.model.Role;
import com.hotel.domain.model.User;
import com.hotel.domain.repository.UserRepository;
import com.hotel.domain.service.UserDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * APPLICATION LAYER — UserUseCase Implementation.
 */
@Service
@Transactional
public class UserUseCaseImpl implements UserUseCase {

    private static final Logger log = LoggerFactory.getLogger(UserUseCaseImpl.class);

    private final UserRepository userRepository;
    private final UserDomainService userDomainService;

    public UserUseCaseImpl(UserRepository userRepository, UserDomainService userDomainService) {
        this.userRepository    = userRepository;
        this.userDomainService = userDomainService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findByRole(role)
                .stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return UserResponse.from(findUserOrThrow(id));
    }

    @Override
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = findUserOrThrow(id);

        if (!user.getEmail().equalsIgnoreCase(request.email())) {
            userDomainService.ensureEmailIsUnique(request.email());
        }

        user.updateProfile(request.name(), request.email());

        if (request.role() != null) {
            user.changeRole(request.role());
        }

        User updated = userRepository.save(user);
        log.info("User {} updated successfully", id);
        return UserResponse.from(updated);
    }

    @Override
    public void changePassword(Long id, ChangePasswordRequest request) {
        User user = findUserOrThrow(id);

        if (!userDomainService.passwordMatches(request.oldPassword(), user.getPassword())) {
            throw new UnauthorizedException("Ancien mot de passe incorrect");
        }

        String encoded = userDomainService.encodePassword(request.newPassword());
        user.changePassword(encoded);
        userRepository.save(user);
        log.info("Password changed for user {}", id);
    }

    @Override
    public void deactivateUser(Long id) {
        User user = findUserOrThrow(id);
        user.deactivate();
        userRepository.save(user);
        log.info("User {} deactivated", id);
    }

    @Override
    public void activateUser(Long id) {
        User user = findUserOrThrow(id);
        user.activate();
        userRepository.save(user);
        log.info("User {} activated", id);
    }

    @Override
    public void deleteUser(Long id) {
        findUserOrThrow(id);
        userRepository.deleteById(id);
        log.info("User {} deleted", id);
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}