package br.com.observacidade.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.observacidade.dto.OcorrenciaRequestDTO;
import br.com.observacidade.entity.Ocorrencia;
import br.com.observacidade.entity.Usuario;
import br.com.observacidade.repository.OcorrenciaRepository;
import br.com.observacidade.repository.ComentarioRepository;
import br.com.observacidade.repository.NotificacaoRepository;
import br.com.observacidade.repository.UsuarioRepository;

@Service
public class OcorrenciaService {

    @PersistenceContext
    private EntityManager entityManager;

    private final OcorrenciaRepository ocorrenciaRepository;
    private final ComentarioRepository comentarioRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public OcorrenciaService(
            OcorrenciaRepository ocorrenciaRepository,
            UsuarioRepository usuarioRepository,
            ComentarioRepository comentarioRepository,
            NotificacaoRepository notificacaoRepository) {
        this.ocorrenciaRepository = ocorrenciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.comentarioRepository = comentarioRepository;
        this.notificacaoRepository = notificacaoRepository;
    }

    public Ocorrencia salvar(OcorrenciaRequestDTO dto, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Ocorrencia ocorrencia = new Ocorrencia();
        ocorrencia.setCategoria(dto.getCategoria());
        ocorrencia.setDescricao(dto.getDescricao());
        ocorrencia.setRua(dto.getRua() == null || dto.getRua().isBlank() ? null : dto.getRua());
        ocorrencia.setBairro(dto.getBairro() == null || dto.getBairro().isBlank() ? null : dto.getBairro());
        ocorrencia.setReferencia(dto.getReferencia() == null || dto.getReferencia().isBlank() ? null : dto.getReferencia());
        ocorrencia.setUrgencia(dto.getUrgencia());
        ocorrencia.setImagemBase64(dto.getImagemBase64());
        ocorrencia.setUsuario(usuario);
        ocorrencia.setImagemBase64(dto.getImagemBase64());

        return ocorrenciaRepository.save(ocorrencia);
    }

    public List<Ocorrencia> listar() {
        return ocorrenciaRepository.findAll();
    }

    public Ocorrencia buscar(Long id) {
        return ocorrenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ocorrência não encontrada"));
    }

    public Ocorrencia atualizar(Long id, OcorrenciaRequestDTO dto) {
        Ocorrencia ocorrencia = buscar(id);

        ocorrencia.setCategoria(dto.getCategoria());
        ocorrencia.setDescricao(dto.getDescricao());
        ocorrencia.setRua(dto.getRua() == null || dto.getRua().isBlank() ? null : dto.getRua());
        ocorrencia.setBairro(dto.getBairro() == null || dto.getBairro().isBlank() ? null : dto.getBairro());
        ocorrencia.setReferencia(dto.getReferencia() == null || dto.getReferencia().isBlank() ? null : dto.getReferencia());
        ocorrencia.setUrgencia(dto.getUrgencia());
        ocorrencia.setImagemBase64(dto.getImagemBase64());

        return ocorrenciaRepository.save(ocorrencia);
    }

    public Ocorrencia salvarEntidade(Ocorrencia ocorrencia) {
        return ocorrenciaRepository.save(ocorrencia);
    }

    @Transactional
    public void deletar(Long id) {
        entityManager.createNativeQuery("DELETE FROM notificacoes WHERE ocorrencia_id = :id")
                .setParameter("id", id)
                .executeUpdate();

        entityManager.createNativeQuery("DELETE FROM comentarios WHERE ocorrencia_id = :id")
                .setParameter("id", id)
                .executeUpdate();

        entityManager.createNativeQuery("DELETE FROM ocorrencias WHERE id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }
}
