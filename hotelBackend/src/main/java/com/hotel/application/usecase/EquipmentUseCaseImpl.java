package com.hotel.application.usecase;

import com.hotel.application.dto.request.CreateEquipmentRequest;
import com.hotel.application.dto.request.UpdateEquipmentRequest;
import com.hotel.application.dto.response.EquipmentResponse;
import com.hotel.domain.exception.EquipmentNotFoundException;
import com.hotel.domain.model.Equipment;
import com.hotel.domain.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * APPLICATION LAYER — Equipment Use Case Implementation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EquipmentUseCaseImpl implements EquipmentUseCase {

    private final EquipmentRepository equipmentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentResponse> getAllEquipment() {
        log.debug("Fetching all equipment");
        return equipmentRepository.findAll()
                .stream()
                .map(EquipmentResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentResponse getEquipmentById(Long id) {
        return EquipmentResponse.from(findOrThrow(id));
    }

    @Override
    public EquipmentResponse createEquipment(CreateEquipmentRequest request) {
        log.debug("Creating equipment: {}", request.name());
        Equipment equipment = Equipment.create(
                request.name(),
                request.category(),
                request.description(),
                request.icon()
        );
        return EquipmentResponse.from(equipmentRepository.save(equipment));
    }

    @Override
    public EquipmentResponse updateEquipment(Long id, UpdateEquipmentRequest request) {
        log.debug("Updating equipment id={}", id);
        Equipment equipment = findOrThrow(id);
        equipment.update(
                request.name(),
                request.category(),
                request.description(),
                request.icon()
        );
        return EquipmentResponse.from(equipmentRepository.save(equipment));
    }

    @Override
    public void deleteEquipment(Long id) {
        log.debug("Deleting equipment id={}", id);
        if (equipmentRepository.findById(id).isEmpty()) {
            throw new EquipmentNotFoundException("Équipement introuvable: " + id);
        }
        equipmentRepository.deleteById(id);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Equipment findOrThrow(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new EquipmentNotFoundException("Équipement introuvable: " + id));
    }
}