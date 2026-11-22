/* Componentes e utilitários de interface. */

export function h(tag, props, ...filhos) {
  const el = document.createElement(tag);

  if (props) {
    for (const [chave, valor] of Object.entries(props)) {
      if (valor === null || valor === undefined || valor === false) continue;

      if (chave === 'class') el.className = valor;
      else if (chave === 'html') el.innerHTML = valor;
      else if (chave === 'text') el.textContent = valor;
      else if (chave === 'dataset') Object.assign(el.dataset, valor);
      else if (chave === 'style' && typeof valor === 'object') Object.assign(el.style, valor);
      else if (chave.startsWith('on') && typeof valor === 'function') {
        el.addEventListener(chave.slice(2).toLowerCase(), valor);
      } else {
        el.setAttribute(chave, valor === true ? '' : valor);
      }
    }
  }

  for (const filho of filhos.flat(3)) {
    if (filho === null || filho === undefined || filho === false) continue;
    el.append(filho instanceof Node ? filho : document.createTextNode(String(filho)));
  }

  return el;
}

export function limpar(el) {
  while (el.firstChild) el.removeChild(el.firstChild);
}

export function iniciais(nome) {
  if (!nome) return '?';
  const partes = nome.trim().split(/\s+/).filter(Boolean);
  if (partes.length === 1) return partes[0].slice(0, 2).toUpperCase();
  return (partes[0][0] + partes[partes.length - 1][0]).toUpperCase();
}

