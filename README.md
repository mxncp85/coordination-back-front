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

Les GitHub Actions vérifient que le front (`npm run build`) et le back (`./mvnw compile`) compilent à chaque push / PR sur `main`, puis lancent une analyse SonarQube.

### Secrets SonarQube

À configurer dans GitHub → Settings → Secrets and variables → Actions :

| Secret | Description |
|--------|-------------|
| `SONAR_TOKEN` | Token d’authentification SonarQube / SonarCloud |
| `SONAR_HOST_URL` | URL du serveur SonarQube (ex. `https://sonarcloud.io` ou votre instance) |

Adapter aussi `sonar.projectKey` (et `sonar.organization` pour SonarCloud) dans `sonar-project.properties`.

### Rapport Sonar dans les PR (vers `main`)

Sur les PR ciblant `main`, SonarCloud publie un check **SonarCloud Code Analysis**, un résumé dans Conversation, et des annotations sur les lignes concernées — à condition que :

1. Le projet soit **lié à GitHub** dans SonarCloud (Analyze new project via GitHub, pas un projet « manuel » seul)
2. Les secrets `SONAR_TOKEN` / `SONAR_HOST_URL` soient configurés
3. (Optionnel) Pour bloquer le merge si le quality gate échoue : GitHub → Settings → Rules / Branch protection → require status check **`SonarCloud Code Analysis`**

### Dependabot

Chaque lundi, Dependabot ouvre des PR pour mettre à jour :

- les deps npm (`front/`)
- les deps Maven (`back/`)
- les GitHub Actions

Les PR Dependabot sont auto-mergées (squash) dès que la CI passe. Config : `.github/dependabot.yml`.

Prérequis GitHub : activer **Allow auto-merge** (Settings → General) et, si tu as une branch protection, exiger les checks CI avant merge.

## Convention de branches

Format contrôlé en premier job CI sur les PR vers `main` (si le nom est invalide, front / back / Sonar ne démarrent pas) :

```
type/issue-XXX-description-courte
```

Exemples :

```
feat/issue-12-liste-aventuriers
fix/issue-8-taux-journalier
chore/issue-1-config-ci
```

Règles :

- `type` ∈ `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`
- `XXX` = numéro de l’issue GitHub
- description en kebab-case (minuscules, tirets)
- exception : branches `dependabot/*`

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
