package br.com.observacidade.service;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import br.com.observacidade.dto.*;
import br.com.observacidade.entity.*;
import br.com.observacidade.enums.*;
import br.com.observacidade.exception.AcessoNegadoException;
import br.com.observacidade.exception.RecursoNaoEncontradoException;
import br.com.observacidade.exception.RegraNegocioException;
import br.com.observacidade.repository.*;
import br.com.observacidade.security.UsuarioAutenticado;

@Service
public class OcorrenciaService {

    private final OcorrenciaRepository ocorrenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ComentarioRepository comentarioRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final OcorrenciaImagemRepository imagemRepository;
    private final HistoricoOcorrenciaRepository historicoRepository;
    private final NotificacaoService notificacaoService;
    private final UsuarioAutenticado usuarioAutenticado;

    @Value("${app.upload.max-bytes:5242880}")
    private long maxBytes;

    public OcorrenciaService(
            OcorrenciaRepository ocorrenciaRepository,
            UsuarioRepository usuarioRepository,
            ComentarioRepository comentarioRepository,
            NotificacaoRepository notificacaoRepository,
            OcorrenciaImagemRepository imagemRepository,
            HistoricoOcorrenciaRepository historicoRepository,
            NotificacaoService notificacaoService,
            UsuarioAutenticado usuarioAutenticado) {
        this.ocorrenciaRepository = ocorrenciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.comentarioRepository = comentarioRepository;
        this.notificacaoRepository = notificacaoRepository;
        this.imagemRepository = imagemRepository;
        this.historicoRepository = historicoRepository;
        this.notificacaoService = notificacaoService;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @Transactional
    public OcorrenciaResponseDTO criar(OcorrenciaRequestDTO dto, String email) {
        Usuario usuario = buscarUsuario(email);

        Ocorrencia ocorrencia = new Ocorrencia();
        aplicarDados(ocorrencia, dto);
        ocorrencia.setUsuario(usuario);
        ocorrencia.setStatus(StatusOcorrencia.RECEBIDO);

        ocorrencia = ocorrenciaRepository.save(ocorrencia);

        if (dto.getImagemBase64() != null && !dto.getImagemBase64().isBlank()) {
            salvarImagem(ocorrencia.getId(), dto.getImagemBase64());
        }

        registrarHistorico(ocorrencia, null, StatusOcorrencia.RECEBIDO,
                "Ocorrência registrada pelo cidadão.", usuario);

        notificacaoService.criar(usuario, ocorrencia, "OCORRENCIA",
                "Ocorrência registrada",
                "Recebemos sua ocorrência " + ocorrencia.getProtocolo()
                        + ". Você pode acompanhar o andamento pelo aplicativo.");

        return detalhar(ocorrencia.getId(), false);
    }

    @Transactional(readOnly = true)
    public PageResponse<OcorrenciaResumoDTO> listar(
            StatusOcorrencia status,
            Categoria categoria,
            Urgencia urgencia,
            String bairro,
            String busca,
            Pageable pageable) {

        Page<Ocorrencia> pagina = ocorrenciaRepository.findAll(
                OcorrenciaSpecifications.filtro(status, categoria, urgencia, bairro, busca),
                pageable);

        return resumir(pagina);
    }

    @Transactional(readOnly = true)
    public PageResponse<OcorrenciaResumoDTO> listarDoUsuario(String email, Pageable pageable) {
        Page<Ocorrencia> pagina = ocorrenciaRepository.findAll(
                OcorrenciaSpecifications.doUsuario(email),
                pageable);

        return resumir(pagina);
    }

    @Transactional(readOnly = true)
    public OcorrenciaResponseDTO buscarDetalhe(Long id, boolean incluirImagem) {
        return detalhar(id, incluirImagem);
    }

    @Transactional
    public OcorrenciaResponseDTO atualizar(Long id, OcorrenciaRequestDTO dto, String email) {
        Ocorrencia ocorrencia = buscarEntidade(id);
        verificarPermissao(ocorrencia, email);

        aplicarDados(ocorrencia, dto);
        ocorrencia = ocorrenciaRepository.save(ocorrencia);

        if (dto.getImagemBase64() != null && !dto.getImagemBase64().isBlank()) {
            salvarImagem(ocorrencia.getId(), dto.getImagemBase64());
        }

        return detalhar(ocorrencia.getId(), false);
    }

    @Transactional
    public OcorrenciaResponseDTO atualizarStatus(Long id, StatusUpdateDTO dto, String emailAdmin) {
        Ocorrencia ocorrencia = buscarEntidade(id);
        StatusOcorrencia anterior = ocorrencia.getStatus();

        if (anterior == dto.getStatus()) {
            throw new RegraNegocioException("A ocorrência já está com o status " + dto.getStatus().getLabel());
        }

        Usuario admin = buscarUsuario(emailAdmin);

        ocorrencia.setStatus(dto.getStatus());
        ocorrencia = ocorrenciaRepository.save(ocorrencia);

        registrarHistorico(ocorrencia, anterior, dto.getStatus(), dto.getObservacao(), admin);

        if (ocorrencia.getUsuario() != null) {
            String mensagem = "Sua ocorrência " + ocorrencia.getProtocolo()
                    + " agora está como: " + dto.getStatus().getLabel() + ".";

            if (dto.getObservacao() != null && !dto.getObservacao().isBlank()) {
                mensagem += " Observação: " + dto.getObservacao().trim();
            }

            notificacaoService.criar(ocorrencia.getUsuario(), ocorrencia, dto.getStatus().name(),
                    "Status da ocorrência atualizado", mensagem);
        }

        return detalhar(ocorrencia.getId(), false);
    }

    @Transactional
    public ImagemResponseDTO atualizarImagem(Long id, MultipartFile arquivo, String email) {
        Ocorrencia ocorrencia = buscarEntidade(id);
        verificarPermissao(ocorrencia, email);

        if (arquivo == null || arquivo.isEmpty()) {
            throw new RegraNegocioException("Envie um arquivo de imagem");
        }

        if (arquivo.getSize() > maxBytes) {
            throw new RegraNegocioException("A imagem excede o limite de " + (maxBytes / 1024 / 1024) + " MB");
        }

        String contentType = arquivo.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new RegraNegocioException("O arquivo enviado não é uma imagem válida");
        }

        try {
            String base64 = Base64.getEncoder().encodeToString(arquivo.getBytes());
            OcorrenciaImagem imagem = salvarImagemSalva(ocorrencia.getId(), base64, contentType, (int) arquivo.getSize());
            return new ImagemResponseDTO(imagem.getConteudoBase64(), imagem.getContentType(), imagem.getTamanhoBytes());
        } catch (java.io.IOException ex) {
            throw new RegraNegocioException("Não foi possível ler o arquivo enviado");
        }
    }

