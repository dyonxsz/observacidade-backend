package br.com.observacidade.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import br.com.observacidade.entity.Suporte;

public interface SuporteRepository extends JpaRepository<Suporte, Long> {
    List<Suporte> findByUsuarioEmailOrderByDataCriacaoDesc(String email);
}
