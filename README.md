# API de Controle Financeiro

[![CI](https://github.com/<usuario>/<repo>/actions/workflows/ci.yml/badge.svg)](https://github.com/<usuario>/<repo>/actions/workflows/ci.yml)

Aplicação full-stack para registro de receitas e despesas, com dashboard interativo, filtros por período/categoria e resumo mensal automático.

## Stack

**Backend**
- Java 21 · Spring Boot 3.5
- Spring Web · Spring Data JPA · Bean Validation
- PostgreSQL (prod) · H2 (dev)
- Flyway (migrations)
- Springdoc OpenAPI 3 (Swagger UI)
- Lombok · Maven

**Frontend**
- React 18 · TypeScript · Vite
- Tailwind CSS 3 · componentes estilo shadcn/ui
- Recharts (gráficos) · Lucide (ícones) · Axios

**Infra**
- Dockerfile multi-stage (back e front) · Docker Compose (db + back + front)
- GitHub Actions (CI: testes do backend + build do frontend)

## Funcionalidades

- CRUD de transações (receitas/despesas) com paginação
- **Filtros avançados:** busca por descrição (LIKE case-insensitive), filtro por tipo (RECEITA/DESPESA), intervalo livre de datas (sobrepõe mes/ano), categoria — todos combináveis
- Resumo do mês atual: total de receitas, total de despesas, saldo e flag de saldo negativo
- CRUD de categorias com tipo (RECEITA/DESPESA)
- Validação de coerência: o tipo da transação precisa bater com o tipo da categoria
- Tratamento global de erros com mensagens amigáveis
- Documentação interativa via Swagger UI
- Dashboard web com cards de resumo, gráfico de pizza (despesas por categoria) e gráfico de linha (evolução receitas × despesas)
- **Autenticação via JWT** com access (1h) + refresh (7d) — cada usuário só enxerga suas próprias transações; refresh transparente no frontend via interceptor
- **Logout server-side revogável:** refresh token carrega um `jti` (UUID); o `/logout` o adiciona à blacklist e qualquer tentativa subsequente de `/refresh` retorna 401. Job hourly limpa entradas expiradas
- **Rotação de refresh token:** cada `/refresh` revoga o token usado e emite um novo — refresh é de uso único; um token vazado e já usado não serve para nada
- **Categorias do sistema vs. pessoais:** as categorias seedadas são globais e imutáveis (`doSistema: true`); categorias criadas pelo usuário são privadas — outro usuário não as vê nem altera
- **Importação de extrato em CSV** (`descricao,valor,data,tipo,categoria`, com suporte a campos entre aspas) — criação automática de categorias, relatório de erros por linha, limite de 5000 linhas e upload de até 1MB
- CRUD completo de categorias (criar, editar, excluir) com proteção contra deletar/mudar tipo de categoria em uso
- **Dark mode** com persistência em `localStorage` e respeito à preferência do sistema
- **Layout mobile-friendly** com sidebar em drawer e tabela adaptativa
- **Investimentos:** aba dedicada com cotações ao vivo (dólar e Bitcoin), carteira pessoal de ativos com cálculo de rendimento (R$ e %) usando preços em tempo real, e busca de qualquer ação da B3 para ver o gráfico de preço. **Funciona sem cadastro:** dólar/BTC via [AwesomeAPI](https://docs.awesomeapi.com.br/), busca e preço de ações via [brapi.dev](https://brapi.dev) (`/quote/list`, sem token) e gráfico histórico via mfinance.com.br (~3 meses)

## Como rodar

### Pré-requisitos
- Java 21+
- Node 18+ (para o frontend)
- Docker (opcional, só pro perfil `prod`)

### Modo dev (H2 em memória — não precisa de banco)

Em dois terminais:

```bash
# Terminal 1 — backend
./mvnw spring-boot:run
```

```bash
# Terminal 2 — frontend
cd frontend
npm install   # primeira vez apenas
npm run dev
```

| Serviço | URL |
|---|---|
| Frontend (dashboard) | http://localhost:5173 |
| Backend (API) | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| Console H2 | http://localhost:8080/h2 (JDBC `jdbc:h2:mem:financas`, user `sa`, sem senha) |

### Modo full Docker (db + backend + frontend num único comando)

```bash
docker-compose up --build
```

| Serviço | URL |
|---|---|
| Frontend (Nginx) | http://localhost:8081 |
| Backend (API) | http://localhost:8080 |
| PostgreSQL | localhost:5432 |

Variáveis de ambiente suportadas:

| Variável | Default |
|---|---|
| `DB_URL` | `jdbc:postgresql://db:5432/financas` |
| `DB_USERNAME` | `financas` |
| `DB_PASSWORD` | `financas` |
| `JWT_SECRET` | obrigatório no perfil `prod` (o boot falha com o secret default); o compose traz um valor de demo local |
| `BRAPI_TOKEN` | **opcional.** As cotações funcionam sem cadastro; o token só eleva o rate-limit da brapi. Deixe vazio que tudo funciona |
| `BRAPI_BASE_URL` | `https://brapi.dev/api` (default) |

## Autenticação

Todas as rotas sob `/api/**` (exceto `/api/auth/**`) exigem um **Bearer token JWT** no header:

```
Authorization: Bearer <token>
```

**Usuário padrão** (criado no primeiro boot pelo `DataSeeder`, **apenas no perfil `dev`** — em prod não há credenciais default):

| E-mail | Senha |
|---|---|
| `admin@financas.local` | `admin123` |

Você também pode criar uma conta nova em `POST /api/auth/register` ou pela tela de login do frontend.

## Endpoints principais

| Método | Rota | Auth | Descrição |
|---|---|---|---|
| `POST` | `/api/auth/register` | ❌ | Cria conta e retorna `accessToken` + `refreshToken` |
| `POST` | `/api/auth/login` | ❌ | Autentica e retorna `accessToken` + `refreshToken` |
| `POST` | `/api/auth/refresh` | ❌ | Troca um `refreshToken` válido por um novo `accessToken` |
| `POST` | `/api/auth/logout` | ❌ | Revoga o `refreshToken` (adiciona o `jti` à blacklist) |
| `POST` | `/api/transacoes` | ✅ | Cria uma nova transação |
| `GET` | `/api/transacoes` | ✅ | Lista paginada · params: `mes`/`ano`, `dataInicio`/`dataFim` (sobrepõe mes/ano), `categoriaId`, `tipo`, `q` (busca por descrição), `page`, `size`, `sort` |
| `PUT` | `/api/transacoes/{id}` | ✅ | Atualiza uma transação |
| `DELETE` | `/api/transacoes/{id}` | ✅ | Remove uma transação |
| `GET` | `/api/transacoes/resumo` | ✅ | Resumo do mês atual (receitas, despesas, saldo) |
| `POST` | `/api/transacoes/importar` | ✅ | Importa transações de um CSV (multipart) |
| `GET` | `/api/categorias` | ✅ | Lista todas as categorias |
| `POST` | `/api/categorias` | ✅ | Cria uma nova categoria |
| `PUT` | `/api/categorias/{id}` | ✅ | Atualiza nome/tipo (bloqueia mudança de tipo se em uso) |
| `DELETE` | `/api/categorias/{id}` | ✅ | Remove (bloqueia se houver transação usando) |
| `GET` | `/api/investimentos` | ✅ | Carteira do usuário com preço atual, valor e rendimento + resumo total |
| `POST` | `/api/investimentos` | ✅ | Adiciona um ativo à carteira (ticker, classe, quantidade, preço médio) |
| `PUT` | `/api/investimentos/{id}` | ✅ | Atualiza um ativo da carteira |
| `DELETE` | `/api/investimentos/{id}` | ✅ | Remove um ativo da carteira |
| `GET` | `/api/mercado/dolar` | ✅ | Cotação atual do dólar (USD-BRL) via AwesomeAPI |
| `GET` | `/api/mercado/bitcoin` | ✅ | Cotação atual do Bitcoin (BTC-BRL) via AwesomeAPI |
| `GET` | `/api/mercado/buscar?q=` | ✅ | Busca ativos da B3 por termo (para o autocomplete/gráfico) |
| `GET` | `/api/mercado/cotacao/{ticker}` | ✅ | Cotação atual de um ativo da B3 |
| `GET` | `/api/mercado/historico/{ticker}?range=3mo&interval=1d` | ✅ | Série histórica de preço para o gráfico (mfinance, ~3 meses) |

### Exemplos `curl`

```bash
# 1) login (pega o token)
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@financas.local","senha":"admin123"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")

# 2) listar categorias
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/categorias

# 3) criar uma receita
curl -X POST http://localhost:8080/api/transacoes \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "descricao": "Salário Junho",
    "valor": 5000.00,
    "data": "2026-06-01",
    "tipo": "RECEITA",
    "categoriaId": 1
  }'

# 4) listar transações de junho/2026
curl -H "Authorization: Bearer $TOKEN" 'http://localhost:8080/api/transacoes?mes=6&ano=2026'

# 5) resumo do mês atual
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/transacoes/resumo
```

## Estrutura do projeto

```
.
├── src/main/java/com/fernando/financas/
│   ├── config/          # OpenAPI, CORS
│   ├── controller/      # CategoriaController, TransacaoController
│   ├── dto/             # records de request/response
│   ├── entity/          # Usuario, Categoria, Transacao, TipoTransacao
│   ├── exception/       # exceções de negócio + ApiExceptionHandler global
│   ├── repository/      # Spring Data JPA
│   └── service/         # regras de negócio
├── src/main/resources/
│   ├── application.properties             # perfil dev ativo
│   ├── application-dev.properties         # H2
│   ├── application-prod.properties        # Postgres
│   └── db/migration/                      # Flyway
│       ├── V1__init.sql                   # schema
│       └── V2__seed.sql                   # usuário padrão + categorias
├── frontend/
│   ├── src/
│   │   ├── components/  # SummaryCards, TransactionsTable, dialogs, charts
│   │   ├── lib/         # api client, types, utils
│   │   └── pages/       # Dashboard
│   └── vite.config.ts   # proxy /api → :8080
├── docker-compose.yml   # PostgreSQL
└── pom.xml
```

## Decisões técnicas

- **DTOs como `record`** — imutáveis, concisos, sem boilerplate
- **`@PrePersist`** preenche `criado_em` no lado da aplicação (não depende do default do banco em ambientes diversos)
- **Filtros opcionais via JPQL com `IS NULL`** no `TransacaoRepository.buscar` — uma única query atende todos os combos de filtro
- **Coerência tipo categoria/transação** validada no service (`RegraNegocioException` → HTTP 422)
- **Springdoc 2.8.x** porque versões anteriores são incompatíveis com Spring 6.2 (Boot 3.5)
- **Frontend desacoplado** — Vite serve dev separado e faz proxy de `/api` pro backend, evitando CORS em desenvolvimento; em prod o `CorsConfig` libera origens conhecidas
- **Lançamento agendado** — `data` aceita futuro; o response marca `agendada: true` quando aplicável
- **Aviso de saldo negativo no POST** — o `TransacaoResponse` traz `aviso` quando a despesa criada leva o saldo do mês a ficar negativo, sem bloquear a operação
- **Isolamento de dados por usuário** — todas as queries de transação filtram por `usuario_id` no banco; nem por força bruta um usuário acessa dados do outro

## O que ainda falta

- [ ] Exportação CSV/PDF do extrato
- [x] Menu com mais páginas (Dashboard + Investimentos)
- [ ] Ações dos EUA nativas em dólar (hoje só B3 — brapi não cobre ações americanas)
- [ ] Histórico de aportes por ativo (hoje a carteira guarda posição consolidada: quantidade + preço médio)
