package br.com.observacidade.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.observacidade.dto.PageResponse;
import br.com.observacidade.dto.UsuarioResponseDTO;
import br.com.observacidade.entity.Ocorrencia;
import br.com.observacidade.entity.Usuario;
import br.com.observacidade.exception.RecursoNaoEncontradoException;
import br.com.observacidade.exception.RegraNegocioException;
import br.com.observacidade.repository.ComentarioRepository;
import br.com.observacidade.repository.HistoricoOcorrenciaRepository;
import br.com.observacidade.repository.NotificacaoRepository;
import br.com.observacidade.repository.OcorrenciaImagemRepository;
import br.com.observacidade.repository.OcorrenciaRepository;
import br.com.observacidade.repository.SuporteRepository;
import br.com.observacidade.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final OcorrenciaRepository ocorrenciaRepository;
    private final ComentarioRepository comentarioRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final OcorrenciaImagemRepository imagemRepository;
    private final HistoricoOcorrenciaRepository historicoRepository;
    private final SuporteRepository suporteRepository;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            OcorrenciaRepository ocorrenciaRepository,
            ComentarioRepository comentarioRepository,
            NotificacaoRepository notificacaoRepository,
            OcorrenciaImagemRepository imagemRepository,
            HistoricoOcorrenciaRepository historicoRepository,
            SuporteRepository suporteRepository) {
        this.usuarioRepository = usuarioRepository;
        this.ocorrenciaRepository = ocorrenciaRepository;
        this.comentarioRepository = comentarioRepository;
        this.notificacaoRepository = notificacaoRepository;
        this.imagemRepository = imagemRepository;
        this.historicoRepository = historicoRepository;
        this.suporteRepository = suporteRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<UsuarioResponseDTO> listar(Pageable pageable) {
        Page<Usuario> pagina = usuarioRepository.findAll(pageable);
        return PageResponse.from(pagina, UsuarioResponseDTO::new);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscar(Long id) {
        return new UsuarioResponseDTO(buscarEntidade(id));
    }

    @Transactional
    public UsuarioResponseDTO tornarAdmin(Long id) {
        Usuario usuario = buscarEntidade(id);
        usuario.setRole("ADMIN");
        return new UsuarioResponseDTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponseDTO tornarUsuario(Long id, String emailLogado) {
        Usuario usuario = buscarEntidade(id);

        if (usuario.getEmail().equalsIgnoreCase(emailLogado)) {
            throw new RegraNegocioException("Você não pode remover o seu próprio acesso de administrador");
        }

        usuario.setRole("USER");
        return new UsuarioResponseDTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public void deletar(Long id, String emailLogado) {
        Usuario usuario = buscarEntidade(id);

        if (usuario.getEmail().equalsIgnoreCase(emailLogado)) {
            throw new RegraNegocioException("Você não pode excluir a sua própria conta por aqui");
        }

        List<Ocorrencia> ocorrencias = ocorrenciaRepository.findByUsuarioId(id);

        for (Ocorrencia ocorrencia : ocorrencias) {
            notificacaoRepository.deleteByOcorrenciaId(ocorrencia.getId());
            comentarioRepository.deleteByOcorrenciaId(ocorrencia.getId());
            historicoRepository.deleteByOcorrenciaId(ocorrencia.getId());
            imagemRepository.deleteByOcorrenciaId(ocorrencia.getId());
            ocorrenciaRepository.delete(ocorrencia);
        }

        notificacaoRepository.deleteByUsuarioId(id);
        comentarioRepository.deleteByUsuarioId(id);
        suporteRepository.deleteByUsuarioId(id);

        usuarioRepository.delete(usuario);
    }

    private Usuario buscarEntidade(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }
}
