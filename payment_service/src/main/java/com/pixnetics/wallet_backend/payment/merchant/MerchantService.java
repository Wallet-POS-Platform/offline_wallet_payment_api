package com.pixnetics.wallet_backend.payment.merchant;

import com.pixnetics.wallet_backend.enums.UserType;
import com.pixnetics.wallet_backend.payment.client.Client;
import com.pixnetics.wallet_backend.payment.client.ClientRepository;
import com.pixnetics.wallet_backend.payment.merchant.dto.MerchantCreationRequest;
import com.pixnetics.wallet_backend.payment.merchant.dto.MerchantResponse;
import com.pixnetics.wallet_backend.payment.merchant.exception.MerchantNotFoundException;
import com.pixnetics.wallet_backend.payment.user.User;
import com.pixnetics.wallet_backend.payment.user.UserRepository;
import com.pixnetics.wallet_backend.payment.user.exception.UsernameAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public MerchantResponse getById(UUID id) {
        log.info("Creating a new client");
        Merchant merchant = merchantRepository.findById(id)
                .orElseThrow(() -> new MerchantNotFoundException(id));

        return new MerchantResponse(
                merchant.getId(),
                merchant.getUser().getUsername()
        );
    }

    @Transactional
    public MerchantResponse create(MerchantCreationRequest request){
        log.info("Creating a new merchant");

        if(userRepository.existsByUsername(request.username())){
            throw new UsernameAlreadyExistsException(request.username());
        }

        User user = new User();
        user.setType(UserType.MERCHANT);
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        Merchant merchant = new Merchant();
        merchant.setUser(user);
        merchant.setName(request.businessName());
        merchant.setCategory(request.category());
        merchant.setAddress(request.address());
        merchant.setPayoutIBAN(request.payoutIBAN());
        merchant.setSettleType(request.settleType());

        Merchant savedClient = merchantRepository.save(merchant);

        return new MerchantResponse(
                savedClient.getId(),
                savedClient.getUser().getUsername()
        );
    }
}
