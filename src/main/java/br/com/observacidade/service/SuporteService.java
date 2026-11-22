package br.com.observacidade.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.observacidade.dto.PageResponse;
import br.com.observacidade.dto.SuporteRequestDTO;
import br.com.observacidade.dto.SuporteResponseDTO;
import br.com.observacidade.dto.SuporteUpdateDTO;
import br.com.observacidade.entity.Suporte;
import br.com.observacidade.entity.Usuario;
import br.com.observacidade.exception.RecursoNaoEncontradoException;
import br.com.observacidade.exception.RegraNegocioException;
import br.com.observacidade.repository.SuporteRepository;
import br.com.observacidade.repository.UsuarioRepository;

@Service
public class SuporteService {

    private static final Set<String> STATUS_VALIDOS =
            Set.of("ABERTO", "EM_ANDAMENTO", "RESPONDIDO", "FECHADO");

    private final SuporteRepository suporteRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoService notificacaoService;

    public SuporteService(
            SuporteRepository suporteRepository,
            UsuarioRepository usuarioRepository,
            NotificacaoService notificacaoService) {
        this.suporteRepository = suporteRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacaoService = notificacaoService;
    }

    @Transactional
    public SuporteResponseDTO criar(SuporteRequestDTO dto, String email) {
        Usuario usuario = buscarUsuario(email);

        Suporte suporte = new Suporte();
        suporte.setAssunto(dto.getAssunto().trim());
        suporte.setMensagem(dto.getMensagem().trim());
        suporte.setUsuario(usuario);
        suporte.setStatus("ABERTO");

        suporte = suporteRepository.save(suporte);

        notificacaoService.criar(usuario, null, "SUPORTE",
                "Mensagem enviada ao suporte",
                "Recebemos sua solicitação sobre: " + dto.getAssunto().trim());

        return new SuporteResponseDTO(suporte);
    }

    @Transactional(readOnly = true)
    public List<SuporteResponseDTO> listarDoUsuario(String email) {
        return suporteRepository.findByUsuarioEmailIgnoreCaseOrderByDataCriacaoDesc(email)
                .stream()
                .map(SuporteResponseDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<SuporteResponseDTO> listar(String status, Pageable pageable) {
        Page<Suporte> pagina = (status == null || status.isBlank())
                ? suporteRepository.findAll(pageable)
                : suporteRepository.findByStatusIgnoreCase(status.trim(), pageable);

        return PageResponse.from(pagina, SuporteResponseDTO::new);
    }

    @Transactional
    public SuporteResponseDTO responder(Long id, SuporteUpdateDTO dto, String emailAdmin) {
        Suporte suporte = buscar(id);
        Usuario admin = buscarUsuario(emailAdmin);

        String status = dto.getStatus() == null || dto.getStatus().isBlank()
                ? "RESPONDIDO"
                : dto.getStatus().trim().toUpperCase();

        if (!STATUS_VALIDOS.contains(status)) {
            throw new RegraNegocioException(
                    "Status inválido. Use ABERTO, EM_ANDAMENTO, RESPONDIDO ou FECHADO.");
        }

        suporte.setResposta(dto.getResposta().trim());
        suporte.setStatus(status);
        suporte.setDataResposta(LocalDateTime.now());
        suporte.setRespondidoPor(admin.getNome());

        suporte = suporteRepository.save(suporte);

        notificacaoService.criar(suporte.getUsuario(), null, "SUPORTE",
                "Resposta do suporte",
                "Seu chamado \"" + suporte.getAssunto() + "\" foi respondido: " + dto.getResposta().trim());

        return new SuporteResponseDTO(suporte);
    }

    @Transactional
    public void deletar(Long id) {
        suporteRepository.delete(buscar(id));
    }

    private Suporte buscar(Long id) {
        return suporteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chamado de suporte não encontrado"));
    }

    private Usuario buscarUsuario(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }
}
