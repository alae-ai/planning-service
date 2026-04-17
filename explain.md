# Planning Service (Microservice) - Gestion des Creneaux

## 1) Presentation du projet

Ce microservice Spring Boot (planning-service) fait partie d'un systeme de prise de rendez-vous medical en architecture microservices.  
Son role est de gerer les **creneaux horaires** (time slots) des medecins.

Un **Creneau** represente une plage horaire (date + heure debut/fin) associee a un medecin, avec un etat de disponibilite.

Remarque: pour un TP universitaire, le stockage est volontairement simple: une **liste en memoire** (pas de base de donnees).

## 2) Architecture (Model / DAO / Controller)

L'architecture suit un schema "TP CRUD" classique:

- **Model (modele)**: objet metier `Creneau`
- **DAO**: couche d'acces aux donnees (ici une liste statique en memoire)
- **Controller**: expose l'API REST (endpoints HTTP) et applique les regles metier simples

### Diagramme (flux)

```
Client (Postman/Front)
        |
        v
  CreneauController  (REST)
        |
        v
     CreneauDao      (interface)
        |
        v
   CreneauDaoImpl    (Repository)
        |
        v
Static List<Creneau> (Fake DB)
```

## 3) Description des classes

### a) Modele

**Classe:** `com.planning.microplanning.model.Creneau`

Champs:
- `id` (Long): identifiant unique du creneau
- `medecinId` (Long): identifiant du medecin proprietaire du creneau
- `date` (LocalDate): date du creneau
- `heureDebut` (LocalTime): heure de debut
- `heureFin` (LocalTime): heure de fin
- `disponible` (boolean): `true` si le creneau est libre, `false` s'il est bloque

La classe contient: constructeurs, getters/setters, `toString()`.

### b) DAO (In-Memory)

**Interface:** `com.planning.microplanning.dao.CreneauDao`

Methodes:
- `findAll()` : retourner tous les creneaux
- `findById(id)` : retourner un creneau par son id
- `save(creneau)` : creer/mettre a jour un creneau
- `findByMedecinId(medecinId)` : retourner les creneaux d'un medecin
- `findAvailable()` : retourner les creneaux disponibles

**Implementation:** `com.planning.microplanning.dao.CreneauDaoImpl` (`@Repository`)

Caracteristiques:
- Utilise une **liste statique** comme "base de donnees" fictive.
- Genere les ids avec un compteur (`AtomicLong`).
- Initialise des donnees de test pour **au moins 2 medecins** (ex: `medecinId=1` et `medecinId=2`).

### c) Controller REST

**Classe:** `com.planning.microplanning.web.controller.CreneauController` (`@RestController`)

Roles:
- Expose les endpoints REST.
- Appelle la couche DAO.
- Applique les regles metier "bloquer/liberer".

### d) Gestion d'erreurs (reponses propres)

Pour eviter des erreurs 500 generiques, une gestion simple est ajoutee:
- `com.planning.microplanning.web.error.CreneauNotFoundException` -> HTTP 404
- `com.planning.microplanning.web.error.CreneauStateException` -> HTTP 409
- `com.planning.microplanning.web.error.ApiExceptionHandler` (`@RestControllerAdvice`) formate une reponse JSON d'erreur.

## 4) Endpoints REST (API)

Base URL (par defaut dans ce projet): `http://localhost:9090`

### 1. GET /creneaux
Retourne la liste de tous les creneaux.

- Methode: `GET`
- URL: `/creneaux`
- Reponse: `200 OK` + JSON `List<Creneau>`

### 2. GET /creneaux/{id}
Retourne un creneau par son id.

- Methode: `GET`
- URL: `/creneaux/1`
- Reponse:
  - `200 OK` + JSON `Creneau`
  - `404 Not Found` si l'id n'existe pas

### 3. GET /creneaux/disponibles
Retourne uniquement les creneaux disponibles.

- Methode: `GET`
- URL: `/creneaux/disponibles`
- Reponse: `200 OK` + JSON `List<Creneau>`

### 4. GET /creneaux/medecin/{medecinId}
Retourne les creneaux d'un medecin donne.

- Methode: `GET`
- URL: `/creneaux/medecin/1`
- Reponse: `200 OK` + JSON `List<Creneau>`

### 5. POST /creneaux
Cree un nouveau creneau.

- Methode: `POST`
- URL: `/creneaux`
- Body: JSON `Creneau` (sans id)
- Reponse:
  - `201 Created` + creneau cree
  - Header `Location: /creneaux/{id}`

Remarque: dans cette version TP, l'id fourni (s'il existe) est ignore pour forcer une creation.

### 6. PUT /creneaux/{id}/bloquer
Bloque un creneau (le rend indisponible).

- Methode: `PUT`
- URL: `/creneaux/1/bloquer`
- Reponse:
  - `200 OK` + creneau modifie
  - `404 Not Found` si l'id n'existe pas
  - `409 Conflict` si le creneau est deja bloque

### 7. PUT /creneaux/{id}/liberer
Libere un creneau (le rend disponible).

- Methode: `PUT`
- URL: `/creneaux/1/liberer`
- Reponse:
  - `200 OK` + creneau modifie
  - `404 Not Found` si l'id n'existe pas
  - `409 Conflict` si le creneau est deja libre

## 5) Regles metier

1. **Interdiction de bloquer un creneau deja bloque**
   - Si `disponible == false`, alors `PUT /bloquer` renvoie `409 Conflict`.
2. **Interdiction de liberer un creneau deja libre**
   - Si `disponible == true`, alors `PUT /liberer` renvoie `409 Conflict`.

## 6) Exemples de requetes (style Postman)

Les exemples ci-dessous supposent que l'application tourne sur le port `9090`.

### a) Recuperer tous les creneaux
```
GET http://localhost:9090/creneaux
```

### b) Recuperer un creneau par id
```
GET http://localhost:9090/creneaux/1
```

### c) Recuperer les creneaux disponibles
```
GET http://localhost:9090/creneaux/disponibles
```

### d) Recuperer les creneaux d'un medecin
```
GET http://localhost:9090/creneaux/medecin/1
```

### e) Creer un creneau
```
POST http://localhost:9090/creneaux
Content-Type: application/json

{
  "medecinId": 1,
  "date": "2026-04-20",
  "heureDebut": "08:00:00",
  "heureFin": "08:30:00",
  "disponible": true
}
```

### f) Bloquer un creneau
```
PUT http://localhost:9090/creneaux/1/bloquer
```

### g) Liberer un creneau
```
PUT http://localhost:9090/creneaux/1/liberer
```

## 7) Limites (TP)

- Les donnees sont en memoire: au redemarrage, la liste est reinitialisee.
- Aucune authentification/autorisation n'est geree ici (ce serait traite dans un autre microservice ou via une gateway).
- Pas de base de donnees; l'objectif est de pratiquer l'architecture REST simple (Model/DAO/Controller).

