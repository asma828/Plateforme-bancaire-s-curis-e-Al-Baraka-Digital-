# 🏦 Al Baraka Digital - Plateforme Bancaire Sécurisée

Plateforme bancaire digitale sécurisée avec gestion des opérations, validation par agents et contrôle administratif.

## 📋 Table des matières

- [Fonctionnalités](#fonctionnalités)
- [Technologies utilisées](#technologies-utilisées)
- [Architecture](#architecture)
- [Installation](#installation)
- [Configuration](#configuration)
- [Déploiement avec Docker](#déploiement-avec-docker)
- [Endpoints API](#endpoints-api)
- [Tests](#tests)

## ✨ Fonctionnalités

### Pour les Clients
-  Inscription et connexion avec JWT
-  Création d'opérations (Dépôt, Retrait, Virement)
-  Validation automatique pour montants ≤ 10 000 DH
-  Upload de justificatifs pour montants > 10 000 DH
-  Consultation de l'historique des opérations

### Pour les Agents Bancaires
-  Consultation des opérations en attente
-  Approbation/Rejet des opérations
-  Consultation des documents justificatifs

### Pour les Administrateurs
-  Gestion complète des utilisateurs
-  Création de comptes Clients/Agents/Admins
-  Activation/Désactivation des comptes

## 🛠 Technologies utilisées

- **Backend**: Spring Boot 3.2.0
- **Sécurité**: Spring Security 6 + JWT
- **Base de données**: MySQL 8.0
- **ORM**: Spring Data JPA / Hibernate
- **Build**: Maven
- **Conteneurisation**: Docker & Docker Compose
- **Documentation**: OpenAPI/Swagger (optionnel)

## 🏗 Architecture

```
com.albaraka.digital
├── config/          # Configuration Spring Security
├── controller/      # Controllers REST
├── dto/             # Data Transfer Objects
├── exception/       # Gestion des exceptions
├── model/           # Entités JPA
├── repository/      # Repositories Spring Data
├── security/        # JWT & UserDetailsService
└── service/         # Logique métier
```

## 📦 Installation

### Prérequis
- Java 17+
- Maven 3.6+
- Docker & Docker Compose (pour le déploiement)
- MySQL 8.0 (si exécution locale)

### Installation locale

1. **Cloner le repository**
```bash
git clone https://github.com/asma828/Plateforme-bancaire-s-curis-e-Al-Baraka-Digital-.git
cd albaraka-digital
```

2. **Configurer la base de données MySQL**
```sql
CREATE DATABASE albaraka;
```

3. **Configurer application.properties**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/albaraka_db
spring.datasource.username=root
spring.datasource.password=votre_mot_de_passe
jwt.secret=votre-secret-jwt-super-long
```

4. **Compiler et démarrer l'application**
```bash
mvn clean install
mvn spring-boot:run
```

L'application sera accessible sur `http://localhost:8080`

## 🐳 Déploiement avec Docker

### Option 1: Docker Compose (Recommandé)

1. **Créer le fichier .env**
```bash
cp .env.example .env
# Éditer .env avec vos valeurs
```

2. **Démarrer les services**
```bash
docker-compose up -d
```

3. **Vérifier les logs**
```bash
docker-compose logs -f app
```

4. **Arrêter les services**
```bash
docker-compose down
```

### Option 2: Docker uniquement

1. **Build l'image**
```bash
docker build -t albaraka-digital .
```

2. **Lancer le conteneur**
```bash
docker run -d \
  -p 8080:8080 \
  -e DB_URL=jdbc:mysql://host.docker.internal:3306/albaraka \
  -e DB_USER=root \
  -e DB_PASSWORD=password \
  -e JWT_SECRET=your-secret-key \
  --name albaraka-app \
  albaraka-digital
```

## 🔐 Sécurité

- **Authentification**: JWT stateless avec durée de validité 24h
- **Autorisation**: Contrôle d'accès basé sur les rôles (RBAC)
- **Mots de passe**: Hashage avec BCrypt
- **Upload**: Validation des types et tailles de fichiers (max 5MB)
- **CORS**: Configuration sécurisée

## 📊 Règles Métier

### Validation automatique des opérations

| Montant | Action |
|---------|--------|
| ≤ 10 000 DH | Validation automatique + mise à jour solde |
| > 10 000 DH | Statut PENDING + document requis |

### Types d'opérations

- **DEPOSIT**: Dépôt d'argent
- **WITHDRAWAL**: Retrait d'argent
- **TRANSFER**: Virement vers un autre compte

### Statuts d'opération

- **PENDING**: En attente de validation
- **APPROVED**: Approuvée par agent
- **REJECTED**: Rejetée par agent
- **COMPLETED**: Complétée automatiquement


## 📝 Variables d'environnement

| Variable | Description | Valeur par défaut |
|----------|-------------|-------------------|
| `DB_URL` | URL de la base de données | jdbc:mysql://localhost:3306/albaraka_db |
| `DB_USER` | Utilisateur MySQL | root |
| `DB_PASSWORD` | Mot de passe MySQL | root |
| `JWT_SECRET` | Clé secrète JWT | (doit être changée) |
| `JWT_EXPIRATION` | Durée validité JWT (ms) | 86400000 (24h) |

## 🚀 CI/CD (À venir)

- GitHub Actions / GitLab CI
- Tests automatisés
- Déploiement automatique
- Analyse de code (SonarQube)


---

