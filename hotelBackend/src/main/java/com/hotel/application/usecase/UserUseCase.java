package com.hotel.application.usecase;

import com.hotel.application.dto.request.ChangePasswordRequest;
import com.hotel.application.dto.request.UpdateUserRequest;
import com.hotel.application.dto.response.UserResponse;
import com.hotel.domain.model.Role;

import java.util.List;

/**
 * APPLICATION LAYER — User Management Use Case Port
 */
public interface UserUseCase {

    List<UserResponse> getAllUsers();

    List<UserResponse> getUsersByRole(Role role);

    UserResponse getUserById(Long id);

    UserResponse updateUser(Long id, UpdateUserRequest request);

    void changePassword(Long id, ChangePasswordRequest request);

    void deactivateUser(Long id);

    void activateUser(Long id);

    void deleteUser(Long id);
}