    @Transactional(readOnly = true)
    public ImagemArquivo obterImagem(Long id) {
        buscarEntidade(id);

        OcorrenciaImagem imagem = imagemRepository.findByOcorrenciaId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Esta ocorrência não possui imagem"));

        try {
            byte[] bytes = Base64.getDecoder().decode(imagem.getConteudoBase64());
            return new ImagemArquivo(bytes, imagem.getContentType());
        } catch (IllegalArgumentException ex) {
            throw new RegraNegocioException("A imagem armazenada está corrompida");
        }
    }

    @Transactional
    public void deletar(Long id, String email) {
        Ocorrencia ocorrencia = buscarEntidade(id);
        verificarPermissao(ocorrencia, email);

        notificacaoRepository.deleteByOcorrenciaId(id);
        comentarioRepository.deleteByOcorrenciaId(id);
        historicoRepository.deleteByOcorrenciaId(id);
        imagemRepository.deleteByOcorrenciaId(id);
        ocorrenciaRepository.delete(ocorrencia);
    }

    @Transactional(readOnly = true)
    public EstatisticasDTO estatisticas() {
        Map<String, Long> porStatus = new java.util.LinkedHashMap<>();
        for (StatusOcorrencia status : StatusOcorrencia.values()) {
            porStatus.put(status.name(), ocorrenciaRepository.countByStatus(status));
        }

        Map<String, Long> porCategoria = new java.util.LinkedHashMap<>();
        for (Categoria categoria : Categoria.values()) {
            long total = ocorrenciaRepository.countByCategoria(categoria);
            if (total > 0) {
                porCategoria.put(categoria.name(), total);
            }
        }

        Map<String, Long> porUrgencia = new java.util.LinkedHashMap<>();
        for (Urgencia urgencia : Urgencia.values()) {
            porUrgencia.put(urgencia.name(), ocorrenciaRepository.countByUrgencia(urgencia));
        }

        return new EstatisticasDTO(ocorrenciaRepository.count(), porStatus, porCategoria, porUrgencia);
    }

