/* Comunicação com a API do Observa Cidade. */

const CHAVE_TOKEN = 'oc.token';
const CHAVE_USUARIO = 'oc.usuario';

export class ApiError extends Error {
  constructor(status, mensagem, campos) {
    super(mensagem);
    this.name = 'ApiError';
    this.status = status;
    this.campos = campos || null;
  }
}

function lerUsuario() {
  try {
    return JSON.parse(localStorage.getItem(CHAVE_USUARIO) || 'null');
  } catch {
    return null;
  }
}

export const sessao = {
  token: localStorage.getItem(CHAVE_TOKEN) || null,
  usuario: lerUsuario(),

  get autenticado() {
    return Boolean(this.token);
  },

  get admin() {
    return this.usuario && this.usuario.role === 'ADMIN';
  },

  salvar(dados) {
    this.token = dados.token;
    this.usuario = {
      id: dados.id,
      nome: dados.nome,
      email: dados.email,
      role: dados.role,
    };

    localStorage.setItem(CHAVE_TOKEN, this.token);
    localStorage.setItem(CHAVE_USUARIO, JSON.stringify(this.usuario));
  },

  atualizarUsuario(usuario) {
    if (!usuario) return;
    this.usuario = { ...this.usuario, ...usuario };
    localStorage.setItem(CHAVE_USUARIO, JSON.stringify(this.usuario));
  },

  limpar() {
    this.token = null;
    this.usuario = null;
    localStorage.removeItem(CHAVE_TOKEN);
    localStorage.removeItem(CHAVE_USUARIO);
  },
};

async function requisitar(caminho, opcoes = {}) {
  const { method = 'GET', corpo, form, params } = opcoes;

  let url = caminho;

  if (params) {
    const query = new URLSearchParams();
    Object.entries(params).forEach(([chave, valor]) => {
      if (valor !== null && valor !== undefined && valor !== '') {
        query.append(chave, valor);
      }
    });
    const texto = query.toString();
    if (texto) url += (url.includes('?') ? '&' : '?') + texto;
  }

  const headers = {};
  if (sessao.token) headers.Authorization = `Bearer ${sessao.token}`;

  let body;
  if (form) {
    body = form;
  } else if (corpo !== undefined) {
    headers['Content-Type'] = 'application/json';
    body = JSON.stringify(corpo);
  }

  let resposta;
  try {
    resposta = await fetch(url, { method, headers, body });
  } catch {
    throw new ApiError(0, 'Não foi possível conectar ao servidor. Verifique se a API está no ar.');
  }

  if (resposta.status === 204) return null;

  const texto = await resposta.text();
  let dados = null;

  if (texto) {
    try {
      dados = JSON.parse(texto);
    } catch {
      dados = texto;
    }
  }

  if (!resposta.ok) {
    const publica = caminho.startsWith('/auth/login') || caminho.startsWith('/auth/cadastro');

    if (resposta.status === 401 && !publica) {
      sessao.limpar();
      window.dispatchEvent(new CustomEvent('oc:sessao-expirada'));
    }

    const mensagem =
      (dados && dados.mensagem) ||
      (typeof dados === 'string' && dados) ||
      `Erro ${resposta.status}`;

    throw new ApiError(resposta.status, mensagem, dados && dados.campos);
  }

  return dados;
}

export const api = {
  get: (caminho, params) => requisitar(caminho, { params }),
  post: (caminho, corpo) => requisitar(caminho, { method: 'POST', corpo }),
  put: (caminho, corpo) => requisitar(caminho, { method: 'PUT', corpo }),
  del: (caminho) => requisitar(caminho, { method: 'DELETE' }),
  upload: (caminho, form) => requisitar(caminho, { method: 'POST', form }),
};

/* -------------------------------------------------------------------------- */
/* Imagens (precisam do header Authorization, por isso viram blob URL)        */
/* -------------------------------------------------------------------------- */

const cacheImagens = new Map();

export async function urlImagem(id) {
  if (!id) return null;
  if (cacheImagens.has(id)) return cacheImagens.get(id);

  const resposta = await fetch(`/ocorrencias/${id}/imagem`, {
    headers: { Authorization: `Bearer ${sessao.token}` },
  });

  if (!resposta.ok) {
    throw new ApiError(resposta.status, 'Imagem indisponível');
  }

  const blob = await resposta.blob();
  const url = URL.createObjectURL(blob);
  cacheImagens.set(id, url);
  return url;
}

export function esquecerImagem(id) {
  const url = cacheImagens.get(id);
  if (url) {
    URL.revokeObjectURL(url);
    cacheImagens.delete(id);
  }
}

/* -------------------------------------------------------------------------- */
/* Sessão expirada                                                            */
/* -------------------------------------------------------------------------- */

export function ouvirSessaoExpirada(handler) {
  window.addEventListener('oc:sessao-expirada', handler);
}
