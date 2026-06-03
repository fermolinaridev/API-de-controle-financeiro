# API de Controle Financeiro

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
- Docker Compose (PostgreSQL)

## Funcionalidades

- CRUD de transações (receitas/despesas) com paginação
- Filtros por mês, ano e categoria
- Resumo do mês atual: total de receitas, total de despesas, saldo e flag de saldo negativo
- CRUD de categorias com tipo (RECEITA/DESPESA)
- Validação de coerência: o tipo da transação precisa bater com o tipo da categoria
- Tratamento global de erros com mensagens amigáveis
- Documentação interativa via Swagger UI
- Dashboard web com cards de resumo, gráfico de pizza (despesas por categoria) e gráfico de linha (evolução receitas × despesas)
- **Autenticação via JWT** com Spring Security — cada usuário só enxerga suas próprias transações

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

### Modo prod (PostgreSQL via Docker)

```bash
docker-compose up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

Variáveis de ambiente (com defaults pro compose):

| Variável | Default |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/financas` |
| `DB_USERNAME` | `financas` |
| `DB_PASSWORD` | `financas` |

## Autenticação

Todas as rotas sob `/api/**` (exceto `/api/auth/**`) exigem um **Bearer token JWT** no header:

```
Authorization: Bearer <token>
```

**Usuário padrão** (criado no primeiro boot pelo `DataSeeder`):

| E-mail | Senha |
|---|---|
| `admin@financas.local` | `admin123` |

Você também pode criar uma conta nova em `POST /api/auth/register` ou pela tela de login do frontend.

## Endpoints principais

| Método | Rota | Auth | Descrição |
|---|---|---|---|
| `POST` | `/api/auth/register` | ❌ | Cria conta e retorna JWT |
| `POST` | `/api/auth/login` | ❌ | Autentica e retorna JWT |
| `POST` | `/api/transacoes` | ✅ | Cria uma nova transação |
| `GET` | `/api/transacoes` | ✅ | Lista paginada · query params: `mes`, `ano`, `categoriaId`, `page`, `size`, `sort` |
| `PUT` | `/api/transacoes/{id}` | ✅ | Atualiza uma transação |
| `DELETE` | `/api/transacoes/{id}` | ✅ | Remove uma transação |
| `GET` | `/api/transacoes/resumo` | ✅ | Resumo do mês atual (receitas, despesas, saldo) |
| `GET` | `/api/categorias` | ✅ | Lista todas as categorias |
| `POST` | `/api/categorias` | ✅ | Cria uma nova categoria |

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

## O que ainda falta

- [ ] Testes (`@SpringBootTest`, `MockMvc`, `@DataJpaTest`)
- [ ] Dockerfile da aplicação Spring (multi-stage)
- [ ] Dockerfile do frontend (Nginx servindo o `dist/`)
- [ ] GitHub Actions (CI: build + testes)
- [ ] Aviso de saldo negativo no `POST /api/transacoes` (hoje só aparece no `/resumo`)
- [ ] Lançamento agendado (transações com data futura — hoje `@PastOrPresent` bloqueia)
- [ ] Refresh token (hoje o JWT expira em 24h e o usuário precisa logar de novo)
