# Documentation du microservice Planning (Creneaux)

## 🧩 Overview

Ce microservice gère les **créneaux de consultation** (des “slots” horaires) pour des médecins.

Son objectif est simple :

- Enregistrer des créneaux en base de données (date + heure).
- Dire si un créneau est **disponible** ou **réservé**.
- Permettre de **bloquer** (réserver) et **libérer** un créneau.
- Donner une statistique : **combien de créneaux réservés par médecin pour un mois donné**.

Ce service fait partie d’un système de planification (médecins, rendez-vous, statistiques). Ici, on se concentre sur la partie **créneaux**.

## ⚙️ Main Features

### 1) Consulter les créneaux

Ce que ça fait :

- Permet de récupérer la liste de tous les créneaux.
- Permet de récupérer un créneau précis par son `id`.

Pourquoi ça existe :

- Pour afficher un planning.
- Pour vérifier l’état d’un créneau (disponible ou non).

Comment l’utiliser (API) :

- Appeler l’endpoint “liste”.
- Ou appeler l’endpoint “détail” avec un `id`.

### 2) Créer des créneaux

Ce que ça fait :

- Permet d’ajouter un nouveau créneau en base.

Pourquoi ça existe :

- Pour construire le planning d’un médecin.

Comment l’utiliser (API) :

- Envoyer les informations du créneau (médecin, date, heures, disponibilité).

Remarque importante :

- Le service corrige certaines données si elles sont incomplètes (voir “Business Logic”).

### 3) Gérer la disponibilité (disponible / réservé)

Définition simple :

- `disponible = true` : le créneau est libre.
- `disponible = false` : le créneau est réservé (bloqué).

Ce que ça fait :

- Lister uniquement les créneaux disponibles.
- Bloquer un créneau (le passer en réservé).
- Libérer un créneau (le repasser en disponible).

Pourquoi ça existe :

- Pour éviter de proposer un créneau déjà pris.
- Pour gérer la réservation d’un créneau.

Comment l’utiliser (API) :

- Appeler “disponibles” pour voir ce qui est libre.
- Appeler “bloquer” pour réserver.
- Appeler “libérer” pour annuler la réservation.

### 4) Lier les créneaux à un médecin

Ce que ça fait :

- Filtrer les créneaux d’un médecin grâce à `medecinId`.

Pourquoi ça existe :

- Chaque médecin a son propre planning.

Comment l’utiliser (API) :

- Appeler l’endpoint “créneaux par médecin” avec `medecinId`.

### 5) Statistiques : nombre de créneaux réservés par médecin (par mois)

Ce que ça fait :

- Calcule, pour un **mois donné**, le **nombre de créneaux réservés** par médecin.
- Un créneau réservé est défini par : `disponible = false`.
- Le filtre sur le temps se fait avec `Creneau.date` (année + mois).
- Le résultat est regroupé par `medecinId`.

Pourquoi ça existe :

- Pour faire un reporting simple.
- Pour voir quels médecins ont le plus de créneaux réservés sur une période.

Comment l’utiliser (API) :

- Appeler l’endpoint stats avec `year` et `month`.

Exemple de réponse (format) :

- `medecinId` : identifiant du médecin.
- `nbCreneauxReserves` : nombre de créneaux réservés pour ce médecin sur le mois.

## 🔌 APIs (Endpoints)

Où sont les APIs dans le projet :

- Les endpoints sont définis dans la couche **Controller**.
- Fichier principal : `web/controller/CreneauController`.

### Endpoint de santé (root)

- `GET /`
  - Vérifie que l’application tourne.
  - Retourne un simple message texte.

### Endpoints Creneaux (base URL : `/creneaux`)

- `GET /creneaux`
  - Récupère tous les créneaux (triés par date puis heure de début).

- `GET /creneaux/{id}`
  - Récupère un créneau par son identifiant.
  - Si l’`id` n’existe pas, l’API renvoie une erreur “not found”.

- `GET /creneaux/disponibles`
  - Récupère uniquement les créneaux **disponibles**.
  - Les créneaux expirés (dans le passé) ne doivent pas être proposés comme disponibles.

- `GET /creneaux/medecin/{medecinId}`
  - Récupère les créneaux d’un médecin.

- `POST /creneaux`
  - Crée un nouveau créneau.
  - Renvoie le créneau créé et une URL de ressource.

- `PUT /creneaux/{id}/bloquer`
  - Réserve (bloque) un créneau.
  - Le créneau devient `disponible = false`.
  - Si le créneau est déjà réservé, l’API renvoie un conflit.

- `PUT /creneaux/{id}/liberer`
  - Libère un créneau réservé.
  - Le créneau devient `disponible = true`.
  - Si le créneau est déjà disponible, l’API renvoie un conflit.

### Endpoint Statistiques

- `GET /creneaux/stats/medecins?year=YYYY&month=MM`
  - Retourne le nombre de créneaux **réservés** par médecin pour un mois.
  - `year` doit être >= 1.
  - `month` doit être entre 1 et 12.

## 🧠 Business Logic

Où se trouve la logique :

- La logique métier est dans la couche **Service**.
- Fichier principal : `service/CreneauServiceImpl`.

Types de logique présents :

- Validation simple des entrées (exemples : `id` non null, mois valide, etc.).
- Tri et filtrage (exemple : créneaux disponibles, créneaux par médecin).
- Règles de cohérence sur les données (auto-correction “self-healing”).
- Gestion d’état (bloquer / libérer) de façon sûre.
- Calcul de statistiques (groupement et comptage).

### Auto-correction (stabilisation) des créneaux

Le service peut corriger certains champs pour éviter des données incohérentes :

- Si `date` est manquante, il met la date du jour.
- Si les heures sont manquantes, il met des valeurs par défaut.
- Si l’heure de fin n’est pas après l’heure de début, il corrige l’heure de fin.
- Si le créneau est dans le passé (déjà terminé), il ne doit pas rester disponible.
- Si `medecinId` est manquant ou invalide, le service peut le rendre “non disponible” par sécurité.

### Réservation / libération (gestion d’état)

Pour éviter les erreurs quand plusieurs personnes appellent l’API en même temps :

- Le service utilise une mise à jour “atomique” en base pour bloquer/libérer.
- Si quelqu’un a déjà réservé entre-temps, le service renvoie un message clair.

### Gestion des erreurs (réponses API)

Les erreurs sont gérées dans `web/error/ApiExceptionHandler`.

Exemples d’erreurs possibles :

- 400 : paramètres manquants ou incorrects (ex : `month=13`).
- 404 : créneau ou médecin introuvable (selon le cas).
- 409 : conflit d’état (ex : bloquer un créneau déjà bloqué).
- 503 : service externe indisponible (ex : dépendance “médecin-service” simulée).
- 500 : erreur inattendue (cas rare, logué côté serveur).

