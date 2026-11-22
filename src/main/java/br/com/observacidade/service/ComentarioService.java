package br.com.observacidade.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.observacidade.dto.ComentarioRequestDTO;
import br.com.observacidade.dto.ComentarioResponseDTO;
import br.com.observacidade.entity.Comentario;
import br.com.observacidade.entity.Ocorrencia;
import br.com.observacidade.entity.Usuario;
import br.com.observacidade.exception.AcessoNegadoException;
import br.com.observacidade.exception.RecursoNaoEncontradoException;
import br.com.observacidade.repository.ComentarioRepository;
import br.com.observacidade.repository.OcorrenciaRepository;
import br.com.observacidade.repository.UsuarioRepository;
import br.com.observacidade.security.UsuarioAutenticado;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final OcorrenciaRepository ocorrenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoService notificacaoService;
    private final UsuarioAutenticado usuarioAutenticado;

    public ComentarioService(
            ComentarioRepository comentarioRepository,
            OcorrenciaRepository ocorrenciaRepository,
            UsuarioRepository usuarioRepository,
            NotificacaoService notificacaoService,
            UsuarioAutenticado usuarioAutenticado) {
        this.comentarioRepository = comentarioRepository;
        this.ocorrenciaRepository = ocorrenciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacaoService = notificacaoService;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @Transactional(readOnly = true)
    public List<ComentarioResponseDTO> listar(Long ocorrenciaId) {
        buscarOcorrencia(ocorrenciaId);

        return comentarioRepository.findByOcorrenciaIdOrderByDataCriacaoDesc(ocorrenciaId)
                .stream()
                .map(ComentarioResponseDTO::new)
                .toList();
    }

    @Transactional
    public ComentarioResponseDTO criar(Long ocorrenciaId, ComentarioRequestDTO dto, String email) {
        Ocorrencia ocorrencia = buscarOcorrencia(ocorrenciaId);
        Usuario usuario = buscarUsuario(email);

        Comentario comentario = new Comentario();
        comentario.setTexto(dto.getTexto().trim());
        comentario.setOcorrencia(ocorrencia);
        comentario.setUsuario(usuario);

        comentario = comentarioRepository.save(comentario);

        if (ocorrencia.getUsuario() != null
                && !ocorrencia.getUsuario().getId().equals(usuario.getId())) {

            notificacaoService.criar(ocorrencia.getUsuario(), ocorrencia, "COMENTARIO",
                    "Novo comentário na ocorrência",
                    usuario.getNome() + " comentou: " + dto.getTexto().trim());
        }

        return new ComentarioResponseDTO(comentario);
    }

    @Transactional
    public void deletar(Long ocorrenciaId, Long comentarioId, String email) {
        Comentario comentario = comentarioRepository.findByIdAndOcorrenciaId(comentarioId, ocorrenciaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Comentário não encontrado"));

        boolean autor = comentario.getUsuario() != null
                && comentario.getUsuario().getEmail().equalsIgnoreCase(email);

        if (!autor && !usuarioAutenticado.isAdmin()) {
            throw new AcessoNegadoException("Você só pode excluir os seus próprios comentários");
        }

        comentarioRepository.delete(comentario);
    }

    private Ocorrencia buscarOcorrencia(Long ocorrenciaId) {
        return ocorrenciaRepository.findById(ocorrenciaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ocorrência não encontrada"));
    }

    private Usuario buscarUsuario(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }
}
