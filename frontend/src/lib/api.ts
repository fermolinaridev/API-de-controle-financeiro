import axios from "axios"
import type { Categoria, Page, Resumo, TipoTransacao, Transacao } from "./types"

const api = axios.create({ baseURL: "/api" })

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
