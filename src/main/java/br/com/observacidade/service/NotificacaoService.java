package br.com.observacidade.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.observacidade.dto.NotificacaoResponseDTO;
import br.com.observacidade.entity.Notificacao;
import br.com.observacidade.entity.Ocorrencia;
import br.com.observacidade.entity.Usuario;
import br.com.observacidade.exception.RecursoNaoEncontradoException;
import br.com.observacidade.repository.NotificacaoRepository;

@Service
public class NotificacaoService {

    private final NotificacaoRepository repository;

    public NotificacaoService(NotificacaoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void criar(Usuario usuario, Ocorrencia ocorrencia, String tipo, String titulo, String mensagem) {
        if (usuario == null) {
            return;
        }

        Notificacao notificacao = new Notificacao();
        notificacao.setUsuario(usuario);
        notificacao.setOcorrencia(ocorrencia);
        notificacao.setTipo(tipo);
        notificacao.setTitulo(titulo);
        notificacao.setMensagem(mensagem);
        notificacao.setLida(false);

        repository.save(notificacao);
    }

    @Transactional(readOnly = true)
    public List<NotificacaoResponseDTO> listarTodas(String email) {
        return repository.findByUsuarioEmailOrderByDataCriacaoDesc(email)
                .stream()
                .map(NotificacaoResponseDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificacaoResponseDTO> listarNaoLidas(String email) {
        return repository.findByUsuarioEmailAndLidaFalseOrderByDataCriacaoDesc(email)
                .stream()
                .map(NotificacaoResponseDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public long contarNaoLidas(String email) {
        return repository.countByUsuarioEmailIgnoreCaseAndLidaFalse(email);
    }

    @Transactional
    public NotificacaoResponseDTO marcarComoLida(Long id, String email) {
        Notificacao notificacao = repository.findByIdAndUsuarioEmailIgnoreCase(id, email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Notificação não encontrada"));

        notificacao.setLida(true);
        return new NotificacaoResponseDTO(repository.save(notificacao));
    }

    @Transactional
    public int marcarTodasComoLidas(String email) {
        return repository.marcarTodasComoLidas(email);
    }

    @Transactional
    public void deletar(Long id, String email) {
        Notificacao notificacao = repository.findByIdAndUsuarioEmailIgnoreCase(id, email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Notificação não encontrada"));

        repository.delete(notificacao);
    }
}
