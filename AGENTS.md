# AGENTS.md — Diretrizes do Projeto

## 1. Contexto tecnológico

Este projeto utiliza:

- Java 21
- Spring Boot
- Maven
- Spring Data JPA
- Spring Security
- PostgreSQL
- Flyway
- JUnit 5
- Mockito
- AssertJ
- Spring Boot Test

Antes de implementar qualquer alteração, analise a estrutura existente e respeite os padrões já adotados no projeto.

## 2. Princípios de arquitetura e desenvolvimento

Toda implementação deve seguir boas práticas de engenharia de software:

- Aplicar os princípios SOLID.
- Priorizar código limpo, legível e de fácil manutenção.
- Manter responsabilidades bem definidas entre as camadas.
- Respeitar a separação entre Controller, Service, Repository, Model e DTO.
- Aplicar baixo acoplamento e alta coesão.
- Priorizar composição em vez de herança quando apropriado.
- Evitar duplicação de código.
- Utilizar injeção de dependências pelo construtor.
- Aplicar padrões de projeto quando realmente agregarem valor.
- Evitar complexidade desnecessária.

Considere os princípios de Clean Architecture quando forem adequados à estrutura do projeto, sem introduzir camadas ou abstrações desnecessárias.

## 3. Testes automatizados — obrigatórios

**Toda nova funcionalidade ou alteração em regras de negócio deve possuir testes automatizados.**

Utilize preferencialmente:

- JUnit 5 para estruturação e execução dos testes.
- Mockito para simular dependências externas.
- AssertJ para as verificações e afirmações.
- Spring Boot Test para testes de integração com o contexto do Spring.

### 3.1 Testes unitários

Os testes unitários devem:

- Isolar a classe que está sendo testada.
- Simular suas dependências utilizando Mockito.
- Não acessar banco de dados ou serviços externos.
- Não depender da ordem de execução.
- Ser determinísticos e independentes.
- Seguir o padrão Arrange, Act e Assert (Preparar, Executar e Verificar).

Utilize, quando apropriado:

- `@ExtendWith(MockitoExtension.class)`
- `@Mock`
- `@InjectMocks`
- `when()`
- `thenReturn()`
- `thenThrow()`
- `verify()`
- `assertThat()`

Não simule a própria classe que está sendo testada.

Evite utilizar mocks para DTOs, entidades e objetos de valor simples. Prefira instâncias reais desses objetos.

### 3.2 Cobertura mínima dos serviços

Para cada Service, considere cenários como:

1. Execução bem-sucedida.
2. Entrada inválida.
3. Regra de negócio violada.
4. Registro não encontrado.
5. Exceção lançada por uma dependência.
6. Valores nulos ou limites, quando aplicável.

Os testes devem validar tanto o resultado quanto as interações relevantes com as dependências.

### 3.3 Testes de Controllers

Utilize `@WebMvcTest` quando adequado.

Valide:

- Códigos HTTP.
- Estrutura das respostas.
- Validação dos parâmetros.
- Tratamento de exceções.
- Autenticação e autorização.
- Comportamento diante de requisições inválidas.

### 3.4 Testes de Repository

Utilize `@DataJpaTest` para testar operações de persistência.

Valide:

- Consultas personalizadas.
- Relacionamentos entre entidades.
- Persistência e recuperação de dados.
- Restrições e comportamentos específicos do banco de dados, quando necessário.

### 3.5 Testes de segurança

A segurança é uma parte crítica da aplicação.

Crie testes específicos para:

- Autenticação.
- Geração e validação de JWT.
- Expiração de tokens.
- Refresh tokens.
- Usuários sem autenticação.
- Usuários sem permissão.
- Acesso autorizado e não autorizado aos endpoints.

## 4. Refatoração de código

Antes de realizar qualquer refatoração:

1. Analise a implementação atual.
2. Identifique problemas de arquitetura e código.
3. Identifique possíveis violações dos princípios SOLID.
4. Verifique a existência de testes automatizados.
5. Crie ou atualize os testes antes de modificar o comportamento existente.
6. Refatore preservando o comportamento funcional.
7. Execute os testes após as alterações.

Priorize a identificação e correção de:

- Injeção de dependências por atributos (`@Autowired` em campos).
- Classes com responsabilidades excessivas.
- Métodos muito extensos.
- Duplicação de código.
- Alto acoplamento.
- Baixa coesão.
- Tratamento inadequado de exceções.
- Configurações duplicadas.
- Dependências desnecessárias.
- Ausência de testes unitários.

Não faça refatorações apenas por preferência estética. Explique o problema encontrado e o benefício esperado da alteração.

## 5. Boas práticas com Spring Boot

- Utilize injeção de dependências pelo construtor.
- Centralize a configuração do `PasswordEncoder` em um único Bean.
- Utilize `@Transactional` nas operações que exigem transação.
- Utilize DTOs para entrada e saída da API.
- Evite expor entidades JPA diretamente nos Controllers.
- Utilize Bean Validation para validar entradas.
- Centralize o tratamento de exceções.
- Mantenha as configurações de segurança organizadas.
- Armazene informações sensíveis em variáveis de ambiente.
- Nunca exponha senhas, tokens ou chaves secretas no código-fonte.

## 6. Maven e execução dos testes

Após implementar ou refatorar código, execute os comandos adequados.

Limpar e executar os testes:

`mvn clean test`

Executar a verificação completa do projeto:

`mvn verify`

Não declare que os testes passaram sem executá-los.

Se algum teste falhar:

1. Analise a causa.
2. Corrija o problema.
3. Execute novamente os testes.
4. Informe claramente qualquer falha que permaneça.

## 7. Fluxo de trabalho do GitHub Copilot

Ao receber uma solicitação de implementação ou refatoração:

1. Analise a estrutura do projeto.
2. Identifique as classes envolvidas.
3. Explique brevemente o problema identificado.
4. Apresente uma proposta de solução.
5. Verifique os impactos arquiteturais.
6. Implemente as alterações necessárias.
7. Crie ou atualize os testes unitários com JUnit 5 e Mockito.
8. Execute os testes.
9. Apresente um resumo das alterações realizadas.

Para alterações maiores, divida o trabalho em etapas pequenas e verificáveis.

Não modifique arquivos sem necessidade e não remova funcionalidades existentes sem autorização.

## 8. Definição de pronto (Definition of Done)

Uma implementação ou refatoração somente será considerada concluída quando:

- [x] O código estiver organizado e legível.
- [x] Os princípios SOLID forem respeitados.
- [x] A arquitetura existente for preservada ou melhorada.
- [x] As regras de negócio estiverem cobertas por testes unitários.
- [x] Os testes utilizarem JUnit 5 e Mockito quando apropriado.
- [x] Os cenários de sucesso e falha estiverem contemplados.
- [x] Os testes automatizados forem executados.
- [x] Não houver regressões conhecidas.
- [x] As alterações estiverem documentadas quando necessário.

**Regra principal:** priorize sempre qualidade, segurança, testabilidade e manutenibilidade. Não entregue apenas código que funciona; entregue código que possa ser compreendido, testado, evoluído e mantido.
