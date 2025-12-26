-- Script d'initialisation de la base de données Al Baraka Digital

-- Créer la base de données si elle n'existe pas
CREATE DATABASE IF NOT EXISTS albaraka;
USE albaraka;

-- Ce script sera exécuté automatiquement au premier démarrage du conteneur MySQL
-- Les tables seront créées automatiquement par Hibernate avec spring.jpa.hibernate.ddl-auto=update

-- Vous pouvez ajouter ici des données de test si nécessaire après le premier démarrage

-- Exemple de données de test (à décommenter après la création des tables par Hibernate)
/*
-- Insérer un admin par défaut (mot de passe: admin123)
INSERT INTO users (email, password, full_name, role, active, created_at)
VALUES ('admin@albaraka.com', '$2a$10$XqXqXqXqXqXqXqXqXqXqXuE', 'Administrator', 'ADMIN', true, NOW());

-- Insérer un agent bancaire par défaut (mot de passe: agent123)
INSERT INTO users (email, password, full_name, role, active, created_at)
VALUES ('agent@albaraka.com', '$2a$10$YyYyYyYyYyYyYyYyYyYyYuE', 'Agent Bancaire', 'AGENT_BANCAIRE', true, NOW());

-- Insérer un client test (mot de passe: client123)
INSERT INTO users (email, password, full_name, role, active, created_at)
VALUES ('client@albaraka.com', '$2a$10$ZzZzZzZzZzZzZzZzZzZzZuE', 'Client Test', 'CLIENT', true, NOW());
*/