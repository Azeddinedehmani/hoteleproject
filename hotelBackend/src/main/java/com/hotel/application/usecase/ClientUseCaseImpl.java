package com.hotel.application.usecase;

import com.hotel.application.dto.request.CreateClientRequest;
import com.hotel.application.dto.request.UpdateClientRequest;
import com.hotel.application.dto.response.ClientResponse;
import com.hotel.domain.exception.ClientAlreadyExistsException;
import com.hotel.domain.exception.ClientNotFoundException;
import com.hotel.domain.model.Client;
import com.hotel.domain.model.Role;
import com.hotel.domain.model.User;
import com.hotel.domain.repository.ClientRepository;
import com.hotel.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * APPLICATION LAYER — ClientUseCase Implementation.
 *
 * FIX : createClient() crée désormais aussi un User avec le rôle CLIENT
 *       afin que le client puisse se connecter via /auth/login.
 *       Le mot de passe par défaut est son numéro CIN (ou son email si pas de CIN).
 */
@Service
@Transactional
public class ClientUseCaseImpl implements ClientUseCase {

    private static final Logger log = LoggerFactory.getLogger(ClientUseCaseImpl.class);

    private final ClientRepository clientRepository;
    private final UserRepository   userRepository;      // ← AJOUT
    private final PasswordEncoder  passwordEncoder;      // ← AJOUT

    public ClientUseCaseImpl(ClientRepository clientRepository,
                             UserRepository userRepository,
                             PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.userRepository   = userRepository;
        this.passwordEncoder  = passwordEncoder;
    }

    // ────────────────── Queries ──────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ClientResponse> getAllClients() {
        return clientRepository.findAll()
                .stream()
                .map(ClientResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse getClientById(Long id) {
        return ClientResponse.from(findOrThrow(id));
    }

    // ────────────────── Commands ──────────────────

    @Override
    public ClientResponse createClient(CreateClientRequest request) {
        if (clientRepository.existsByEmail(request.email())) {
            throw new ClientAlreadyExistsException("email", request.email());
        }
        if (request.cin() != null && !request.cin().isBlank()
                && clientRepository.existsByCin(request.cin())) {
            throw new ClientAlreadyExistsException("CIN", request.cin());
        }

        // 1. Créer le client dans la table clients
        Client client = Client.create(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phone(),
                request.cin()
        );
        Client saved = clientRepository.save(client);
        log.info("Client créé — id={}, email={}", saved.getId(), saved.getEmail());

        // 2. Créer le compte User correspondant (si pas déjà existant)
        //    ➜ mot de passe par défaut = CIN (ou email si pas de CIN)
        if (!userRepository.existsByEmail(request.email())) {
            String rawPassword = (request.cin() != null && !request.cin().isBlank())
                    ? request.cin()
                    : request.email();

            String displayName = request.firstName() + " " + request.lastName();

            User clientUser = User.create(
                    displayName,
                    request.email(),
                    passwordEncoder.encode(rawPassword),
                    Role.CLIENT
            );
            userRepository.save(clientUser);
            log.info("Compte CLIENT créé — email={}, mdp_defaut={}",
                    request.email(),
                    request.cin() != null && !request.cin().isBlank() ? "[CIN]" : "[email]");
        } else {
            log.info("Compte User déjà existant pour email={} — rôle non modifié", request.email());
        }

        return ClientResponse.from(saved);
    }

    @Override
    public ClientResponse updateClient(Long id, UpdateClientRequest request) {
        Client client = findOrThrow(id);

        if (!client.getEmail().equalsIgnoreCase(request.email())
                && clientRepository.existsByEmail(request.email())) {
            throw new ClientAlreadyExistsException("email", request.email());
        }
        if (request.cin() != null && !request.cin().isBlank()
                && !request.cin().equalsIgnoreCase(client.getCin())
                && clientRepository.existsByCin(request.cin())) {
            throw new ClientAlreadyExistsException("CIN", request.cin());
        }

        client.updateProfile(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phone(),
                request.cin()
        );

        Client updated = clientRepository.save(client);
        log.info("Client mis à jour — id={}", updated.getId());
        return ClientResponse.from(updated);
    }

    @Override
    public void deleteClient(Long id) {
        findOrThrow(id);
        clientRepository.deleteById(id);
        log.info("Client supprimé — id={}", id);
    }

    // ────────────────── Helper ──────────────────

    private Client findOrThrow(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));
    }
}