    @Transactional(readOnly = true)
    public EstatisticasDTO estatisticasDoUsuario(String email) {
        Map<String, Long> porStatus = new java.util.LinkedHashMap<>();
        for (StatusOcorrencia status : StatusOcorrencia.values()) {
            porStatus.put(status.name(), ocorrenciaRepository.countByUsuarioEmailIgnoreCaseAndStatus(email, status));
        }

        Map<String, Long> porCategoria = new java.util.LinkedHashMap<>();
        for (Categoria categoria : Categoria.values()) {
            long total = ocorrenciaRepository.countByUsuarioEmailIgnoreCaseAndCategoria(email, categoria);
            if (total > 0) {
                porCategoria.put(categoria.name(), total);
            }
        }

        Map<String, Long> porUrgencia = new java.util.LinkedHashMap<>();
        for (Urgencia urgencia : Urgencia.values()) {
            porUrgencia.put(urgencia.name(), ocorrenciaRepository.countByUsuarioEmailIgnoreCaseAndUrgencia(email, urgencia));
        }

        return new EstatisticasDTO(
                ocorrenciaRepository.countByUsuarioEmailIgnoreCase(email), porStatus, porCategoria, porUrgencia);
    }

    @Transactional(readOnly = true)
    public Ocorrencia buscarEntidade(Long id) {
        return ocorrenciaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ocorrência não encontrada"));
    }

    private PageResponse<OcorrenciaResumoDTO> resumir(Page<Ocorrencia> pagina) {
        List<Long> ids = pagina.getContent().stream().map(Ocorrencia::getId).toList();

        Set<Long> comImagem = ids.isEmpty()
                ? Set.of()
                : imagemRepository.findByOcorrenciaIdIn(ids).stream()
                        .map(OcorrenciaImagem::getOcorrenciaId)
                        .collect(Collectors.toSet());

        return PageResponse.from(pagina, o -> new OcorrenciaResumoDTO(o, comImagem.contains(o.getId())));
    }

    private OcorrenciaResponseDTO detalhar(Long id, boolean incluirImagem) {
        Ocorrencia ocorrencia = buscarEntidade(id);
        OcorrenciaImagem imagem = imagemRepository.findByOcorrenciaId(id).orElse(null);
        long comentarios = comentarioRepository.countByOcorrenciaId(id);

        List<HistoricoResponseDTO> historico = historicoRepository
                .findByOcorrenciaIdOrderByDataCriacaoAsc(id)
                .stream()
                .map(HistoricoResponseDTO::from)
                .toList();

        return new OcorrenciaResponseDTO(ocorrencia, imagem, comentarios, historico, incluirImagem);
    }

