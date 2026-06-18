package br.com.observacidade.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.observacidade.entity.Ocorrencia;

public interface OcorrenciaRepository extends JpaRepository<Ocorrencia, Long> {
}
