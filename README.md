# SGE - Sistema de Gestão Escolar

Projeto base para Java 21 + Spring Boot 3.5.6 + JSF/Jakarta Faces + PrimeFaces + MySQL, empacotado como WAR.

## Importar no Eclipse

1. Extraia o ZIP.
2. Eclipse -> File -> Import -> Maven -> Existing Maven Projects.
3. Selecione a pasta `sge`.
4. Finish.
5. Aguarde o Maven baixar as dependências.

## Configurar MySQL

Crie o banco:

```sql
CREATE DATABASE sge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Depois altere `src/main/resources/application.properties`:

```properties
spring.datasource.username=root
spring.datasource.password=SUA_SENHA
```

## Gerar WAR

```bash
mvn clean package
```

O arquivo será:

`target/sge.war`

## Deploy no WildFly

Copie `target/sge.war` para a pasta `standalone/deployments` do WildFly.

A aplicação ficará disponível em:

`http://localhost:8080/sge/login.xhtml`

## Observação

Esta é a base inicial do projeto. O login e os CRUDs ainda serão implementados nas próximas etapas.
