# 📱 Micro Rede Social ( FUMOBET )

## 📌 Sobre o Projeto

Este projeto consiste no desenvolvimento de um aplicativo Android de uma micro rede social sobre apostas esportivas e dicas, permitindo que usuários compartilhem fotos, textos e localização.

O aplicativo foi desenvolvido como parte da disciplina **Dispositivos Móveis 2**, do curso de Análise e Desenvolvimento de Sistemas do IFSP – Campus Araraquara.

---

## 🚀 Funcionalidades

### 🔐 Autenticação

* Cadastro de usuário com nome, e-mail e senha
* Login utilizando Firebase Authentication
* Persistência de sessão (usuário permanece logado ao reabrir o app)

### 📝 Postagens

* Criação de post contendo:

  * Imagem (selecionada da galeria)
  * Texto descritivo
  * Localização (cidade atual via GPS - opcional)
* Armazenamento das postagens no Firebase Firestore

### 📰 Feed

* Exibição das postagens no feed
* Paginação (carregamento gradual dos posts)
* Busca de postagens por cidade

### 👤 Perfil

* Edição dos dados do usuário:

  * Nome
  * Senha
  * Foto de perfil

### 📍 Localização

* Captura automática da localização do usuário
* Conversão das coordenadas em nome da cidade (geocodificação)

---

## 🛠️ Tecnologias Utilizadas

* Kotlin
* Android Studio
* Firebase Authentication
* Firebase Firestore
* API 33 (Android 13)

---

## 📸 Demonstração

🎥 Vídeo curto demonstrando o funcionamento do aplicativo:



https://github.com/user-attachments/assets/ef3b0f8d-60a3-4bd3-a460-74d76736bfa0

---

## 🎥 Vídeo de Apresentação

Neste vídeo é apresentada a explicação da implementação do projeto, detalhando as principais partes do código e sua lógica de funcionamento.


[![YouTube](https://img.shields.io/badge/YouTube-Assista%20ao%20Vídeo-red?logo=youtube)](https://youtu.be/B7sR9GHAhrs)



---

## 📂 Como Executar o Projeto

### ⚙️ Pré-requisitos

Antes de iniciar, você precisa ter instalado:

* Android Studio
* JDK 17 ou superior
* Dispositivo Android ou emulador configurado

---

### 🔽 1. Clonar o repositório

```bash
https://github.com/ygorsoares777/MicroRedeSocial-DMO2.git
```

---

### 📱 2. Abrir no Android Studio

* Abra o Android Studio
* Clique em **"Open"**
* Selecione a pasta do projeto clonado
* Aguarde o Gradle sincronizar

---

### 🔥 3. Configurar o Firebase (OBRIGATÓRIO)

Este projeto utiliza Firebase para autenticação e banco de dados.

1. Acesse o Firebase Console
2. Crie um novo projeto
3. Adicione um app Android
4. Baixe o arquivo `google-services.json`
5. Coloque o arquivo dentro da pasta:

```
app/google-services.json
```

6. No Firebase, ative:

   * Authentication (método Email/Senha)
   * Firestore Database

---

### ▶️ 4. Executar o aplicativo

* Conecte um celular **ou** inicie um emulador
* Clique em **Run ▶️** no Android Studio

---

### ✅ 5. Testar funcionalidades

Após executar, você pode testar:

* Cadastro e login de usuário
* Criação de post com imagem
* Visualização do feed
* Busca por cidade
* Edição de perfil

---


---

## 👨‍💻 Autor

**Ygor Soares da Silva**
