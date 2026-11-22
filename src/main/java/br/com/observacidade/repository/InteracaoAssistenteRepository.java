package br.com.observacidade.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.observacidade.entity.InteracaoAssistente;
import br.com.observacidade.entity.Usuario;

public interface InteracaoAssistenteRepository extends JpaRepository<InteracaoAssistente, Long> {

    List<InteracaoAssistente> findTop20ByUsuarioOrderByDataHoraDesc(Usuario usuario);

    long countByOrigem(String origem);

    List<InteracaoAssistente> findTop5ByOrigemOrderByDataHoraDesc(String origem);
}
