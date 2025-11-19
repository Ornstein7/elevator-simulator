# Elevator Simulator

Simulation d'ascenseurs dans une tour d'habitation (projet Java/Maven).

- **Langage** : Java 17  
- **Build** : Maven  
- **Dépendance externe** : Jackson (JSON)

---

## 1. Construction

À la racine du projet :

```bash
mvn clean package
```

Le JAR exécutable (avec dépendances) est généré dans `target/` :

```text
target/elevator-simulator-1.0-SNAPSHOT-jar-with-dependencies.jar
```

---

## 2. Exécution

### Mode non interactif

```bash
java -jar target/elevator-simulator-1.0-SNAPSHOT-jar-with-dependencies.jar \
  --config=config/demo-config.json \
  --strategy=nearest \
  --report=target/reports/demo-report.json
```

**Options principales :**

- `--config=...` : chemin (classpath) du fichier JSON de config (par défaut : `config/demo-config.json`)
- `--strategy=fcfs|nearest` : heuristique de contrôle des ascenseurs
- `--report=...` : base du chemin du rapport global JSON

(Si aucun argument n’est fourni, l’application utilise config/demo-config.json, la stratégie nearest et écrit les rapports dans target/reports/demo-report.json (et ses variantes))

### Mode interactif (menu console)

```bash
java -jar target/elevator-simulator-1.0-SNAPSHOT-jar-with-dependencies.jar --interactive
```

Menu permettant de :

- afficher un résumé (bâtiment, résidents, ascenseurs)
- ajouter / modifier / supprimer un ascenseur (+ undo simple)
- changer de stratégie (`fcfs` / `nearest`)
- choisir le chemin du rapport JSON
- lancer une simulation et voir un résumé des métriques

---

## 3. Configuration (`config/*.json`)

Les fichiers de configuration sont dans `src/main/resources/config/`.

Une config décrit :

- le **bâtiment** : `floorsCount`, `floorHeight`
- les **résidents** : nombre par étage, habitudes (fenêtres horaires)
- les **ascenseurs** : `id`, `capacity`, vitesse max, accélération, temps de porte
- la **simulation** : durée de la journée, `tickSeconds`, `randomSeed` (reproductibilité)

Pour tester un autre scénario, créer un nouveau JSON dans `config/` et utiliser `--config=...`.

---

## 4. Rapports générés

Après une simulation, plusieurs fichiers JSON sont écrits (par défaut dans `target/reports/`) :

- `<report>.json` : rapport global (temps d'attente/trajet moyen, médian, max, par étage, énergie, occupation, liste des requêtes)
- `<report>-elevators.json` : arrêts par ascenseur (heure, étage, montés, descendus, passagers restants)
- `<report>-residents.json` : trajets par résident (origine, destination, horaires, ascenseur utilisé)

---

## 5. Documentation

Dans le répertoire `doc/` :

- `user.pdf` : manuel utilisateur (installation, exécution, menus, rapports)
- `dev.pdf` : documentation développeur (architecture, extension du code)
- `study.pdf` : étude des simulations (comparaison de scénarios et heuristiques)
- `experience_x.pdf`, `experience_y.pdf`, `experience_z.pdf` : retours individuels des 3 membres du groupe

