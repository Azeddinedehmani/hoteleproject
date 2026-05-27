package com.hotel.application.usecase;

import com.hotel.application.dto.request.CreateEquipmentRequest;
import com.hotel.application.dto.request.UpdateEquipmentRequest;
import com.hotel.application.dto.response.EquipmentResponse;

import java.util.List;

/**
 * APPLICATION LAYER — Equipment Use Case Port.
 * Endpoints frontend :
 *   GET    /equipment
 *   POST   /equipment
 *   PUT    /equipment/{id}
 *   DELETE /equipment/{id}
 */
public interface EquipmentUseCase {

    List<EquipmentResponse> getAllEquipment();

    EquipmentResponse getEquipmentById(Long id);

    EquipmentResponse createEquipment(CreateEquipmentRequest request);

    EquipmentResponse updateEquipment(Long id, UpdateEquipmentRequest request);

    void deleteEquipment(Long id);
}