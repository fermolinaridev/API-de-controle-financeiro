import axios from "axios"
import type { Categoria, Page, Resumo, TipoTransacao, Transacao } from "./types"
import { auth } from "./auth"

const api = axios.create({ baseURL: "/api" })

api.interceptors.request.use(cfg => {
  const t = auth.getToken()
  if (t) cfg.headers.Authorization = `Bearer ${t}`
  return cfg
})

api.interceptors.response.use(
  r => r,
  err => {
    if (err.response?.status === 401 || err.response?.status === 403) {
      if (auth.isAuthenticated()) {
        auth.clear()
        window.location.href = "/"
      }
    }
    return Promise.reject(err)
  }
)

export interface AuthPayload { token: string; expiresIn: number; nome: string; email: string }

export const AuthApi = {
  login: (body: { email: string; senha: string }) =>
    api.post<AuthPayload>("/auth/login", body).then(r => r.data),
  register: (body: { nome: string; email: string; senha: string }) =>
    api.post<AuthPayload>("/auth/register", body).then(r => r.data),
}

export const TransacoesApi = {
  list: (params: { mes?: number; ano?: number; categoriaId?: number; page?: number; size?: number; sort?: string }) =>
    api.get<Page<Transacao>>("/transacoes", { params }).then(r => r.data),
  create: (body: TransacaoInput) => api.post<Transacao>("/transacoes", body).then(r => r.data),
  update: (id: number, body: TransacaoInput) => api.put<Transacao>(`/transacoes/${id}`, body).then(r => r.data),
  remove: (id: number) => api.delete(`/transacoes/${id}`).then(r => r.data),
  resumo: () => api.get<Resumo>("/transacoes/resumo").then(r => r.data),
}

export const CategoriasApi = {
  list: () => api.get<Categoria[]>("/categorias").then(r => r.data),
  create: (body: { nome: string; tipo: TipoTransacao }) =>
    api.post<Categoria>("/categorias", body).then(r => r.data),
}

export interface TransacaoInput {
  descricao: string
  valor: number
  data: string
  tipo: TipoTransacao
  categoriaId: number
}
