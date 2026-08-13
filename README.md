# Cartão de Crédito Interativo — Android Studio

Aplicativo Android desenvolvido em **Kotlin** com **ConstraintLayout** para a
Atividade 3 da disciplina de Programação Para Dispositivos Móveis (IFTM).

O app simula a interface de um cartão de crédito, refletindo em tempo real,
na parte superior da tela, os dados digitados em um formulário abaixo.

## Funcionalidades

- **Preview em tempo real**: número, nome do titular e validade digitados no
  formulário são exibidos instantaneamente no cartão exibido no topo.
- **Máscara automática do número do cartão**: insere um espaço a cada 4
  dígitos digitados (`1234 5678 9012 3456`), limitado a 16 dígitos.
- **Máscara automática da validade**: formata a entrada no padrão `MM/AA`
  conforme o usuário digita.
- **Validação de dados**: ao clicar em "Confirmar", o app verifica se o
  número do cartão possui 16 dígitos, se o nome tem ao menos 3 caracteres e
  se a validade está em um formato válido (mês entre 01 e 12), exibindo uma
  mensagem de erro caso algo esteja incorreto.

## Desafios técnicos implementados

### Desafio 1 — UI Dinâmica (flip do cartão)

O cartão utiliza um `ViewFlipper` contendo duas `CardView` (frente e verso).
Ao focar no campo **CVV**, o app anima uma rotação no eixo Y (via
`ObjectAnimator` + `AnimatorSet`) alternando para o verso do cartão, onde o
CVV é exibido sobre a faixa de assinatura. Ao focar em qualquer outro campo
do formulário, o cartão gira de volta para a frente.

### Desafio 2 — Identificação dinâmica da bandeira

Conforme os primeiros dígitos do número do cartão são inseridos, o app
identifica a bandeira e atualiza o logotipo (texto) e a cor de fundo do
cartão instantaneamente:

| Bandeira   | Prefixo identificado         |
|------------|-------------------------------|
| Visa       | Começa com `4`                |
| Mastercard | `51`–`55` ou `2221`–`2720`    |
| Outra      | Qualquer outro prefixo        |

## Arquitetura

- `TextWatcher` em cada campo do formulário (`etCardNumber`, `etHolder`,
  `etValidity`, `etCvv`) monitora as mudanças de texto, aplica as máscaras e
  atualiza a UI do cartão em tempo real.
- `setOnFocusChangeListener` nos campos controla a animação de flip do
  cartão (verso ao focar no CVV, frente ao focar nos demais campos).
- A validação final ocorre apenas no clique do botão "Confirmar", evitando
  mensagens de erro prematuras enquanto o usuário ainda está digitando.

## Estrutura do projeto

```
app/src/main/java/com/example/meucartaodecredito/
└── MainActivity.kt

app/src/main/res/layout/
└── activity_main.xml
```

## Como executar

1. Clone este repositório.
2. Abra o projeto no Android Studio.
3. Aguarde a sincronização do Gradle.
4. Execute em um emulador ou dispositivo físico (mínimo recomendado: API 24).

## Tecnologias utilizadas

- Kotlin
- ConstraintLayout
- CardView
- ViewFlipper
- ObjectAnimator / AnimatorSet

## Autor

Paulo — Curso Superior de Tecnologia em Análise e Desenvolvimento de
Sistemas, IFTM Campus Patrocínio.
