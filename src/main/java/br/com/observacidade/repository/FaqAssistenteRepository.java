package br.com.observacidade.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.observacidade.entity.FaqAssistente;

public interface FaqAssistenteRepository extends JpaRepository<FaqAssistente, Long> {

    List<FaqAssistente> findByAtivoTrueOrderByIdAsc();

    Optional<FaqAssistente> findFirstByPerguntaIgnoreCase(String pergunta);
}
