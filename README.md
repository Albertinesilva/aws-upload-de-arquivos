## ☁️📤 AWS S3 Upload de Arquivos com Spring Boot

<p align="center">
<em>Projeto backend desenvolvido para implementar upload de arquivos para a Amazon S3, utilizando Spring Boot, configuração de IAM, políticas de bucket e CORS, seguindo boas práticas de segurança e organização de credenciais.
Este projeto demonstra a integração entre uma API REST e a infraestrutura AWS para armazenamento escalável de arquivos.</em>
</p>

---

### 📌 Introdução

Em aplicações modernas, é comum a necessidade de armazenar arquivos como:

- Imagens de produtos  
- Fotos de usuários  
- Documentos  
- Arquivos estáticos  

Em vez de armazenar esses arquivos localmente no servidor, utilizamos o **Amazon S3 (Simple Storage Service)**, que oferece:

- Alta disponibilidade  
- Escalabilidade automática  
- Segurança configurável  
- Acesso público ou privado controlado  

Este projeto implementa essa integração utilizando Spring Boot e AWS.

---

### 🎯 Objetivo

O objetivo principal deste projeto foi:

- Implementar upload de arquivos para AWS S3  
- Configurar corretamente permissões via IAM  
- Aplicar política de bucket para acesso público controlado  
- Configurar CORS para acesso via navegador  
- Utilizar variáveis de ambiente para proteger credenciais  
- Aplicar boas práticas de segurança e organização  

---

## ☁️ Configuração na AWS

### 🔐 1. Política do Bucket (Bucket Policy)

Para permitir acesso público apenas de leitura aos arquivos armazenados, foi configurada a seguinte política no bucket:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "PublicReadGetObject",
            "Effect": "Allow",
            "Principal": "*",
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::bds-dscatalog1/*"
        }
    ]
}
```

Essa política permite que qualquer usuário acesse os arquivos via URL pública, mas não permite upload ou exclusão.

---
### 🌍 2. Configuração de CORS no Bucket

Para permitir requisições via navegador, foi configurado o seguinte CORS no bucket:

```json
[
    {
        "AllowedHeaders": ["*"],
        "AllowedMethods": ["GET", "HEAD"],
        "AllowedOrigins": ["*"],
        "ExposeHeaders": [],
        "MaxAgeSeconds": 3000
    }
]
```
Isso permite requisições GET e HEAD de qualquer origem.

---
### 🔑 Configuração IAM

Foi criado um usuário IAM específico para a aplicação, com:

- Permissão de acesso ao Amazon S3
- Access Key ID
- Secret Access Key

As credenciais não ficam expostas no código-fonte.
São injetadas via variáveis de ambiente.

---
### ⚙️ Configuração no Spring Boot
#### 📄 application.properties

```properties
aws.access_key_id=${AWS_KEY:empty}
aws.secret_access_key=${AWS_SECRET:empty}
s3.bucket=${DSCATALOG_BUCKET_NAME:empty}
s3.region=${DSCATALOG_BUCKET_REGION:sa-east-1}

spring.servlet.multipart.max-file-size=${MAX_FILE_SIZE:10MB}
spring.servlet.multipart.max-request-size=${MAX_FILE_SIZE:10MB}
```

---
| Variável                | Descrição                            |
|-------------------------|---------------------------------------|
| `AWS_KEY`               | Access Key do IAM                    |
| `AWS_SECRET`            | Secret Key do IAM                    |
| `DSCATALOG_BUCKET_NAME` | Nome do bucket                       |
| `DSCATALOG_BUCKET_REGION` | Região do bucket                   |
| `MAX_FILE_SIZE`         | Tamanho máximo permitido para upload |

---
### 🏗️ Fluxo da Aplicação

1. Cliente envia arquivo para a API
2. Spring Boot recebe a requisição
3. Arquivo é enviado para o Amazon S3
4. A URL pública do arquivo é retornada

---
### 🧠 Boas Práticas Aplicadas

- Uso de variáveis de ambiente
- Separação de credenciais do código
- Política de bucket controlada
- Configuração correta de CORS
- Limitação de tamanho de upload
- Integração limpa entre backend e infraestrutura cloud

---
### 🛠️ Tecnologias Utilizadas

- Java
- Spring Boot
- Spring Web
- AWS SDK
- Amazon S3
- IAM
- Maven

---
### 🚀 Possíveis Evoluções

- Upload privado com Pre-Signed URL
- Exclusão de arquivos no S3
- Versionamento de objetos
- Deploy completo na AWS (EC2 ou Elastic Beanstalk)
- Integração com CloudFront

---
## 👨‍💻 Autor

**Albert Silva de Jesus**  
Desenvolvedor Backend Java | Spring Boot  

---

### 📎 Contato

[![LinkedIn](https://img.shields.io/badge/LinkedIn-%230077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/albert-backend-java-spring-boot/)
[![Gmail](https://img.shields.io/badge/Gmail-D14836?style=for-the-badge&logo=gmail&logoColor=white)](mailto:albertinesilva.17@gmail.com?subject=Contato%20sobre%20o%20projeto%20CAD-MOTOTAXISTA)
