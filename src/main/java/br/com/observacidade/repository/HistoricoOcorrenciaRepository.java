package br.com.observacidade.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.observacidade.entity.HistoricoOcorrencia;

public interface HistoricoOcorrenciaRepository extends JpaRepository<HistoricoOcorrencia, Long> {

    List<HistoricoOcorrencia> findByOcorrenciaIdOrderByDataCriacaoAsc(Long ocorrenciaId);

    void deleteByOcorrenciaId(Long ocorrenciaId);
}
