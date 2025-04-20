package com.example.trading.configuration;

import com.example.trading.entity.User;
import com.example.trading.entity.Wallet;
import com.example.trading.repository.UserRepository;
import com.example.trading.repository.WalletRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;

    private final WalletRepository walletRepository;

    public DataInitializer(UserRepository userRepository, WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        String defaultUsername = "tunguyen";

        Optional<User> userOptional = userRepository.findUserByUsername(defaultUsername);

        User user;
        if (userOptional.isEmpty()) {
            user = new User();
            user.setUsername(defaultUsername);
            user.setFullName("Nguyen Thanh Tu");
            user.setEmail("tunguyen@example.com");
            user.setPhone("0123456789");
            user.setAddress("HCM");

            user = userRepository.save(user);
            System.out.println("Create user success: " + user.getUsername());
        } else {
            user = userOptional.get();
            System.out.println("Exist user: " + user.getUsername());
        }

        Optional<Wallet> walletOptional = walletRepository.findByUserId(user.getId());

        if (walletOptional.isEmpty()) {
            Wallet wallet = new Wallet();
            wallet.setUserId(user.getId());
            wallet.setUsdtBalance(BigDecimal.valueOf(50000.0));
            wallet.setBtcBalance(BigDecimal.ZERO);
            wallet.setEthBalance(BigDecimal.ZERO);

            walletRepository.save(wallet);
            System.out.println("Create wallet success : " + user.getUsername());
        } else {
            System.out.println("Exit wallet: " + user.getUsername());
        }
    }
}
