# EventFlow — Documento de Requisitos e Arquitetura

## 1. Contexto do problema

Sistemas de e-commerce e plataformas de pedidos tradicionalmente processam tudo dentro da mesma requisição HTTP: o cliente finaliza o pedido, e o backend — na mesma thread, no mesmo request — cobra o pagamento, reserva o estoque e dispara notificação. Esse modelo tem problemas conhecidos:

- **Acoplamento temporal**: se o serviço de pagamento está lento, o pedido inteiro fica lento.
- **Falha em cascata**: se a notificação por e-mail cair, o pedido pode falhar mesmo com pagamento aprovado.
- **Sem resiliência a pico de carga**: em Black Friday, por exemplo, todos os serviços precisam responder em tempo real, sem buffer.
- **Sem histórico de eventos**: é difícil saber "o que aconteceu" com um pedido ao longo do tempo.

O **EventFlow** nasce como um projeto de estudo para resolver esse problema através de **arquitetura orientada a eventos (Event-Driven Architecture)**, usando RabbitMQ como espinha dorsal de comunicação assíncrona entre serviços.

### 1.1 Objetivo do projeto

Não é construir um e-commerce completo. É construir um **fluxo de pedidos simulado, mas com profundidade real de engenharia**: retry, dead letter queue, idempotência, observabilidade de filas e visualização de eventos em tempo real. O produto (pedidos, pagamento, estoque) é só o veículo para praticar os conceitos de mensageria.

### 1.2 Problema que o sistema resolve (narrativa de negócio)

> Uma pequena operação de vendas online precisa que, ao finalizar um pedido, três coisas aconteçam de forma **desacoplada**: cobrança, reserva de estoque e notificação ao cliente. Essas três etapas não devem travar a resposta ao usuário, devem sobreviver a falhas temporárias dos serviços dependentes, e a operação precisa de visibilidade sobre o que está acontecendo (o que foi processado, o que falhou, o que está giver retry).

---

## 2. Visão geral da arquitetura

```
Frontend (Angular)
       │
       │ POST /orders
       ▼
  Order Service ──────► PostgreSQL (order-db)
       │
       │ publica evento: OrderCreated
       ▼
   RabbitMQ (orders.exchange - topic)
       │
       ├──► payment.queue ──────► Payment Service ──► PostgreSQL (payment-db)
       │
       ├──► inventory.queue ────► Inventory Service ─► PostgreSQL (inventory-db)
       │
       └──► notification.queue ─► Notification Service

   Falhas de qualquer consumer → retry (com backoff) → DLQ (dead-letter-exchange)

   Redis: idempotência (registro de eventId já processado) + cache
   SSE: Order Service expõe stream de eventos para o frontend em tempo real
```

### 2.1 Escopo por versão (roadmap)

| Versão | Escopo | Objetivo de aprendizado |
|---|---|---|
| **V1** | Order Service + Payment Service, fluxo feliz completo, RabbitMQ básico (exchange, queue, binding), Docker Compose | Publish/consume, exchanges, routing keys |
| **V2** | Adiciona Inventory Service. Implementa retry com backoff, Dead Letter Exchange/Queue, idempotência via Redis | Resiliência e consistência de eventos |
| **V3** | Adiciona Notification Service, Redis para cache, SSE para stream de eventos no frontend, dashboard básico | Tempo real e observabilidade de aplicação |
| **V4** | Prometheus + Grafana + OpenTelemetry para métricas e tracing das filas e serviços | Observabilidade de infraestrutura |
| **V5** | CI/CD com GitHub Actions, build automatizado, push para Docker Hub, testes automatizados no pipeline | DevOps e entrega contínua |

**Recomendação de execução**: construir V1 e V2 como base sólida (fluxo E2E + falha simulada + DLQ funcionando) antes de avançar. V3 em diante são incrementos, não bloqueadores.

---

## 3. Requisitos funcionais

### 3.1 Order Service

| ID | Requisito |
|---|---|
| RF01 | O sistema deve permitir criar um pedido com uma lista de itens (produto, quantidade, preço) |
| RF02 | Ao criar um pedido, o sistema deve publicar o evento `OrderCreated` no RabbitMQ |
| RF03 | O sistema deve permitir consultar um pedido por ID, incluindo seu status atual |
| RF04 | O sistema deve atualizar o status do pedido conforme os eventos de resposta chegam (`PaymentApproved`, `InventoryReserved`, etc.) |
| RF05 | O sistema deve expor um endpoint SSE (`/orders/stream`) que emite eventos de mudança de status em tempo real |
| RF06 | O sistema deve listar o histórico de eventos de um pedido específico (auditoria) |

