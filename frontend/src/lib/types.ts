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

// --- Investimentos ---

export type TipoAtivo = "ACAO" | "FII" | "BDR" | "CRIPTO" | "OUTRO"

export interface Investimento {
  id: number
  ticker: string
  nome: string | null
  classe: TipoAtivo
  quantidade: number
  precoMedio: number
  valorInvestido: number
  precoAtual: number | null
  valorAtual: number | null
  rendimento: number | null
  rendimentoPercentual: number | null
  moeda: string | null
  cotacaoIndisponivel: boolean
}

export interface CarteiraResumo {
  totalInvestido: number
  valorAtualTotal: number
  rendimentoTotal: number
  rendimentoPercentualTotal: number
}

export interface Carteira {
  resumo: CarteiraResumo
  itens: Investimento[]
}

export interface Cotacao {
  ticker: string
  nome: string
  preco: number
  variacaoPercentual: number | null
  moeda: string
}

export interface AtivoBusca {
  ticker: string
  nome: string | null
  logo: string | null
  tipo: string | null
}

export interface PontoHistorico {
  data: string
  fechamento: number
}

export interface Historico {
  ticker: string
  nome: string
  pontos: PontoHistorico[]
}
