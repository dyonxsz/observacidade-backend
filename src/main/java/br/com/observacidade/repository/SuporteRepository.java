package br.com.observacidade.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.observacidade.entity.Suporte;

public interface SuporteRepository extends JpaRepository<Suporte, Long> {

    List<Suporte> findByUsuarioEmailIgnoreCaseOrderByDataCriacaoDesc(String email);

    Page<Suporte> findByStatusIgnoreCase(String status, Pageable pageable);

    Page<Suporte> findByUsuarioEmailIgnoreCase(String email, Pageable pageable);

    void deleteByUsuarioId(Long usuarioId);
}
