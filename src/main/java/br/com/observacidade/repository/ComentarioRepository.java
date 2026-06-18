package br.com.observacidade.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.observacidade.entity.Comentario;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    List<Comentario> findByOcorrenciaIdOrderByDataCriacaoDesc(Long ocorrenciaId);
    void deleteByOcorrenciaId(Long ocorrenciaId);
}