    private void aplicarDados(Ocorrencia ocorrencia, OcorrenciaRequestDTO dto) {
        ocorrencia.setCategoria(dto.getCategoria());
        ocorrencia.setDescricao(dto.getDescricao().trim());
        ocorrencia.setRua(vazioParaNulo(dto.getRua()));
        ocorrencia.setBairro(vazioParaNulo(dto.getBairro()));
        ocorrencia.setReferencia(vazioParaNulo(dto.getReferencia()));
        ocorrencia.setLatitude(dto.getLatitude());
        ocorrencia.setLongitude(dto.getLongitude());
        ocorrencia.setUrgencia(dto.getUrgencia());
    }

    private void salvarImagem(Long ocorrenciaId, String base64Recebido) {
        ImagemNormalizada imagem = normalizarImagem(base64Recebido);
        salvarImagemSalva(ocorrenciaId, imagem.conteudoBase64(), imagem.contentType(), imagem.tamanhoBytes());
    }

    private OcorrenciaImagem salvarImagemSalva(Long ocorrenciaId, String base64, String contentType, int tamanho) {
        OcorrenciaImagem imagem = imagemRepository.findByOcorrenciaId(ocorrenciaId)
                .orElseGet(OcorrenciaImagem::new);

        imagem.setOcorrenciaId(ocorrenciaId);
        imagem.setConteudoBase64(base64);
        imagem.setContentType(contentType);
        imagem.setTamanhoBytes(tamanho);

        return imagemRepository.save(imagem);
    }

    private ImagemNormalizada normalizarImagem(String base64Recebido) {
        String conteudo = base64Recebido.trim();
        String contentType = "image/jpeg";

        if (conteudo.startsWith("data:")) {
            int virgula = conteudo.indexOf(',');
            if (virgula <= 0) {
                throw new RegraNegocioException("Imagem em base64 inválida");
            }

            String meta = conteudo.substring(5, virgula);
            int pontoVirgula = meta.indexOf(';');
            contentType = pontoVirgula > 0 ? meta.substring(0, pontoVirgula) : meta;
            conteudo = conteudo.substring(virgula + 1);
        }

        if (!contentType.toLowerCase().startsWith("image/")) {
            throw new RegraNegocioException("O conteúdo enviado não é uma imagem");
        }

        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(conteudo.replaceAll("\\s", ""));
        } catch (IllegalArgumentException ex) {
            throw new RegraNegocioException("Imagem em base64 inválida");
        }

        if (bytes.length == 0) {
            throw new RegraNegocioException("A imagem enviada está vazia");
        }

        if (bytes.length > maxBytes) {
            throw new RegraNegocioException("A imagem excede o limite de " + (maxBytes / 1024 / 1024) + " MB");
        }

        return new ImagemNormalizada(
                Base64.getEncoder().encodeToString(bytes),
                contentType.toLowerCase(),
                bytes.length);
    }

    private void registrarHistorico(
            Ocorrencia ocorrencia,
            StatusOcorrencia anterior,
            StatusOcorrencia novo,
            String observacao,
            Usuario autor) {

        HistoricoOcorrencia historico = new HistoricoOcorrencia();
        historico.setOcorrenciaId(ocorrencia.getId());
        historico.setStatusAnterior(anterior);
        historico.setStatusNovo(novo);
        historico.setObservacao(observacao);

        if (autor != null) {
            historico.setUsuarioId(autor.getId());
            historico.setUsuarioNome(autor.getNome());
        }

        historicoRepository.save(historico);
    }

    private void verificarPermissao(Ocorrencia ocorrencia, String email) {
        boolean dono = ocorrencia.getUsuario() != null
                && ocorrencia.getUsuario().getEmail().equalsIgnoreCase(email);

        if (!dono && !usuarioAutenticado.isAdmin()) {
            throw new AcessoNegadoException("Você só pode alterar as ocorrências que você registrou");
        }
    }

    private Usuario buscarUsuario(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }

    private String vazioParaNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    public record ImagemArquivo(byte[] bytes, String contentType) {
    }

    private record ImagemNormalizada(String conteudoBase64, String contentType, int tamanhoBytes) {
    }
}
