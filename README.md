# Notes App

Aplicação de notas com frontend estático, API REST em Jakarta EE (OpenLiberty) e banco PostgreSQL, orquestrada com Docker Compose.

## Rotas

```
nginx:80
 ├── / → arquivos estáticos (./frontend)
 │   ├── /              listagem de notas
 │   ├── /new.html      criar nota
 │   ├── /update.html   editar nota (?id=)
 │   └── /show.html     visualizar nota (?id=)
 │
 └── /api/* → proxy reverso para java-dev:8080/api/
     └── /api/notes
         ├── GET    → findAll()       200
         ├── GET    /{id} → findById()  200 / 204
         ├── POST   → create()          201 / 400
         ├── PUT    /{id} → update()    201 / 400
         └── DELETE /{id} → delete()    204
```

## Dependências

### Jakarta EE 10 (OpenLiberty)

| Feature               | Função                                   |
|-----------------------|------------------------------------------|
| `servlet-5.0`        | Servlet container                        |
| `restfulWs-3.0`      | REST endpoints (JAX-RS)                  |
| `persistence-3.0`    | JPA / EclipseLink                        |
| `beanValidation-3.0` | Validação de beans (`@Size`)             |
| `jdbc-4.2`           | Conexão com PostgreSQL                   |
| `jsonb-2.0`          | Serialização JSON                        |

### Testes (Gradle)

| Dependência                          | Uso                                    |
|--------------------------------------|----------------------------------------|
| `junit-jupiter`                     | Framework de testes                    |
| `jersey-client` + `jersey-hk2` + `jersey-media-jsonbinding` | Cliente HTTP JAX-RS |
| `selenium-java`                     | Automação de navegador                 |

### Banco

PostgreSQL com schema gerado automaticamente pelo JPA.

---

## Testes

### NoteControllerTest — Integração da API

Ordem fixa, compartilha `createdNoteId` via static:

| Ordem | Teste                | Descrição                                  |
|-------|----------------------|--------------------------------------------|
| 1     | `testCreate`         | POST `/api/notes` → 201                    |
| 2     | `testGetAll`         | GET `/api/notes` → 200 + lista não vazia   |
| 3     | `testGetOne`         | GET `/api/notes/{id}` → 200                |
| 4     | `testUpdate`         | PUT `/api/notes/{id}` → 201                |
| 5     | `testDelete`         | DELETE `/api/notes/{id}` → 204             |
| 6     | `testGetOne_NotFound` | GET `/api/notes/{id}` → 204 (já deletada) |

URL base: `base.uri` (padrão `http://nginx-dev/api`).

### NoteAcceptanceTest — Aceitação (Selenium)

`@TestInstance(PER_CLASS)`. Conecta ao Selenium Grid, usa `HttpClient` para limpeza de dados via API.

**Isolamento:** `@BeforeEach` deleta todas as notas via GET + DELETE; `@AfterEach` deleta a nota criada no teste.

| Teste                                    | Descrição                                              |
|------------------------------------------|--------------------------------------------------------|
| `testListPage_showsEmptyState`           | "Sem notas." na página inicial                         |
| `testListPage_showsExistingNotes`        | Cria nota via API, verifica renderização na lista      |
| `testCreateNote_createsAndRedirectsToList` | Preenche formulário, salva, verifica redirect e lista |
| `testUpdatePage_loadsExistingData`       | Navega para edição, verifica dados carregados          |
| `testUpdateNote_updatesAndRedirectsToList` | Edita nota, verifica redirect e lista atualizada      |

| Propriedade    | Padrão                        | Descrição                   |
|----------------|-------------------------------|-----------------------------|
| `selenium.hub` | `http://selenium-chrome:4444/wd/hub` | Selenium Grid       |
| `app.url`      | `http://nginx-dev`            | URL base da aplicação       |
| `api.base`     | `{app.url}/api`               | URL base da API REST        |

---

## Como Rodar

### Testes (comando único)

Sobe toda infraestrutura (db → java-dev → nginx-dev → selenium-chrome), executa os testes e encerra:

```bash
docker compose --profile test up --abort-on-container-exit --exit-code-from java-test
docker compose --profile test down
```

### Desenvolvimento

```bash
docker compose --profile dev up -d
```

Acessar `http://localhost`.

### Produção

```bash
cp example.env .env
docker compose --profile prod up -d
```

Acessar `http://localhost`.
