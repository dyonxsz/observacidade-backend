package br.com.observacidade.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.observacidade.entity.Notificacao;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    List<Notificacao> findByUsuarioEmailOrderByDataCriacaoDesc(String email);
    List<Notificacao> findByUsuarioEmailAndLidaFalseOrderByDataCriacaoDesc(String email);
    void deleteByOcorrenciaId(Long ocorrenciaId);
}
