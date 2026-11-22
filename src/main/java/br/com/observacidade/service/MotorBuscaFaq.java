package br.com.observacidade.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import br.com.observacidade.entity.FaqAssistente;

/**
 * Motor de busca da base de conhecimento (FAQ).
 *
 * Usa TF-IDF + similaridade de cosseno, escrito em Java puro: não precisa de
 * serviço externo, banco vetorial ou chave de API. Se um dia for necessário
 * trocar por embeddings, basta criar outra implementação de busca mantendo o
 * mesmo contrato de {@link Achado}.
 */
public class MotorBuscaFaq {

    public record Item(Long id, String pergunta, String resposta) {
    }

    public record Achado(Long id, String pergunta, String resposta, double pontuacao) {
    }

    private static final Set<String> PARADAS = new HashSet<>(Arrays.asList(
            "a", "o", "as", "os", "um", "uma", "uns", "umas", "de", "do", "da", "dos", "das",
            "em", "no", "na", "nos", "nas", "por", "pelo", "pela", "pelos", "pelas", "para", "pra",
            "com", "sem", "sob", "sobre", "que", "e", "ou", "se", "ao", "aos", "eu", "me", "meu",
            "minha", "meus", "minhas", "voce", "voces", "vc", "ele", "ela", "eles", "elas", "isso",
            "isto", "aquilo", "esse", "essa", "este", "esta", "como", "quando", "onde", "qual",
            "quais", "porque", "tem", "tenho", "ter", "fazer", "faco", "foi", "ser", "sou", "sao",
            "esta", "estao", "estou", "ja", "mais", "muito", "mas", "nao", "sim", "tambem", "the",
            "and", "of", "to", "in", "is", "it", "aqui", "ali", "la", "entao", "tipo", "coisa",
            "quero", "queria", "gostaria", "preciso", "posso", "pode", "poderia", "consigo",
            "consegue", "ajuda", "ajudar", "favor", "por", "favor"));

    private final List<Item> itens;
    private final List<Map<String, Double>> vetores;
    private final Map<String, Double> idf;

    public MotorBuscaFaq(List<FaqAssistente> faqs) {
        this.itens = new ArrayList<>();

        List<List<String>> documentos = new ArrayList<>();

        for (FaqAssistente faq : faqs) {
            itens.add(new Item(faq.getId(), faq.getPergunta(), faq.getResposta()));

            // Peso maior para a pergunta e palavras-chave: são o que melhor resume o assunto.
            List<String> termos = new ArrayList<>();
            termos.addAll(tokenizar(faq.getPergunta()));
            termos.addAll(tokenizar(faq.getPergunta()));
            termos.addAll(tokenizar(faq.getPalavrasChave()));
            termos.addAll(tokenizar(faq.getPalavrasChave()));
            termos.addAll(tokenizar(faq.getPalavrasChave()));
            termos.addAll(tokenizar(faq.getResposta()));

            documentos.add(termos);
        }

        this.idf = calcularIdf(documentos);
        this.vetores = new ArrayList<>();

        for (List<String> documento : documentos) {
            vetores.add(vetorizar(contarTermos(documento), idf));
        }
    }

    public int tamanho() {
        return itens.size();
    }

    public List<Achado> buscar(String consulta, int limite) {
        List<Achado> achados = new ArrayList<>();
        if (consulta == null || consulta.isBlank() || itens.isEmpty()) return achados;

        Map<String, Double> consultaVetor = vetorizar(contarTermos(tokenizar(consulta)), idf);
        if (consultaVetor.isEmpty()) return achados;

        String consultaNormalizada = normalizar(consulta);

        for (int i = 0; i < itens.size(); i++) {
            double similaridade = cosseno(consultaVetor, vetores.get(i));

            // Reforço quando o texto contém literalmente um termo do título da FAQ.
            double bonus = 0;
            for (String termo : tokenizar(itens.get(i).pergunta())) {
                if (consultaNormalizada.contains(termo)) bonus += 0.02;
            }
            bonus = Math.min(bonus, 0.12);

            achados.add(new Achado(
                    itens.get(i).id(),
                    itens.get(i).pergunta(),
                    itens.get(i).resposta(),
                    Math.min(1.0, similaridade + bonus)));
        }

        achados.sort(Comparator.comparingDouble(Achado::pontuacao).reversed());

        return achados.size() > limite ? new ArrayList<>(achados.subList(0, limite)) : achados;
    }

    // ── texto ────────────────────────────────────────────────────────────────

    public static String normalizar(String texto) {
        if (texto == null) return "";
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    static List<String> tokenizar(String texto) {
        List<String> termos = new ArrayList<>();
        if (texto == null || texto.isBlank()) return termos;

        for (String bruto : normalizar(texto).split(" ")) {
            if (bruto.length() < 3) continue;
            if (PARADAS.contains(bruto)) continue;

            // Plural simples: "fotos" -> "foto", "ocorrencias" -> "ocorrencia".
            String termo = bruto;
            if (termo.length() >= 5 && termo.endsWith("s")) {
                termo = termo.substring(0, termo.length() - 1);
            }

            termos.add(termo);
        }

        return termos;
    }

    private static Map<String, Integer> contarTermos(List<String> termos) {
        Map<String, Integer> contagem = new HashMap<>();
        for (String termo : termos) {
            contagem.merge(termo, 1, Integer::sum);
        }
        return contagem;
    }

    private static Map<String, Double> calcularIdf(List<List<String>> documentos) {
        Map<String, Double> frequencia = new HashMap<>();
        int total = documentos.size();

        for (List<String> documento : documentos) {
            for (String termo : new LinkedHashSet<>(documento)) {
                frequencia.merge(termo, 1.0, Double::sum);
            }
        }

        Map<String, Double> valores = new HashMap<>();
        for (Map.Entry<String, Double> entrada : frequencia.entrySet()) {
            valores.put(entrada.getKey(), Math.log(1.0 + (total / entrada.getValue())));
        }
        return valores;
    }

    private static Map<String, Double> vetorizar(Map<String, Integer> contagem, Map<String, Double> idf) {
        Map<String, Double> vetor = new HashMap<>();
        double norma = 0;

        for (Map.Entry<String, Integer> entrada : contagem.entrySet()) {
            double peso = entrada.getValue() * idf.getOrDefault(entrada.getKey(), 1.0);
            vetor.put(entrada.getKey(), peso);
            norma += peso * peso;
        }

        if (norma == 0) return vetor;

        double raiz = Math.sqrt(norma);
        for (Map.Entry<String, Double> entrada : vetor.entrySet()) {
            entrada.setValue(entrada.getValue() / raiz);
        }
        return vetor;
    }

    private static double cosseno(Map<String, Double> a, Map<String, Double> b) {
        if (a.size() > b.size()) {
            Map<String, Double> troca = a;
            a = b;
            b = troca;
        }

        double soma = 0;
        for (Map.Entry<String, Double> entrada : a.entrySet()) {
            Double valor = b.get(entrada.getKey());
            if (valor != null) soma += entrada.getValue() * valor;
        }
        return soma;
    }
}
