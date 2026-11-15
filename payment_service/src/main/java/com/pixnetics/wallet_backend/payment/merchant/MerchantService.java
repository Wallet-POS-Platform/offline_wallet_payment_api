package com.pixnetics.wallet_backend.payment.merchant;

import com.pixnetics.wallet_backend.enums.UserRole;
import com.pixnetics.wallet_backend.enums.WalletType;
import com.pixnetics.wallet_backend.payment.merchant.dto.MerchantCreationRequest;
import com.pixnetics.wallet_backend.payment.merchant.dto.MerchantResponse;
import com.pixnetics.wallet_backend.payment.merchant.exception.MerchantNotFoundException;
import com.pixnetics.wallet_backend.payment.user.User;
import com.pixnetics.wallet_backend.payment.user.UserRepository;
import com.pixnetics.wallet_backend.payment.user.exception.UsernameAlreadyExistsException;
import com.pixnetics.wallet_backend.payment.wallet.WalletService;
import com.pixnetics.wallet_backend.payment.wallet.dto.WalletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;

    @Transactional(readOnly = true)
    public MerchantResponse getById(UUID id) {
        log.info("Getting merchant by ID: {}", id);

        Merchant merchant = merchantRepository.findById(id)
                .orElseThrow(() -> new MerchantNotFoundException(id));

        return toResponse(merchant, walletService.getMerchantWalletById(merchant.getId()));
    }

    @Transactional
    public MerchantResponse create(MerchantCreationRequest request){
        log.info("Creating merchant account");

        if(userRepository.existsByUsername(request.username())){
            log.warn("Merchant account creation rejected: username already exists");
            throw new UsernameAlreadyExistsException(request.username());
        }
        log.debug("Merchant username is available");

        User user = new User();
        user.setRole(UserRole.MERCHANT);
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        Merchant merchant = new Merchant();
        merchant.setUser(user);
        merchant.setName(request.businessName());
        merchant.setCategory(request.category());
        merchant.setAddress(request.address());
        merchant.setCountry(request.countryCode());
        merchant.setPayoutIBAN(request.payoutIBAN());
        merchant.setSettleType(request.settleType());

        Merchant savedMerchant = merchantRepository.save(merchant);
        log.info("Merchant account created: {}", savedMerchant.getId());

        WalletResponse initialWallet = walletService.createWallet("Main", savedMerchant.getUser(), WalletType.MAIN, request.currency());
        log.info("Merchant main wallet created: merchantId={}, walletId={}", savedMerchant.getId(), initialWallet.id());

        return toResponse(savedMerchant, initialWallet);
    }

    private MerchantResponse toResponse(Merchant merchant, WalletResponse mainWallet) {
        return new MerchantResponse(
                merchant.getId(),
                merchant.getUser().getUsername(),
                merchant.getName(),
                merchant.getCategory(),
                merchant.getAddress(),
                merchant.getCountry(),
                merchant.getSettleType(),
                mainWallet
        );
    }
}
