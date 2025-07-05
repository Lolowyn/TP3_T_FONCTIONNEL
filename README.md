# TP3 – Traitement fonctionnel sur MovieLens 100k

Petit pipeline **Scala (style fonctionnel)** → CSV de rapports, puis **dashboard Jupyter** pour l’exploration / visualisation.

Repo : <https://github.com/Lolowyn/TP3_T_FONCTIONNEL>

---

## 1. Ce que fait le projet

| Étape | Détail |
|-------|--------|
| **Ingestion** | Lecture des fichiers *MovieLens 100k* `movies.csv`, `ratings.csv` |
| **Nettoyage** | • Conversion des id en `Int` <br>• Filtre des ratings hors [1 ; 5] |
| **Enrichissement** | Reconstruction de la liste de genres (19 indicateurs binaires → tableau de chaînes) |
| **Statistiques** | Agrégations : nombre de notes & moyenne par film (`report.csv`), par genre (`report_by_genre.csv`)<br>+ stats glissantes 30 jours (`report_window.csv`) |
| **Rapports** | Les trois CSV ci-dessus sont (re)générés à chaque exécution du pipeline |
| **Dashboard** | Notebook **`dashboard.ipynb`** (et export `dashboard.html`) :<br>histogramme des notes, top 20 films, top 10 genres |

---

## 2. Architecture (fonctionnelle + FP Scala)

┌────────┐ read ┌──────────────┐
│movies │ │ movies DF │
│ratings │───► pipeline │ ratings DF │───► groupBy / map / reduce
└────────┘ └──────────────┘ │
writes CSV ◄─┘
↓
Jupyter dashboard


---

## 3. Choix techniques

| Besoin | Outil / lib | Pourquoi |
|--------|-------------|----------|
| Build & tests | **sbt 1.11 + Scala 3.3** | Simplicité, compatibilité FP |
| Agrégations | Scala collections (`map` / `filter` / `groupBy`) | Suffisant pour 100 k lignes |
| Notebook | **Python 3.12, pandas, matplotlib** | Visualiser rapidement les CSV |
| Export statique | `nbconvert` → `dashboard.ipynb` | Lecture sans Jupyter |
| Tests unitaires | **ScalaTest** (`PipelineSpec.scala`) | Vérifie filtrage & stats |

---

## 4. Comment exécuter 

### 4-1. Prérequis

* JDK 17 +  
* `sbt` installé (ou via `cs install sbt`)  
* Python ≥ 3.10

### 4-2. Cloner & lancer le pipeline (privé)

```bash
git clone https://github.com/Lolowyn/TP3_T_FONCTIONNEL.git
cd TP3_T_FONCTIONNEL

# compilation + tests
sbt test

# exécution du job
sbt run

# Les fichiers générés :
report.csv            # par film
report_by_genre.csv   # par genre
report_window.csv     # fenêtre glissante 30 j


# Explorer les rapports
head report.csv
column -s, -t report_by_genre.csv | less -S


# Lancer le dashboard Jupyter
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt      # pandas, matplotlib, jupyter

jupyter notebook                     # ouvrir dashboard.ipynb puis Run-All

# Structure du dépot
.
├── src/
│   ├── main/scala/model/Model.scala
│   ├── main/scala/pipeline/Pipeline.scala
│   └── test/scala/pipeline/PipelineSpec.scala
├── movies.csv               # MovieLens – films
├── ratings.csv              # MovieLens – notes
├── report.csv
├── report_by_genre.csv
├── report_window.csv
├── dashboard.ipynb          # notebook interactif
├── dashboard.html           # export statique
├── build.sbt
├── requirements.txt         # dépendances Python
└── README.md                # (ce fichier)



