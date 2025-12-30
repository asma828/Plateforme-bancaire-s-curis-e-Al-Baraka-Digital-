package com.example.Al.Baraka.config;

import com.example.Al.Baraka.model.User;
import com.example.Al.Baraka.enums.Role;
import com.example.Al.Baraka.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initialise les utilisateurs par défaut au démarrage de l'application
 * - 1 Administrateur
 * - 1 Agent Bancaire
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info(" Initialisation des données par défaut...");

        // Créer l'administrateur si inexistant
        if (!userRepository.existsByEmail("admin@albaraka.com")) {
            User admin = User.builder()
                    .email("admin@albaraka.com")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Administrateur Principal")
                    .role(Role.ADMIN)
                    .active(true)
                    .build();
            userRepository.save(admin);
            log.info(" Administrateur créé : admin@albaraka.com / admin123");
        } else {
            log.info("  Administrateur déjà existant");
        }

        // Créer l'agent bancaire si inexistant
        if (!userRepository.existsByEmail("agent@albaraka.com")) {
            User agent = User.builder()
                    .email("agent@albaraka.com")
                    .password(passwordEncoder.encode("agent123"))
                    .fullName("Agent Bancaire Principal")
                    .role(Role.AGENT_BANCAIRE)
                    .active(true)
                    .build();
            userRepository.save(agent);
            log.info(" Agent bancaire créé : agent@albaraka.com / agent123");
        } else {
            log.info("  Agent bancaire déjà existant");
        }

        log.info(" Initialisation des données terminée");
        log.info("");
        log.info(" Comptes disponibles :");
        log.info("    Admin : admin@albaraka.com / admin123");
        log.info("    Agent : agent@albaraka.com / agent123");
        log.info("    Client : Créer via /auth/register");
        log.info("");
    }
}