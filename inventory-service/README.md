# Inventory Service

API responsável pelo catálogo e pelo controle de estoque do EventFlow.

## Produto

O recurso `Product` representa um item que pode ser mantido no estoque. Cada produto possui um nome único, preço e data de criação.

### Regras de negócio

- O nome é obrigatório, não pode ser vazio e tem no máximo 20 caracteres.
- O preço é obrigatório e deve ser maior que zero.
- Não podem existir dois produtos com o mesmo nome.
- A busca por nome usa correspondência exata.

### Endpoints

| Método | Rota | Descrição | Respostas possíveis |
| --- | --- | --- | --- |
| `POST` | `/api/products` | Cria um produto. | `201`, `400`, `409` |
| `GET` | `/api/products` | Lista produtos de forma paginada. | `200` |
| `GET` | `/api/products/name/{name}` | Busca um produto pelo nome. | `200`, `404` |
| `DELETE` | `/api/products/{id}` | Remove um produto pelo identificador. | `204`, `404` |

### Criar produto

`POST /api/products`

```json
{
  "name": "Notebook",
  "price": 3499.90
}
```

Resposta `201 Created`:

```json
{
  "id": 1,
  "name": "Notebook",
  "price": 3499.90,
  "createdAt": "2026-09-06T10:30:00"
}
```

Se o nome já estiver cadastrado, a API retorna `409 Conflict`. Dados inválidos retornam `400 Bad Request`.

### Listar produtos

`GET /api/products?page=0&size=10&sort=name,asc`

Os parâmetros `page`, `size` e `sort` são opcionais. A resposta contém a página solicitada e seus metadados de paginação.

### Buscar por nome

`GET /api/products/name/Notebook`

Retorna `404 Not Found` quando não existe um produto com o nome informado.

### Excluir produto

`DELETE /api/products/1`

Retorna `204 No Content` em caso de sucesso ou `404 Not Found` quando o identificador não existe.

## Swagger UI

Com a aplicação em execução na porta padrão, a documentação interativa fica disponível em:

- `http://localhost:8083/swagger-ui.html`
- Especificação OpenAPI JSON: `http://localhost:8083/v3/api-docs`

As anotações OpenAPI dos endpoints estão em `controller/docs/ProductControllerDocs.java`; o controller implementa esse contrato em `controller/ProductController.java`.
