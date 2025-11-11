# Elevator Simulator

Simulation d'ascenseurs dans une tour d'habitation (projet Java/Maven).

Le but du projet est de :

- modéliser un immeuble, ses résidents et leurs habitudes de déplacement ;
- simuler différents comportements d'ascenseurs ;
- mesurer des métriques (temps d'attente, temps de trajet, énergie, occupation, etc.) ;
- comparer plusieurs stratégies et configurations à l'aide de scénarios reproductibles.

Ce dépôt contient :

- un **moteur de simulation générique** ;
- des **stratégies d'ascenseurs** extensibles ;
- un **chargement par fichiers de configuration** ;
- des **rapports JSON détaillés** (global, par ascenseur, par résident).

---

## 1. Prérequis

- Java 17+
- Maven 3+
  
---

## 2. Construction

À la racine du projet :

```bash
mvn clean package
```

Pour lancer les tests :

```bash
mvn test
```

---

## 3. Exécution

Exécution standard après build :

```bash
java -jar target/elevator-simulator-1.0-SNAPSHOT.jar
```

Par défaut :

* Config utilisée : `config/demo-config.json`
* Stratégie : `nearest`
* Rapport global : `target/reports/demo-report.json`

### Arguments disponibles

Les arguments se passent à `App` :

* `--config=...`  
  Chemin (dans le classpath) vers un fichier JSON de configuration.  
  Exemple : `--config=config/demo-config.json`

* `--strategy=fcfs|nearest`  
  Choix de la stratégie de contrôle des ascenseurs :
  * `fcfs` : First-Come, First-Served
  * `nearest` : requête la plus proche (par défaut)

* `--report=...`  
  Chemin de base du rapport JSON global.

### Exemples

```bash
# Démo par défaut : nearest + demo-config
java -jar target/elevator-simulator-1.0-SNAPSHOT.jar

# Même config avec stratégie FCFS
java -jar target/elevator-simulator-1.0-SNAPSHOT.jar --strategy=fcfs

# Config personnalisée + rapport personnalisé
java -jar target/elevator-simulator-1.0-SNAPSHOT.jar \
  --config=config/demo-config.json \
  --strategy=nearest \
  --report=target/reports/resultats-nearest.json
```

---

## 4. Structure du projet

### Packages principaux

#### `fr.esipe.elevatorsim`

* **`App`**  
  Point d'entrée :
  * parse les arguments (`--config`, `--strategy`, `--report`) ;
  * charge la configuration ;
  * construit le modèle ;
  * exécute la simulation ;
  * génère les rapports JSON.

#### `fr.esipe.elevatorsim.model`

* **`Building`**  
  Représente le bâtiment :
  * nombre d'étages,
  * hauteur d'étage,
  * liste des étages,
  * liste des ascenseurs.

* **`Floor`**  
  Étage (index + résidents associés).

* **`Resident`**
  * id unique,
  * étage de domicile,
  * plans de trajets (`ResidentTripPlan`).

* **`ResidentTripPlan`**
  * fenêtre horaire (earliest/latest),
  * étage cible (travail, domicile, etc.).

* **`Elevator`**  
  Modèle physique simplifié :
  * position continue (en étages),
  * vitesse + accélération,
  * arrêts programmés,
  * portes (temps d'ouverture),
  * suivi de l'énergie consommée (modèle simplifié).

* **`ElevatorRequest`**  
  Une demande de transport :
  * id résident,
  * heure de demande,
  * étage d'origine / destination,
  * ascenseur assigné,
  * temps de prise en charge,
  * temps d'arrivée.

#### `fr.esipe.elevatorsim.simulation`

* **`SimulationClock`**  
  Gère le temps discret :
  * durée totale (ex : une journée),
  * pas de simulation (tick).

* **`Simulation`**  
  Composant central :
  * génère les `ElevatorRequest` à partir des `ResidentTripPlan` et de la config (avec graine pseudo-aléatoire) ;
  * applique une `ElevatorStrategy` pour décider des arrêts ;
  * met à jour la physique des ascenseurs à chaque tick ;
  * suit les événements (pickups, dropoffs, arrêts d'ascenseurs) ;
  * calcule les métriques globales (temps, énergie, occupation) ;
  * produit des structures exploitables pour les rapports.

#### `fr.esipe.elevatorsim.strategy`

* **`ElevatorStrategy`**  
  Interface générique pour les heuristiques.

* **`FcfsElevatorStrategy`**
  * First-Come, First-Served :
  * traite la requête la plus ancienne disponible.

* **`NearestRequestStrategy`**
  * choisit la requête dont l'étage d'origine est le plus proche de l'ascenseur libre ;
  * meilleure réactivité moyenne dans beaucoup de cas.

* **`StrategyFactory`**
  * fabrique une stratégie à partir d'un nom (`fcfs`, `nearest`, etc.) ;
  * utilisée par `App` pour brancher facilement une heuristique.

#### `fr.esipe.elevatorsim.config`

* **`SimulationConfig`**  
  Mapping du fichier JSON de config (bâtiment, résidents, ascenseurs, simulation).

* **`ConfigLoader`**  
  Charge un `SimulationConfig` depuis le classpath via Jackson.

* **`ModelFactory`**  
  Construit :
  * le `Building` (étages, ascenseurs),
  * les `Resident`,
  * leurs `ResidentTripPlan`,  
    à partir du `SimulationConfig` (incluant la graine aléatoire pour rendre la simulation déterministe).

#### `fr.esipe.elevatorsim.stats`

* **`SimulationStats`**  
  Objet contenant les métriques agrégées :
  * nb total de requêtes,
  * nb complétées,
  * temps d'attente (moyen, médian, max),
  * temps de trajet (moyen, médian, max),
  * temps d'attente moyen par étage d'origine,
  * énergie totale et énergie par requête,
  * taux moyen d'occupation des cabines.

* **`SimulationReport`**  
  Structure sérialisable (stats + liste de requêtes).

* **`JsonReportWriter`**  
  Génère un rapport global JSON (stats + toutes les requêtes).

* **`ElevatorStopsJsonWriter`**  
  Génère un JSON des arrêts par ascenseur :
  * heure,
  * étage,
  * nb montés,
  * nb descendus,
  * passagers restants.

* **`ResidentsReportJsonWriter`**  
  Génère un JSON par résident :
  * id du résident,
  * étage de domicile,
  * liste de ses trajets (origine, destination, heures, ascenseur utilisé).

---

## 5. Fichiers de configuration

Les configurations sont placées dans `src/main/resources/config/`.

Exemple : `config/demo-config.json` :

* `building`
  * `floorsCount`
  * `floorHeight`
* `residents`
  * `defaultPerFloor`
  * `morning` : fenêtre horaire vers un étage cible (souvent 0)
  * `evening` : fenêtre horaire de retour vers l'étage de domicile
* `elevators`
  * liste d'ascenseurs (`id`, `capacity`, `maxSpeedFloorsPerSecond`, etc.)
* `simulation`
  * `dayDurationSeconds`
  * `tickSeconds`
  * `randomSeed` (pour la reproductibilité)

Ce format permet d'expérimenter différentes topologies et profils de trafic sans modifier le code.

---

## 6. Sorties & rapports générés

À chaque exécution, l'application génère (selon le `--report`) :

### 1. Rapport global – `.../demo-report.json`

Contient :

* **Statistiques globales :**
  * nombre de requêtes & requêtes complétées ;
  * temps d'attente moyen, médian, max ;
  * temps de trajet moyen, médian, max ;
  * temps d'attente moyen par étage d'origine ;
  * énergie totale consommée (modèle simplifié) ;
  * énergie moyenne par requête ;
  * taux moyen d'occupation des cabines (0.0 à 1.0).

* **Détail de chaque requête :**
  * résident,
  * origine/destination,
  * horaires (appel, prise en charge, arrivée),
  * ascenseur assigné.

### 2. Rapport arrêts ascenseurs – `.../demo-report-elevators.json`

Pour chaque ascenseur (clé = id) :

* liste des arrêts avec :
  * heure,
  * étage,
  * nombre de passagers montés,
  * nombre de passagers descendus,
  * passagers à bord après l'arrêt.

### 3. Rapport par résident – `.../demo-report-residents.json`

Pour chaque résident :

* id, étage de domicile,
* liste de ses trajets :
  * étage d'origine,
  * étage de destination,
  * heure de demande,
  * heure de prise en charge,
  * heure d'arrivée,
  * ascenseur utilisé.

Ces rapports servent de base directe pour :

* `doc/study.pdf` (analyses, graphiques, comparaisons),
* `doc/dev.pdf` (exemple d'API interne & extensibilité),
* vérifications et tests.

---

## 7. Roadmap & état d'avancement

### 7.1. Déjà réalisé ✅

#### Infrastructure
* Projet Maven (Java 17).
* Organisation par packages claire.
* Tests de fumée (`SimulationSmokeTest`).

#### Modélisation
* Tour (`Building`, `Floor`) avec étages.
* Résidents (`Resident`) avec habitudes (`ResidentTripPlan`).
* Génération de requêtes déterministe à partir d'une graine.

#### Ascenseurs
* `Elevator` avec :
  * position continue,
  * vitesse + accélération,
  * portes,
  * multi-ascenseurs gérés dans la simulation.
* Modèle énergétique simplifié.

#### Stratégies de contrôle
* `FcfsElevatorStrategy` (First-Come, First-Served).
* `NearestRequestStrategy` (requête la plus proche).
* `StrategyFactory` (sélection via argument).

#### Configuration & reproductibilité
* Fichiers JSON externes.
* `SimulationConfig` + `ModelFactory`.
* Graine aléatoire dans la config → simulations reproductibles.

#### Métriques & rapports
* Temps d'attente : moyen, médian, max.
* Temps de trajet : moyen, médian, max.
* Analyse par étage d'origine.
* Énergie totale et par requête.
* Taux moyen d'occupation des cabines.
* Rapports JSON : global, par ascenseur, par résident.

**L'essentiel du cahier des charges est couvert.**

---

### 7.2. Reste à faire (priorité haute avant rendu)

Ces points complètent le projet pour coller parfaitement au sujet.

#### 1. Scénarios supplémentaires

Ajouter plusieurs configurations :

* `weekday.json` : trafic de semaine (matin/soir travail).
* `weekend.json` : trafic plus étalé, visites, etc.
* `evacuation.json` :
  * scénario où tous les résidents doivent rejoindre le rez-de-chaussée à partir d'un instant donné ;
  * mesurer le temps d'évacuation complet.

Intégrer ces scénarios dans `doc/study.pdf` :
* comparaison de stratégies selon le scénario.

#### 2. Heuristique multi-ascenseurs dédiée

Introduire un composant (ex. `TrafficManager`) chargé de :

* attribuer chaque requête à un ascenseur en tenant compte :
  * de la position actuelle,
  * de la charge,
  * éventuellement de "zones" (bas/haut).

Comparer :
* stratégies locales actuelles vs gestion coordonnée,
* en termes de temps d'attente et d'énergie.

#### 3. Tests supplémentaires

Ajouter des tests unitaires plus ciblés :

* comportement d'`Elevator` (atteinte de l'étage cible, portes, stops),
* vérification de FCFS vs Nearest sur un petit jeu de requêtes fixe,
* tests de chargement et validation de config (valeurs invalides, etc.).

#### 4. Documentation du code

Ajouter / compléter :

* Javadoc sur les classes publiques et les stratégies.
* commentaires sur les parties de logique non triviales.

Nettoyer :

* imports inutilisés,
* éventuels restes de code mort.

---

### 7.3. Reste à faire (documentation projet)

À préparer dans le dépôt (non généré automatiquement par le code) :

#### 1. `AUTHORS`
* Noms + mails universitaires des 3 membres du groupe.

#### 2. `LICENSE`
* Choisir une licence (ex. MIT) et l'inclure.

#### 3. `doc/user.pdf`

Contenu recommandé :
* installation,
* exécution (exemples de commandes),
* description des stratégies disponibles,
* description des fichiers de configuration,
* description des rapports générés.

#### 4. `doc/dev.pdf`

Contenu recommandé :
* architecture globale (packages, classes principales),
* diagramme simplifié,
* comment :
  * ajouter une stratégie,
  * ajouter un nouveau scénario de config,
  * étendre les métriques ou formats de sortie.

#### 5. `doc/experience_x.pdf`, `doc/experience_y.pdf`, `doc/experience_z.pdf`

Un par membre :
* tâches réalisées,
* difficultés rencontrées,
* solutions techniques,
* ce qui a été appris.

#### 6. `doc/study.pdf`

Exploiter les rapports JSON pour :
* comparer 1 vs 2 ascenseurs ;
* comparer `fcfs` vs `nearest` ;
* étudier l'impact des configurations (par ex. charge élevée, évacuation) ;

Analyser :
* temps moyen vs pire cas,
* disparités entre étages,
* compromis entre performance et consommation.

---

### 7.4. Bonus (optionnel)

* Ajouter d'autres stratégies :
  * prise en compte du sens de déplacement,
  * priorisation heures de pointe,
  * ascenseur "express" pour étages élevés.
* Simuler des étages ou habitants "spéciaux" (profils d'habitudes différents).
* Interface plus visuelle :
  * affichage texte animé de la position des ascenseurs,
  * ou petite GUI (Swing/JavaFX) si le temps le permet.
