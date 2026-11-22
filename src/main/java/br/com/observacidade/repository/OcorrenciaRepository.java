package br.com.observacidade.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.observacidade.entity.Ocorrencia;
import br.com.observacidade.enums.Categoria;
import br.com.observacidade.enums.StatusOcorrencia;
import br.com.observacidade.enums.Urgencia;

public interface OcorrenciaRepository extends JpaRepository<Ocorrencia, Long>, JpaSpecificationExecutor<Ocorrencia> {

    List<Ocorrencia> findByUsuarioEmailIgnoreCaseOrderByDataCriacaoDesc(String email);

    List<Ocorrencia> findByUsuarioId(Long usuarioId);

    long countByStatus(StatusOcorrencia status);

    long countByCategoria(Categoria categoria);

    long countByUrgencia(Urgencia urgencia);

    long countByUsuarioEmailIgnoreCase(String email);

    long countByUsuarioEmailIgnoreCaseAndStatus(String email, StatusOcorrencia status);

    long countByUsuarioEmailIgnoreCaseAndCategoria(String email, Categoria categoria);

    long countByUsuarioEmailIgnoreCaseAndUrgencia(String email, Urgencia urgencia);
}
