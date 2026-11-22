package br.com.observacidade.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import br.com.observacidade.entity.Ocorrencia;
import br.com.observacidade.enums.Categoria;
import br.com.observacidade.enums.StatusOcorrencia;
import br.com.observacidade.enums.Urgencia;
import jakarta.persistence.criteria.Predicate;

public final class OcorrenciaSpecifications {

    private OcorrenciaSpecifications() {
    }

    public static Specification<Ocorrencia> filtro(
            StatusOcorrencia status,
            Categoria categoria,
            Urgencia urgencia,
            String bairro,
            String busca) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (categoria != null) {
                predicates.add(cb.equal(root.get("categoria"), categoria));
            }
            if (urgencia != null) {
                predicates.add(cb.equal(root.get("urgencia"), urgencia));
            }
            if (bairro != null && !bairro.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("bairro")), "%" + bairro.trim().toLowerCase() + "%"));
            }
            if (busca != null && !busca.isBlank()) {
                String like = "%" + busca.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("descricao")), like),
                        cb.like(cb.lower(root.get("rua")), like),
                        cb.like(cb.lower(root.get("bairro")), like),
                        cb.like(cb.lower(root.get("referencia")), like)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Ocorrencia> doUsuario(String email) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("usuario").get("email")), email.toLowerCase());
    }
}
