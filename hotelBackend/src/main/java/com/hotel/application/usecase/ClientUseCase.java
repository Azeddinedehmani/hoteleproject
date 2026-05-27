package com.hotel.application.usecase;

import com.hotel.application.dto.request.CreateClientRequest;
import com.hotel.application.dto.request.UpdateClientRequest;
import com.hotel.application.dto.response.ClientResponse;

import java.util.List;

/**
 * APPLICATION LAYER — ClientUseCase Port (inbound).
 * Defines all operations available on the Client aggregate.
 */
public interface ClientUseCase {

    /** GET /api/clients */
    List<ClientResponse> getAllClients();

    /** GET /api/clients/{id} */
    ClientResponse getClientById(Long id);

    /** POST /api/clients */
    ClientResponse createClient(CreateClientRequest request);

    /** PUT /api/clients/{id} */
    ClientResponse updateClient(Long id, UpdateClientRequest request);

    /** DELETE /api/clients/{id} */
    void deleteClient(Long id);
}