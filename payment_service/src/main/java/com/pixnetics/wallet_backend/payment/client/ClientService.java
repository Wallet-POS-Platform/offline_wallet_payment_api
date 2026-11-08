package com.pixnetics.wallet_backend.payment.client;

import com.pixnetics.wallet_backend.enums.UserType;
import com.pixnetics.wallet_backend.payment.client.dto.ClientCreationRequest;
import com.pixnetics.wallet_backend.payment.client.dto.ClientResponse;
import com.pixnetics.wallet_backend.payment.client.exception.ClientNotFoundException;
import com.pixnetics.wallet_backend.payment.user.User;
import com.pixnetics.wallet_backend.payment.user.UserRepository;
import com.pixnetics.wallet_backend.payment.user.exception.UsernameAlreadyExistsException;
import com.pixnetics.wallet_backend.payment.wallet.WalletService;
import com.pixnetics.wallet_backend.payment.wallet.dto.WalletCreationRequest;
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
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;

    @Transactional(readOnly = true)
    public ClientResponse getById(UUID id) {
        log.info("Getting a new client");

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));

        List<WalletResponse> wallets = client.getUser().getWallets().stream()
                .map(walletService::toResponse)
                .toList();

        return new ClientResponse(
            client.getId(),
            client.getUser().getUsername(),
            wallets
        );
    }

    @Transactional(readOnly = true)
    public ClientResponse getByUsername(String username) {
        log.info("Getting a new client");

        User client = userRepository.findByUsername(username)
                .orElseThrow(() -> new ClientNotFoundException(username));

        List<WalletResponse> wallets = client.getWallets().stream()
                .map(walletService::toResponse)
                .toList();

        return new ClientResponse(
                client.getId(),
                client.getUsername(),
                wallets
        );
    }

    @Transactional
    public ClientResponse create(ClientCreationRequest request){
        if(userRepository.existsByUsername(request.username())){
            throw new UsernameAlreadyExistsException(request.username());
        }
        log.info("Username is not exists check passed");

        User user = new User();
        user.setType(UserType.CLIENT);
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        Client client = new Client();
        client.setUser(user);
        client.setFirstName(request.firstName());
        client.setLastName(request.lastName());
        client.setAddress(request.address());
        client.setPhoneNumber(request.phoneNumber());
        client.setCountry(request.countryCode());
        client.setPublicKey(request.publicKey().getBytes(StandardCharsets.UTF_8));

        Client savedClient = clientRepository.save(client);
        log.info("Client record created");

        WalletCreationRequest initialWalletRequest = new WalletCreationRequest("MAIN", "Main", "EUR");

        WalletResponse initialWallet = walletService.createWallet(savedClient.getUser(), initialWalletRequest);
        log.info("Client main wallet created");

        return new ClientResponse(
                savedClient.getId(),
                savedClient.getUser().getUsername(),
                List.of(initialWallet)
        );
    }
}