### 3.2 Payment Service

| ID | Requisito |
|---|---|
| RF07 | O serviço deve consumir o evento `OrderCreated` (ou `PaymentRequested`, dependendo da modelagem de eventos) |
| RF08 | O serviço deve simular a cobrança do pedido (aprovação/rejeição pode ser randômica ou baseada em regra simples, ex: valor > limite = rejeitado) |
| RF09 | O serviço deve publicar `PaymentApproved` ou `PaymentRejected` conforme o resultado |
| RF10 | O serviço deve garantir idempotência: se o mesmo `eventId` chegar duas vezes, não deve cobrar duas vezes |

### 3.3 Inventory Service

| ID | Requisito |
|---|---|
| RF11 | O serviço deve consumir `PaymentApproved` e verificar/reservar estoque dos itens do pedido |
| RF12 | O serviço deve publicar `InventoryReserved` ou `InventoryUnavailable` |
| RF13 | O serviço deve manter uma tabela simples de produtos com quantidade disponível |

### 3.4 Notification Service

| ID | Requisito |
|---|---|
| RF14 | O serviço deve consumir o evento `OrderConfirmed` e, a partir dele, gerar a nota fiscal do pedido em PDF (número do pedido, itens, quantidades, valores e data) |
| RF14.1 | Após gerar a nota fiscal, o serviço deve enviar um e-mail ao cliente com o PDF anexado (via SMTP ou serviço de teste como Mailtrap/SendGrid) |
| RF14.2 | O serviço deve registrar, em sua própria base, que a nota fiscal de um `orderId` já foi gerada/enviada — se o evento `OrderConfirmed` for reprocessado (reentrega da fila, retry), o serviço deve identificar que já processou aquele `orderId` e **não gerar nem enviar a nota fiscal novamente** (idempotência) |
| RF14.3 | Falha na geração do PDF ou no envio do e-mail (ex: SMTP fora do ar) não deve reverter ou cancelar o pedido — o pedido permanece `CONFIRMADO`, e a mensagem de notificação segue o fluxo normal de retry/DLQ (RF18/RF19) |
| RF15 | O serviço deve consumir também o evento `OrderCancelled` e simular uma notificação simples de cancelamento ao cliente (pode ser só e-mail de texto, sem PDF) |

### 3.5 Mensageria — requisitos transversais

| ID | Requisito |
|---|---|
| RF16 | Toda mensagem publicada deve conter um `eventId` único, `eventType`, `timestamp` e `payload` |
| RF17 | Consumers devem confirmar mensagens processadas com sucesso via ACK, e rejeitar (NACK) mensagens com erro |
| RF18 | Mensagens rejeitadas devem ser reprocessadas (retry) até um número máximo de tentativas configurável |
| RF19 | Após esgotar as tentativas de retry, a mensagem deve ser roteada para uma Dead Letter Queue (DLQ) específica |
| RF20 | Deve existir um endpoint/tela para listar mensagens em DLQ, com evento, número de erros e data |
| RF21 | Deve ser possível reprocessar manualmente uma mensagem da DLQ (reenviar para a fila original) |

### 3.6 Dashboard / Observabilidade de aplicação (V3)

| ID | Requisito |
|---|---|
| RF22 | O dashboard deve mostrar contadores gerais: total de pedidos, pagamentos aprovados/rejeitados, mensagens em DLQ |
| RF23 | O dashboard deve mostrar um "Event Monitor" com stream ao vivo dos eventos publicados (via SSE) |
| RF24 | O dashboard deve mostrar o estado das filas do RabbitMQ (ready, unacked, DLQ) — via integração com a Management API do RabbitMQ |

---

## 4. Requisitos não funcionais

