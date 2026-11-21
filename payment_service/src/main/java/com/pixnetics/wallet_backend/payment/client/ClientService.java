package com.pixnetics.wallet_backend.payment.client;

import com.pixnetics.wallet_backend.common.enums.UserRole;
import com.pixnetics.wallet_backend.common.enums.WalletType;
import com.pixnetics.wallet_backend.payment.client.dto.ClientCreationRequest;
import com.pixnetics.wallet_backend.payment.client.dto.ClientResponse;
import com.pixnetics.wallet_backend.payment.user.UserService;
import com.pixnetics.wallet_backend.payment.user.exception.UserNotFoundException;
import com.pixnetics.wallet_backend.payment.client.exception.PublicKeyAlreadyRegisteredException;
import com.pixnetics.wallet_backend.payment.user.User;
import com.pixnetics.wallet_backend.payment.user.exception.UsernameAlreadyExistsException;
import com.pixnetics.wallet_backend.payment.wallet.WalletService;
import com.pixnetics.wallet_backend.payment.wallet.dto.WalletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class ClientService {
    private final WalletService walletService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public ClientResponse getById(UUID id) {
        log.info("Getting client by ID: {}", id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        List<WalletResponse> wallets = walletService.getClientWalletsById(client.getId());

        return new ClientResponse(
            client.getId(),
            client.getUser().getUsername(),
            wallets
        );
    }

    @Transactional(readOnly = true)
    public ClientResponse getByUsername(String username) {
        log.info("Getting client by username");

        User client = userService.findByUserName(username);

        List<WalletResponse> wallets = walletService.getClientWalletsById(client.getId());

        return new ClientResponse(
                client.getId(),
                client.getUsername(),
                wallets
        );
    }

    @Transactional
    public ClientResponse create(ClientCreationRequest request){
        log.info("Creating client account");

        if(userService.existsByUsername(request.username())){
            log.warn("Client account creation rejected: username already exists");
            throw new UsernameAlreadyExistsException(request.username());
        }
        log.debug("Client username is available");

        byte[] publicKey = request.publicKey().getBytes(StandardCharsets.UTF_8);
        if (clientRepository.existsByPublicKey(publicKey)) {
            log.warn("Client account creation rejected: public key is already registered");
            throw new PublicKeyAlreadyRegisteredException("This public key cannot be accepted");
        }
        log.debug("Client public key is available");

        User user = new User();
        user.setRole(UserRole.CLIENT);
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        Client client = new Client();
        client.setUser(user);
        client.setFirstName(request.firstName());
        client.setLastName(request.lastName());
        client.setAddress(request.address());
        client.setPhoneNumber(request.phoneNumber());
        client.setCountry(request.countryCode());
        client.setPublicKey(publicKey);

        Client savedClient = clientRepository.save(client);
        log.info("Client account created: {}", savedClient.getId());

        WalletResponse initialWallet = walletService.createWallet("Main", savedClient.getUser(), WalletType.MAIN, request.currency());
        log.info("Client main wallet created: clientId={}, walletId={}", savedClient.getId(), initialWallet.id());

        return new ClientResponse(
                savedClient.getId(),
                savedClient.getUser().getUsername(),
                List.of(initialWallet)
        );
    }
}
