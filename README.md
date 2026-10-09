# Les Capuches d'Opale

Monorepo de gestion de guilde d'aventuriers (coordination front & back — Ynov M2 Dev FullStack).

## Structure

```
coordination-back-front/
├── front/   # React + Vite + TypeScript
├── back/    # Spring Boot (Java 21)
└── .github/workflows/ci.yml
```

## Front (`front/`)

```bash
cd front
npm install
npm run dev
```

Build de production : `npm run build`

## Back (`back/`)

```bash
cd back
./mvnw spring-boot:run
```

Sous Windows : `.\mvnw.cmd spring-boot:run`

Endpoint de santé : [http://localhost:8080/api/health](http://localhost:8080/api/health)

Compile uniquement : `./mvnw -DskipTests compile`

## CI

Les GitHub Actions vérifient que le front (`npm run build`) et le back (`./mvnw compile`) compilent à chaque push / PR sur `main`.

## Convention de commits

Format :

```
type(issue-XXX): description courte
```

| Type | Usage |
|------|--------|
| `feat` | Nouvelle fonctionnalité |
| `fix` | Correction de bug |
| `docs` | Documentation uniquement |
| `style` | Formatage, sans changement de logique |
| `refactor` | Refactoring sans feat ni fix |
| `test` | Ajout ou correction de tests |
| `chore` | Tâches techniques (CI, deps, config…) |

Exemples :

```
feat(issue-12): ajouter la liste des aventuriers
fix(issue-8): corriger le calcul du taux journalier
docs(issue-3): préciser la convention de commits
chore(issue-1): configurer la CI front et back
```

Règles :

- `XXX` = numéro de l’issue GitHub
- description en français, à l’impératif, minuscule, sans point final
- un commit = un sujet cohérent
