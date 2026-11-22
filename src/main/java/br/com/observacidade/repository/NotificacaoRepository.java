package br.com.observacidade.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.observacidade.entity.Notificacao;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findByUsuarioEmailOrderByDataCriacaoDesc(String email);

    List<Notificacao> findByUsuarioEmailAndLidaFalseOrderByDataCriacaoDesc(String email);

    long countByUsuarioEmailIgnoreCaseAndLidaFalse(String email);

    Optional<Notificacao> findByIdAndUsuarioEmailIgnoreCase(Long id, String email);

    @Modifying
    @Query("update Notificacao n set n.lida = true where lower(n.usuario.email) = lower(:email) and n.lida = false")
    int marcarTodasComoLidas(@Param("email") String email);

    void deleteByOcorrenciaId(Long ocorrenciaId);

    void deleteByUsuarioId(Long usuarioId);
}
