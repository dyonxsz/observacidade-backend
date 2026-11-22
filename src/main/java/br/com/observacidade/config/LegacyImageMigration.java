package br.com.observacidade.config;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Migra as fotos do antigo campo "ocorrencias.imagem_base64" para a tabela
 * "ocorrencia_imagens". Seguro para bancos novos: se a coluna não existir,
 * nada é feito.
 */
@Component
@Order(2)
public class LegacyImageMigration implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LegacyImageMigration.class);

    private final JdbcTemplate jdbcTemplate;

    public LegacyImageMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            List<Map<String, Object>> antigas = jdbcTemplate.queryForList(
                    "SELECT id, imagem_base64 FROM ocorrencias "
                            + "WHERE imagem_base64 IS NOT NULL AND imagem_base64 <> '' "
                            + "AND id NOT IN (SELECT ocorrencia_id FROM ocorrencia_imagens)");

            if (antigas.isEmpty()) {
                return;
            }

            int migradas = 0;

            for (Map<String, Object> linha : antigas) {
                Long ocorrenciaId = ((Number) linha.get("id")).longValue();
                String bruto = String.valueOf(linha.get("imagem_base64"));
                String contentType = "image/jpeg";
                String conteudo = bruto.trim();

                if (conteudo.startsWith("data:")) {
                    int virgula = conteudo.indexOf(',');
                    if (virgula <= 0) {
                        continue;
                    }

                    String meta = conteudo.substring(5, virgula);
                    int pontoVirgula = meta.indexOf(';');
                    contentType = pontoVirgula > 0 ? meta.substring(0, pontoVirgula) : meta;
                    conteudo = conteudo.substring(virgula + 1);
                }

                int tamanho;
                try {
                    tamanho = Base64.getDecoder().decode(conteudo.replaceAll("\\s", "")).length;
                } catch (IllegalArgumentException ex) {
                    log.warn("Imagem da ocorrência {} ignorada: base64 inválido", ocorrenciaId);
                    continue;
                }

                jdbcTemplate.update(
                        "INSERT INTO ocorrencia_imagens "
                                + "(ocorrencia_id, conteudo_base64, content_type, tamanho_bytes, data_criacao) "
                                + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)",
                        ocorrenciaId, conteudo, contentType.toLowerCase(), tamanho);

                migradas++;
            }

            if (migradas > 0) {
                jdbcTemplate.update("UPDATE ocorrencias SET imagem_base64 = NULL WHERE imagem_base64 IS NOT NULL");
                log.info("{} imagem(ns) migrada(s) para a tabela ocorrencia_imagens", migradas);
            }
        } catch (Exception ex) {
            // Banco novo não possui a coluna legada: nada a migrar.
            log.debug("Migração de imagens legadas não necessária: {}", ex.getMessage());
        }
    }
}
