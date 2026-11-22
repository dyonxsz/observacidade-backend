package br.com.observacidade.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.observacidade.entity.OcorrenciaImagem;

public interface OcorrenciaImagemRepository extends JpaRepository<OcorrenciaImagem, Long> {

    Optional<OcorrenciaImagem> findByOcorrenciaId(Long ocorrenciaId);

    List<OcorrenciaImagem> findByOcorrenciaIdIn(Collection<Long> ocorrenciaIds);

    boolean existsByOcorrenciaId(Long ocorrenciaId);

    void deleteByOcorrenciaId(Long ocorrenciaId);
}
