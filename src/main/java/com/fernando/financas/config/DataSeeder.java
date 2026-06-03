package com.fernando.financas.config;

import com.fernando.financas.entity.Usuario;
import com.fernando.financas.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (repository.count() == 0) {
            Usuario admin = Usuario.builder()
                    .nome("Admin")
                    .email("admin@financas.local")
                    .senha(encoder.encode("admin123"))
                    .build();
            repository.save(admin);
            log.info("[seed] usuário padrão criado: admin@financas.local / admin123");
        }
    }
}
