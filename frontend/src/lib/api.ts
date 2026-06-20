import axios, { AxiosError, type InternalAxiosRequestConfig } from "axios"
import type {
  AtivoBusca, Carteira, Categoria, Cotacao, Historico, Investimento,
  Page, Resumo, TipoAtivo, TipoTransacao, Transacao,
} from "./types"
import { auth } from "./auth"

const api = axios.create({ baseURL: "/api" })
// instância separada pra refresh — não passa pelos interceptors (evita loop)
const rawApi = axios.create({ baseURL: "/api" })

api.interceptors.request.use(cfg => {
  const t = auth.getAccessToken()
  if (t) cfg.headers.Authorization = `Bearer ${t}`
  return cfg
})

let refreshing: Promise<string | null> | null = null

async function tryRefresh(): Promise<string | null> {
  const rt = auth.getRefreshToken()
  if (!rt) return null
  try {
    const r = await rawApi.post<AuthPayload>("/auth/refresh", { refreshToken: rt })
    // rotação no backend: o refresh usado foi revogado, persiste o novo par
    auth.updateTokens(r.data.accessToken, r.data.refreshToken)
    return r.data.accessToken
  } catch {
    return null
  }
}

api.interceptors.response.use(
  r => r,
  async (err: AxiosError) => {
    const original = err.config as InternalAxiosRequestConfig & { _retried?: boolean }
    const status = err.response?.status
    const isAuth = original?.url?.includes("/auth/")
    if ((status === 401 || status === 403) && !original?._retried && !isAuth && auth.isAuthenticated()) {
      original._retried = true
      if (!refreshing) refreshing = tryRefresh().finally(() => { refreshing = null })
      const newToken = await refreshing
      if (newToken) {
        original.headers.Authorization = `Bearer ${newToken}`
        return api.request(original)
      }
      auth.clear()
      window.location.href = "/"
    }
    return Promise.reject(err)
  }
)

export interface AuthPayload { accessToken: string; refreshToken: string; expiresIn: number; nome: string; email: string }

export const AuthApi = {
  login: (body: { email: string; senha: string }) =>
    api.post<AuthPayload>("/auth/login", body).then(r => r.data),
  register: (body: { nome: string; email: string; senha: string }) =>
    api.post<AuthPayload>("/auth/register", body).then(r => r.data),
  logout: (refreshToken: string) =>
    rawApi.post("/auth/logout", { refreshToken }).then(() => undefined).catch(() => undefined),
}

export interface TransacaoListParams {
  mes?: number
  ano?: number
  dataInicio?: string
  dataFim?: string
  categoriaId?: number
  tipo?: TipoTransacao
  q?: string
  page?: number
  size?: number
  sort?: string
}

export const TransacoesApi = {
  list: (params: TransacaoListParams) =>
    api.get<Page<Transacao>>("/transacoes", { params }).then(r => r.data),
  create: (body: TransacaoInput) => api.post<Transacao>("/transacoes", body).then(r => r.data),
  update: (id: number, body: TransacaoInput) => api.put<Transacao>(`/transacoes/${id}`, body).then(r => r.data),
  remove: (id: number) => api.delete(`/transacoes/${id}`).then(r => r.data),
  resumo: () => api.get<Resumo>("/transacoes/resumo").then(r => r.data),
  importarCsv: (file: File) => {
    const form = new FormData()
    form.append("arquivo", file)
    return api.post<{ importadas: number; falhas: number; erros: string[] }>(
      "/transacoes/importar", form, { headers: { "Content-Type": "multipart/form-data" } }
    ).then(r => r.data)
  },
}

export const CategoriasApi = {
  list: () => api.get<Categoria[]>("/categorias").then(r => r.data),
  create: (body: { nome: string; tipo: TipoTransacao }) =>
    api.post<Categoria>("/categorias", body).then(r => r.data),
  update: (id: number, body: { nome: string; tipo: TipoTransacao }) =>
    api.put<Categoria>(`/categorias/${id}`, body).then(r => r.data),
  remove: (id: number) => api.delete(`/categorias/${id}`).then(r => r.data),
}

export interface TransacaoInput {
  descricao: string
  valor: number
  data: string
  tipo: TipoTransacao
  categoriaId: number
}

export interface InvestimentoInput {
  ticker: string
  classe: TipoAtivo
  quantidade: number
  precoMedio: number
}

export const InvestimentosApi = {
  carteira: () => api.get<Carteira>("/investimentos").then(r => r.data),
  create: (body: InvestimentoInput) => api.post<Investimento>("/investimentos", body).then(r => r.data),
  update: (id: number, body: InvestimentoInput) =>
    api.put<Investimento>(`/investimentos/${id}`, body).then(r => r.data),
  remove: (id: number) => api.delete(`/investimentos/${id}`).then(r => r.data),
}

export const MercadoApi = {
  dolar: () => api.get<Cotacao>("/mercado/dolar").then(r => r.data),
  bitcoin: () => api.get<Cotacao>("/mercado/bitcoin").then(r => r.data),
  buscar: (q: string) => api.get<AtivoBusca[]>("/mercado/buscar", { params: { q } }).then(r => r.data),
  cotacao: (ticker: string) => api.get<Cotacao>(`/mercado/cotacao/${ticker}`).then(r => r.data),
  historico: (ticker: string, range = "3mo", interval = "1d") =>
    api.get<Historico>(`/mercado/historico/${ticker}`, { params: { range, interval } }).then(r => r.data),
}
