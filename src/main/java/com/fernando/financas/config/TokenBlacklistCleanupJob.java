package com.fernando.financas.config;

import com.fernando.financas.repository.RefreshTokenRevogadoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistCleanupJob {

    private final RefreshTokenRevogadoRepository repository;

    // executa toda hora — barato (DELETE indexado por expires_at)
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void limparExpirados() {
        int removidos = repository.deleteExpiradosAntesDe(LocalDateTime.now());
        if (removidos > 0) log.info("[blacklist] {} refresh tokens expirados removidos", removidos);
    }
}
