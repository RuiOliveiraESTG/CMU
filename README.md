# CMU - Aplicação de Avaliação de Estabelecimentos

##  Introdução
Este ficheiro funcionará como relatório do trabalho realizado na época de recurso da unidade curricular de Computação Móvel e Ubíqua (CMU). O projeto tem como objetivo o melhoramento dos conhecimentos teóricos e práticos do desenvolvimento de aplicações móveis em Kotlin, com integração de APIs externas, base de dados local e online, e arquitetura MVVM com LiveData.

---

##  Visão do Trabalho
A aplicação permite que os utilizadores:
- Avaliem cafes;
- Consultem detalhes de cada cafe (nome, morada, contacto telefónico, mapa);
- Acedam ao histórico de avaliações realizadas por eles ou por outros utilizadores;
- Visualizem uma leaderboard dos locais melhor avaliados;
- Utilizem a aplicação em modo online e offline (Room e Firebase);

---

##  Organização do Projeto

### **Data**
Pasta com ficheiros relativos aos dados:
- **local** → Implementação do Room (DAO, entidades e base de dados).
- **remote** → Integração com Firebase e Google Places API.
- **repository** → Lógica de acesso e sincronização entre local/online.
- **model** → data class utilizada para suporte da leaderboard e historico

### **Work**
Pasta com os workers → Sincronizaçao do room com o firebase automatica e notificações

### **ViewModel**
Controlo da lógica entre os dados e a interface
- Integração com **LiveData** para atualização reativa da UI.

### **UI (Screens)**
Todo a interface com o utilizador e respetivos ficheiros de navegação
- **navigation** → Gestão da navegação entre ecrãs com um sistema de rotas e sidebar 
- **screens** → Resto da UI

---

##  Bibliotecas Utilizadas
- **Jetpack Compose** → Criação da UI.
- **Room** → Armazenamento local persistente.
- **Firebase Firestore** → Sincronização e armazenamento online.
- **Firebase Auth** → Registo e autenticação de utilizadores.
- **Retrofit** → Acesso à Google Places API.
- **Google Places API** → Pesquisa e detalhes de estabelecimentos.
- **Google Maps SDK** → Visualização de mapa e geolocalização.
- **Coil** → Carregamento de imagens na UI.

---



## Conclusão
A aplicação cumpre o objetivo inicial, apesar da falta de alguns componentes implementados, como por exemplo:
-Multi linguas
-Interface grafica por melhorar
