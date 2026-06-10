export type TipoTransacao = "RECEITA" | "DESPESA"

export interface Categoria {
  id: number
  nome: string
  tipo: TipoTransacao
  doSistema?: boolean
}

export interface Transacao {
  id: number
  descricao: string
  valor: number
  data: string
  tipo: TipoTransacao
  categoriaId: number
  categoriaNome: string
  agendada?: boolean
  aviso?: string | null
}

export interface Resumo {
  inicio: string
  fim: string
  totalReceitas: number
  totalDespesas: number
  saldo: number
  saldoNegativo: boolean
}

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}
