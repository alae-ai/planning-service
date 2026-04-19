# Packages expliqués (microservice Planning / Creneaux)

Ce document explique la structure des dossiers du projet et le rôle de chaque partie.

## 🏗️ Project Structure

Le code Java principal est dans :

- `src/main/java/com/planning/microplanning`

### 📁 model

- Contient les classes “métier” qui représentent les données.
- Exemple : `Creneau` représente un créneau (une ligne en base de données).
- Un `Creneau` contient notamment :
  - `medecinId`
  - `date`
  - `heureDebut`, `heureFin`
  - `disponible` (vrai/faux)

### 📁 repository

- Contient l’accès à la base de données.
- Ici on utilise Spring Data JPA.
- Le repository permet de :
  - lire les créneaux
  - filtrer (par médecin, par disponibilité)
  - faire des mises à jour simples (bloquer/libérer)

### 📁 service

- Contient la logique métier (les règles).
- Le controller ne fait pas de calcul : il appelle le service.
- Le service s’occupe par exemple de :
  - vérifier les paramètres
  - corriger les données incohérentes (stabilisation)
  - appliquer les règles (expiré = non disponible)
  - bloquer/libérer de façon sûre
  - calculer les statistiques

### 📁 service/dto

- Contient des objets simples utilisés pour retourner des résultats (souvent des réponses d’API).
- Exemple : `MedecinStatsDTO`
  - `medecinId`
  - `nbCreneauxReserves`

### 📁 web/controller

- Contient les contrôleurs REST (les endpoints HTTP).
- C’est le point d’entrée de l’application.
- Chaque méthode correspond à une URL (ex : `/creneaux`, `/creneaux/{id}`, etc.).

### 📁 web/error

- Contient la gestion des erreurs.
- Il y a :
  - des exceptions (ex : “creneau introuvable”, “conflit d’état”)
  - un handler global qui transforme les exceptions en réponses JSON claires

### 📁 web/dto

- Dossier présent dans le projet.
- Dans la version actuelle, il est vide.
- Il peut servir plus tard à mettre des DTOs liés aux réponses/entrées API.

### 📁 dao

- Dossier présent dans le projet.
- Dans la version actuelle, il est vide.
- Souvent, “DAO” sert à définir des accès base de données (alternative aux repositories).

### 📁 integration

- Dossier présent dans le projet.
- Dans la version actuelle, il est vide.
- Il peut servir à isoler le code d’intégration avec d’autres services.

## 🧠 Layers Explanation (explication simple)

- **Controller**
  - Reçoit la requête HTTP de l’utilisateur.
  - Appelle le service.
  - Retourne la réponse HTTP.

- **Service**
  - Applique les règles métier.
  - Fait les validations.
  - Prépare les données de sortie.

- **Repository**
  - Parle avec la base de données.
  - Sauvegarde et récupère les créneaux.

- **Entity / Model**
  - Représente les données stockées en base.
  - Exemple : un `Creneau` représente un enregistrement de créneau.

- **DTO**
  - Sert à transporter des données de façon simple.
  - Exemple : une réponse de statistiques n’a pas besoin de tout le créneau, juste un comptage.

## 🔌 Where are the APIs?

- Les APIs sont dans `web/controller`.
- Le controller principal est `CreneauController`.
- Les URLs commencent par `/creneaux` pour tout ce qui concerne les créneaux.
- Un endpoint de test existe aussi à la racine : `GET /`.

## 📄 Configuration (simple)

Les paramètres sont dans :

- `src/main/resources/application.properties`

On y trouve notamment :

- Le port du service.
- L’URL de la base de données PostgreSQL.
- Une URL de dépendance externe (médecins) et un mode de simulation de panne.