export function fmtDataHora(valor) {
  if (!valor) return '—';
  const data = new Date(valor);
  if (Number.isNaN(data.getTime())) return '—';
  return data.toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export function fmtData(valor) {
  if (!valor) return '—';
  const data = new Date(valor);
  if (Number.isNaN(data.getTime())) return '—';
  return data.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

export function tempoRelativo(valor) {
  if (!valor) return '';
  const data = new Date(valor);
  const diff = Date.now() - data.getTime();
  const minutos = Math.floor(diff / 60000);

  if (minutos < 1) return 'agora';
  if (minutos < 60) return `${minutos} min atrás`;

  const horas = Math.floor(minutos / 60);
  if (horas < 24) return `${horas} h atrás`;

  const dias = Math.floor(horas / 24);
  if (dias === 1) return 'ontem';
  if (dias < 30) return `${dias} dias atrás`;

  return fmtData(valor);
}

/* -------------------------------------------------------------------------- */
/* Ícones                                                                     */
/* -------------------------------------------------------------------------- */

const CAMINHOS = {
  home: '<path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><path d="M9 22V12h6v10"/>',
  list: '<path d="M8 6h13M8 12h13M8 18h13"/><path d="M3 6h.01M3 12h.01M3 18h.01"/>',
  plus: '<path d="M12 5v14M5 12h14"/>',
  bell: '<path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/>',
  headset:
    '<path d="M3 18v-6a9 9 0 0 1 18 0v6"/><path d="M21 19a2 2 0 0 1-2 2h-1a2 2 0 0 1-2-2v-3a2 2 0 0 1 2-2h3z"/><path d="M3 19a2 2 0 0 0 2 2h1a2 2 0 0 0 2-2v-3a2 2 0 0 0-2-2H3z"/>',
  user: '<path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>',
  chart: '<path d="M18 20V10M12 20V4M6 20v-6"/>',
  users:
    '<path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>',
  logout: '<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="M16 17l5-5-5-5"/><path d="M21 12H9"/>',
  search: '<circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>',
  pin: '<path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/>',
  camera:
    '<path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/><circle cx="12" cy="13" r="4"/>',
  filter: '<path d="M22 3H2l8 9.46V19l4 2v-8.54z"/>',
  close: '<path d="M18 6L6 18M6 6l12 12"/>',
  check: '<path d="M20 6L9 17l-5-5"/>',
  arrowLeft: '<path d="M19 12H5M12 19l-7-7 7-7"/>',
  arrowRight: '<path d="M5 12h14M12 5l7 7-7 7"/>',
  clock: '<circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/>',
  message: '<path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>',
  trash:
    '<path d="M3 6h18"/><path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>',
  edit: '<path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4z"/>',
  image:
    '<rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><path d="M21 15l-5-5L5 21"/>',
  shield: '<path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>',
  refresh: '<path d="M23 4v6h-6"/><path d="M1 20v-6h6"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10"/><path d="M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>',
  chevronLeft: '<path d="M15 18l-6-6 6-6"/>',
  chevronRight: '<path d="M9 18l6-6-6-6"/>',
  alert: '<path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><path d="M12 9v4M12 17h.01"/>',
  send: '<path d="M22 2L11 13"/><path d="M22 2l-7 20-4-9-9-4z"/>',
  info: '<circle cx="12" cy="12" r="10"/><path d="M12 16v-4M12 8h.01"/>',
  menu: '<path d="M3 12h18M3 6h18M3 18h18"/>',
  sparkles: '<path d="M12 3l1.9 4.6L18.5 9.5l-4.6 1.9L12 16l-1.9-4.6L5.5 9.5l4.6-1.9z"/>',
};

export function svgIcone(nome, tamanho = 20, espessura = 1.8) {
  const caminho = CAMINHOS[nome] || CAMINHOS.info;
  return `<svg width="${tamanho}" height="${tamanho}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="${espessura}" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${caminho}</svg>`;
}

export function icone(nome, tamanho = 20) {
  return h('span', { class: 'icone-svg', html: svgIcone(nome, tamanho), style: { display: 'inline-flex' } });
}

/* -------------------------------------------------------------------------- */
/* Etiquetas                                                                  */
/* -------------------------------------------------------------------------- */

export const ROTULOS_STATUS = {
  RECEBIDO: 'Recebida',
  EM_ANALISE: 'Em análise',
  EM_ANDAMENTO: 'Em andamento',
  RESOLVIDO: 'Resolvida',
};

export const ROTULOS_URGENCIA = {
  BAIXA: 'Baixa',
  MEDIA: 'Média',
  ALTA: 'Alta',
};

export const ROTULOS_SUPORTE = {
  ABERTO: 'Aberto',
  EM_ANDAMENTO: 'Em andamento',
  RESPONDIDO: 'Respondido',
  FECHADO: 'Fechado',
};

export function etiquetaStatus(status, rotulo) {
  const chave = String(status || '').toLowerCase();
  return h('span', { class: `etiqueta ${chave}` }, rotulo || ROTULOS_STATUS[status] || status || '—');
}

export function etiquetaUrgencia(urgencia, rotulo) {
  const chave = String(urgencia || '').toLowerCase();
  return h('span', { class: `etiqueta ${chave}` }, rotulo || ROTULOS_URGENCIA[urgencia] || urgencia || '—');
}

export function etiquetaSuporte(status) {
  const chave = String(status || '').toLowerCase();
  return h('span', { class: `etiqueta ${chave}` }, ROTULOS_SUPORTE[status] || status || '—');
}

/* -------------------------------------------------------------------------- */
/* Avisos flutuantes                                                          */
/* -------------------------------------------------------------------------- */

export function toast(mensagem, tipo = 'info', duracao = 4200) {
  const raiz = document.getElementById('toasts');
  if (!raiz) return;

  const item = h('div', { class: `toast ${tipo}`, role: 'status' }, mensagem);
  raiz.append(item);

  window.setTimeout(() => {
    item.style.opacity = '0';
    item.style.transform = 'translateY(6px)';
    item.style.transition = '180ms ease';
    window.setTimeout(() => item.remove(), 200);
  }, duracao);
}

/* -------------------------------------------------------------------------- */
/* Modal e confirmação                                                        */
/* -------------------------------------------------------------------------- */

let fecharAtual = null;

export function modal({ titulo, corpo, acoes = [], largo = false, aoFechar }) {
  fecharAtual?.();

  const raiz = document.getElementById('modal-root');
  const overlay = h('div', { class: 'overlay' });

  const caixa = h('div', { class: `modal${largo ? ' largo' : ''}`, role: 'dialog', 'aria-modal': 'true' });

  const fechar = () => {
    overlay.remove();
    document.removeEventListener('keydown', aoTecla);
    document.body.style.overflow = '';
    fecharAtual = null;
    if (aoFechar) aoFechar();
  };

  const aoTecla = (evento) => {
    if (evento.key === 'Escape') fechar();
  };

  fecharAtual = fechar;

  overlay.addEventListener('click', (evento) => {
    if (evento.target === overlay) fechar();
  });

  document.addEventListener('keydown', aoTecla);
  document.body.style.overflow = 'hidden';

  const cabecalho = h(
    'div',
    { class: 'modal-cabecalho' },
    h('h2', {}, titulo),
    h('button', { class: 'icone-botao', 'aria-label': 'Fechar', html: svgIcone('close', 18), onClick: fechar }),
  );

  caixa.append(cabecalho, h('div', { class: 'modal-corpo' }, corpo));

  if (acoes.length) {
    const rodape = h('div', { class: 'modal-acoes' });
    acoes.forEach((acao) => {
      rodape.append(
        h(
          'button',
          {
            class: `btn ${acao.classe || 'btn-neutro'}`,
            disabled: acao.desabilitado || false,
            onClick: () => acao.acao(fechar),
          },
          acao.rotulo,
        ),
      );
    });
    caixa.append(rodape);
  }

  overlay.append(caixa);
  raiz.append(overlay);

  const primeiro = caixa.querySelector('input, select, textarea, button');
  if (primeiro) window.setTimeout(() => primeiro.focus(), 60);

  return { fechar, caixa };
}

export function confirmar(mensagem, opcoes = {}) {
  const { titulo = 'Confirmar ação', rotulo = 'Confirmar', perigo = false } = opcoes;

  return new Promise((resolver) => {
    const { fechar } = modal({
      titulo,
      corpo: h('p', { style: { color: 'var(--cinza-600)', fontSize: '14px' } }, mensagem),
      acoes: [
        { rotulo: 'Cancelar', classe: 'btn-neutro', acao: () => { fechar(); resolver(false); } },
        {
          rotulo,
          classe: perigo ? 'btn-perigo' : 'btn-primario',
          acao: () => { fechar(); resolver(true); },
        },
      ],
      aoFechar: () => resolver(false),
    });
  });
}

/* -------------------------------------------------------------------------- */
/* Estados                                                                    */
/* -------------------------------------------------------------------------- */

export function carregando(texto = 'Carregando…') {
  return h('div', { class: 'carregando' }, h('div', { class: 'girando' }), texto);
}

export function vazio({ icone: nome = 'list', titulo, texto, acao }) {
  const bloco = h(
    'div',
    { class: 'vazio' },
    h('div', { class: 'icone', html: svgIcone(nome, 26) }),
    h('strong', {}, titulo),
    texto ? h('p', {}, texto) : null,
  );

  if (acao) {
    bloco.append(
      h(
        'div',
        { style: { marginTop: '14px' } },
        h('button', { class: 'btn btn-primario', onClick: acao.onClick }, acao.rotulo),
      ),
    );
  }

  return bloco;
}

export function aviso(tipo, mensagem) {
  return h('div', { class: `aviso ${tipo}` }, h('span', { html: svgIcone('info', 18) }), h('span', {}, mensagem));
}

/* -------------------------------------------------------------------------- */
/* Foto                                                                       */
/* -------------------------------------------------------------------------- */

/**
 * Redimensiona e comprime a imagem no navegador antes de enviar em base64.
 * Evita estourar o limite de 5 MB do backend com fotos de celular.
 */
export function comprimirImagem(arquivo, larguraMaxima = 1280, qualidade = 0.78) {
  return new Promise((resolver, rejeitar) => {
    if (!arquivo.type || !arquivo.type.startsWith('image/')) {
      rejeitar(new Error('Selecione um arquivo de imagem.'));
      return;
    }

    const leitor = new FileReader();

    leitor.onerror = () => rejeitar(new Error('Não foi possível ler a imagem.'));
    leitor.onload = () => {
      const imagem = new Image();

      imagem.onerror = () => rejeitar(new Error('Imagem inválida.'));
      imagem.onload = () => {
        const escala = Math.min(1, larguraMaxima / imagem.width);
        const largura = Math.max(1, Math.round(imagem.width * escala));
        const altura = Math.max(1, Math.round(imagem.height * escala));

        const tela = document.createElement('canvas');
        tela.width = largura;
        tela.height = altura;

        const contexto = tela.getContext('2d');
        contexto.drawImage(imagem, 0, 0, largura, altura);

        resolver(tela.toDataURL('image/jpeg', qualidade));
      };

      imagem.src = leitor.result;
    };

    leitor.readAsDataURL(arquivo);
  });
}
