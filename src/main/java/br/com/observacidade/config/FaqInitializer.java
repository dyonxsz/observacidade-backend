package br.com.observacidade.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import br.com.observacidade.entity.FaqAssistente;
import br.com.observacidade.repository.FaqAssistenteRepository;

/**
 * Carrega a base de conhecimento do assistente virtual.
 *
 * A carga é idempotente: itens já existentes (mesma pergunta) são ignorados.
 * Assim a base pode ser ampliada em novas versões sem duplicar registros.
 */
@Component
@Order(20)
public class FaqInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FaqInitializer.class);

    private final FaqAssistenteRepository repository;

    public FaqInitializer(FaqAssistenteRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        List<FaqAssistente> base = List.of(
                faq("Como registro uma ocorrência?",
                        "Na tela inicial toque no botão central de nova ocorrência, escolha a categoria "
                                + "(por exemplo buraco, iluminação, lixo ou alagamento), descreva o problema, "
                                + "confirme o local e envie. O GPS do celular é usado para marcar o ponto.",
                        "Ocorrências",
                        "registrar ocorrencia, criar denuncia, nova ocorrencia, reportar problema, enviar ocorrencia"),

                faq("Preciso anexar foto para abrir uma ocorrência?",
                        "A foto é opcional, mas ajuda muito a equipe a entender o problema. Você pode tirar "
                                + "uma foto na hora ou escolher uma da galeria. A imagem fica anexada à ocorrência.",
                        "Ocorrências",
                        "foto, imagem, camera, galeria, anexar, evidencia, print"),

                faq("Por que o aplicativo pede acesso à localização?",
                        "A localização é usada para marcar automaticamente o endereço da ocorrência e mostrar "
                                + "no mapa o ponto exato do problema. Sem esse acesso você ainda pode informar o "
                                + "endereço manualmente.",
                        "Localização",
                        "gps, localizacao, endereco, mapa, permissao, coordenadas, onde fica"),

                faq("Como acompanho o andamento da minha ocorrência?",
                        "Abra a ocorrência na lista da tela inicial. Lá aparecem o status atual "
                                + "(Pendente, Em andamento ou Resolvida), o histórico de mudanças e os "
                                + "comentários da equipe.",
                        "Ocorrências",
                        "acompanhar, status, andamento, historico, protocolo, resposta, situacao"),

                faq("O que significa cada status da ocorrência?",
                        "Pendente: registrada e aguardando análise. Em andamento: a equipe já assumiu e está "
                                + "atuando. Resolvida: o problema foi atendido. Você recebe uma notificação "
                                + "quando o status muda.",
                        "Ocorrências",
                        "status, pendente, em andamento, resolvida, significado, cores"),

                faq("Posso editar ou excluir uma ocorrência que registrei?",
                        "Sim, mas apenas as ocorrências criadas por você. Na tela de detalhes aparecem as "
                                + "opções de editar e excluir. Ocorrências de outros cidadãos ficam somente "
                                + "para visualização.",
                        "Ocorrências",
                        "editar, alterar, excluir, apagar, deletar, remover, minha ocorrencia, permissao"),

                faq("Quais categorias de ocorrência existem?",
                        "As categorias são definidas pela prefeitura e aparecem no formulário de nova "
                                + "ocorrência, com nome e ícone. Exemplos comuns: buraco na via, iluminação "
                                + "pública, limpeza e lixo, alagamento, poda de árvore e sinalização.",
                        "Ocorrências",
                        "categoria, tipos, lista, buraco, iluminacao, lixo, alagamento, arvore, sinalizacao"),

                faq("Recebo aviso quando minha ocorrência é atualizada?",
                        "Sim. Sempre que o status muda ou a equipe comenta, aparece uma notificação no ícone "
                                + "de sino da tela inicial. Toque nela para ver o detalhe da ocorrência.",
                        "Notificações",
                        "notificacao, aviso, sino, alerta, push, atualizacao, acompanhar"),

                faq("Como funciona o aplicativo para o administrador?",
                        "O administrador vê um painel com todas as ocorrências, pode filtrar por status, "
                                + "alterar o status, responder comentários, gerenciar usuários e consultar "
                                + "estatísticas e chamados de suporte.",
                        "Administração",
                        "admin, administrador, painel, gestao, moderar, gerenciar, funcionario, prefeitura"),

                faq("O que o administrador consegue fazer com os usuários?",
                        "No menu de usuários o administrador consulta a lista de cidadãos cadastrados, "
                                + "promove ou remove permissões e pode desativar contas em caso de uso "
                                + "indevido do aplicativo.",
                        "Administração",
                        "usuarios, admin, permissao, promover, desativar conta, bloquear, gerenciar pessoas"),

                faq("Onde vejo as estatísticas das ocorrências?",
                        "Na tela inicial do administrador há um resumo com totais por status e por categoria, "
                                + "além dos indicadores mais recentes. Esses números são calculados direto no "
                                + "banco de dados da aplicação.",
                        "Administração",
                        "estatisticas, numeros, indicadores, grafico, total, relatorio, dashboard"),

                faq("Como abro um chamado para o suporte?",
                        "Acesse Ajuda e Suporte no perfil, escreva o assunto e a mensagem. O chamado fica "
                                + "registrado e a equipe responde pelo próprio aplicativo, na lista dos seus "
                                + "chamados.",
                        "Suporte",
                        "suporte, ajuda, chamado, contato, reclamacao, falar com equipe, problema no app"),

                faq("Esqueci minha senha. Como recupero o acesso?",
                        "Na tela de login toque em esqueci minha senha e informe o telefone cadastrado. "
                                + "Siga as instruções enviadas para definir uma nova senha. Se não conseguir, "
                                + "abra um chamado em Ajuda e Suporte.",
                        "Conta",
                        "senha, recuperar senha, esqueci, login, acesso, trocar senha, redefinir"),

                faq("Como altero meus dados de cadastro?",
                        "Abra a tela de perfil. Lá você atualiza nome, telefone e foto, e também pode trocar "
                                + "a senha. O e-mail de login não pode ser alterado pelo aplicativo.",
                        "Conta",
                        "perfil, alterar dados, nome, telefone, email, trocar senha, editar cadastro"),

                faq("Meus dados pessoais ficam seguros?",
                        "Sim. O acesso à API é protegido por autenticação com token, as senhas são guardadas "
                                + "criptografadas e cada cidadão só enxerga e altera as próprias ocorrências. "
                                + "As imagens também exigem autenticação para serem abertas.",
                        "Privacidade",
                        "seguranca, lgpd, privacidade, dados pessoais, senha, criptografia, token"),

                faq("Quanto tempo leva para uma ocorrência ser resolvida?",
                        "O prazo varia conforme a categoria e a agenda da secretaria responsável, por isso o "
                                + "aplicativo não promete uma data fixa. Você acompanha cada etapa pelo status "
                                + "e pelas notificações.",
                        "Ocorrências",
                        "prazo, quanto tempo, demora, quando resolve, previsao, sla"),

                faq("O aplicativo funciona sem internet?",
                        "O envio e a consulta das ocorrências precisam de conexão com a internet. Se a rede "
                                + "estiver instável, os dados digitados podem ser perdidos; envie novamente "
                                + "quando a conexão voltar.",
                        "Aplicativo",
                        "offline, sem internet, conexao, rede, erro de rede, nao carrega"),

                faq("O que é o assistente virtual do Observa Cidade?",
                        "É este chat de ajuda. Ele consulta uma base de perguntas frequentes sobre o "
                                + "aplicativo e, quando a inteligência artificial está habilitada, redige a "
                                + "resposta com base nessa mesma base. Se não souber, orienta a abrir um "
                                + "chamado no suporte.",
                        "Assistente",
                        "assistente, chatbot, ia, inteligencia artificial, ajuda automatica, faq, robo"),

                faq("Como o assistente usa inteligência artificial?",
                        "A pergunta é comparada com a base de conhecimento por similaridade de texto. Os "
                                + "trechos mais relevantes servem de contexto para o modelo de linguagem, que "
                                + "responde em português. Sem chave de IA configurada, a própria base responde.",
                        "Assistente",
                        "ia, llm, modelo, gemini, openai, como funciona, similaridade, embeddings"),

                faq("O assistente pode errar a resposta?",
                        "Pode. Ele só usa as informações da base de conhecimento e evita inventar prazos, "
                                + "telefones ou políticas. Quando não encontra o assunto, avisa que não sabe e "
                                + "sugere abrir um chamado em Ajuda e Suporte.",
                        "Assistente",
                        "erro, errado, confiavel, alucinacao, nao sabe, resposta incorreta")
        );

        int novos = 0;
        for (FaqAssistente item : base) {
            if (repository.findFirstByPerguntaIgnoreCase(item.getPergunta()).isPresent()) {
                continue;
            }
            repository.save(item);
            novos++;
        }

        if (novos > 0) {
            log.info("Base de conhecimento do assistente: {} novas perguntas cadastradas (total {}).",
                    novos, repository.count());
        }
    }

    private static FaqAssistente faq(String pergunta, String resposta, String categoria, String palavrasChave) {
        FaqAssistente item = new FaqAssistente();
        item.setPergunta(pergunta);
        item.setResposta(resposta);
        item.setCategoria(categoria);
        item.setPalavrasChave(palavrasChave);
        item.setAtivo(true);
        return item;
    }
}