| ID | Requisito |
|---|---|
| RNF01 | Todos os serviços devem ser containerizados via Docker e orquestrados via Docker Compose (V1-V5) e Kubernetes (V6) |
| RNF02 | Cada serviço deve ter seu próprio banco de dados (padrão *database per service*) |
| RNF03 | Mensagens devem ser persistentes (durable queues, persistent messages) para sobreviver a restart do broker |
| RNF04 | O sistema deve suportar processamento assíncrono sem bloquear a resposta HTTP inicial do pedido |
| RNF05 | Testes de integração devem usar Testcontainers para RabbitMQ e PostgreSQL |
| RNF06 | Logs devem ser estruturados o suficiente para rastrear um pedido específico por múltiplos serviços (correlation ID) |
| RNF07 (V4) | Métricas de filas, consumers e latência devem ser expostas via Prometheus |
| RNF08 (V5) | Pipeline de CI deve rodar testes automaticamente a cada push/PR |

---

## 5. Modelagem de eventos

### 5.1 Lista de eventos do domínio

```
OrderCreated
PaymentRequested
PaymentApproved
PaymentRejected
InventoryReserved
InventoryUnavailable
OrderConfirmed
OrderCancelled
InvoiceGenerated
InvoiceSent
NotificationSent
NotificationFailed
```

### 5.2 Estrutura padrão de um evento (envelope)

```json
{
  "eventId": "uuid-v4",
  "eventType": "PaymentApproved",
  "orderId": "uuid-v4",
  "timestamp": "2026-09-04T14:32:01Z",
  "payload": {
    "amount": 150.00,
    "paymentMethod": "credit_card"
  }
}
```

### 5.3 Máquina de estados do pedido

```
CREATED → PAYMENT_PENDING → PAYMENT_APPROVED → INVENTORY_PENDING
   → INVENTORY_RESERVED → CONFIRMED

Qualquer etapa pode ir para CANCELLED se:
- PaymentRejected
- InventoryUnavailable
- Falha esgotou retries e foi para DLQ sem recuperação manual
```

---

## 6. Topologia RabbitMQ (proposta inicial)

```
Exchange: orders.exchange (type: topic)

Routing keys:
  order.created
  payment.requested
  payment.approved
  payment.rejected
  inventory.reserved
  inventory.unavailable
  order.confirmed
  order.cancelled

Queues:
  payment.queue          (binding: order.created)
  inventory.queue        (binding: payment.approved)
  notification.queue     (binding: order.confirmed, order.cancelled)

Dead Letter:
  Exchange: dlx.exchange
  Queue: failed-orders.queue

Cada queue principal é configurada com:
  x-dead-letter-exchange: dlx.exchange
  x-message-ttl (opcional, para expiração)
  retry count controlado via header (x-retry-count) ou fila intermediária de delay
```

---

## 7. Stack técnica proposta

| Camada | Tecnologia |
|---|---|
| Backend | Java 21 + Spring Boot |
| Mensageria | RabbitMQ (Spring AMQP) |
| Geração de PDF (nota fiscal) | OpenPDF ou iText |
| Envio de e-mail | Spring Mail + Mailtrap (ambiente de teste) |
| Banco de dados | PostgreSQL (um schema/instância por serviço) |
| Cache / Idempotência | Redis |
| Frontend | Angular + TypeScript |
| Tempo real | Server-Sent Events (SSE) |
| Testes | JUnit, Mockito, Testcontainers |
| Containerização | Docker + Docker Compose |
| Observabilidade (V4) | Prometheus + Grafana + OpenTelemetry |
| CI/CD (V5) | GitHub Actions + Docker Hub |

---

## 8. Fora de escopo (explicitamente)

- Pagamento real (gateway de pagamento de verdade) — será sempre simulado.
- Nota fiscal com validade fiscal real (integração com SEFAZ, NF-e homologada) — o PDF gerado é apenas uma representação simplificada para fins de estudo, sem valor fiscal.
- Autenticação/autorização robusta (JWT completo) — pode ser adicionado depois, não é o foco deste projeto (já foi trabalhado no Caraíva Tours Hub).
- Multi-tenancy.
- Internacionalização (i18n) do frontend na v1.

---

## 9. Critérios de "pronto" por versão

- **V1 pronto quando**: um pedido criado no frontend gera cobrança simulada, e o status do pedido reflete o resultado, tudo via Docker Compose com um único comando.
- **V2 pronto quando**: é possível simular falha proposital num consumer, ver o retry acontecer nos logs, e a mensagem cair na DLQ após esgotar tentativas — com endpoint para listar e reprocessar DLQ.
- **V3 pronto quando**: o dashboard mostra eventos em tempo real via SSE e reflete o estado real das filas do RabbitMQ.