# Missié Moustass 2 - Application d'Enregistrement Audio

## 📝 Description

Missié Moustass 2 est une application Java d'enregistrement audio avec interface graphique Swing. Elle permet aux utilisateurs d'enregistrer, de lire, de gérer et de partager des enregistrements audio de manière sécurisée avec chiffrement AES.

## ✨ Fonctionnalités

### 🎤 Enregistrement Audio

- Enregistrement audio en temps réel
- Sauvegarde automatique avec chiffrement AES
- Gestion des métadonnées (nom, date, durée)
- Support de différents formats audio

### 🔐 Sécurité

- Chiffrement AES des enregistrements
- Système d'authentification utilisateur
- Gestion des clés de chiffrement personnalisées
- Protection des données sensibles

### 👥 Gestion des Utilisateurs

- Système de connexion/inscription
- Interface administrateur
- Gestion des permissions utilisateur
- Historique des connexions récentes

### 📤 Partage d'Enregistrements

- Partage sécurisé entre utilisateurs
- Gestion des permissions de partage
- Historique des partages
- Notifications de partage

### 🎵 Lecture Audio

- Lecteur audio intégré
- Contrôles de lecture (play/pause/stop)
- Ouverture avec clé personnalisée
- Gestion des erreurs de déchiffrement

## 🏗️ Architecture

### Structure du Projet

```
src/
├── controller/          # Contrôleurs MVC
├── model/              # Modèles de données
├── service/            # Services métier
│   ├── impl/          # Implémentations des services
│   └── ...            # Services principaux
├── view/              # Interfaces utilisateur (Swing)
├── util/              # Utilitaires et helpers
├── main/              # Classes principales
└── test/              # Tests unitaires
```

### Services Principaux

- **AudioRecordingService** : Gestion des enregistrements audio
- **UserService** : Gestion des utilisateurs
- **CryptographyService** : Chiffrement/déchiffrement
- **SharedRecordingService** : Partage d'enregistrements
- **DatabaseService** : Accès aux données SQLite
- **NotificationService** : Système de notifications

### Technologies Utilisées

- **Java Swing** : Interface graphique
- **SQLite** : Base de données locale
- **AES** : Chiffrement des données
- **RSA** : Chiffrement asymétrique
- **Maven/Gradle** : Gestion des dépendances

## 🚀 Installation et Démarrage

### Prérequis

- Java 8 ou supérieur
- Système audio fonctionnel
- Permissions d'écriture dans le répertoire de l'application

### Compilation

```bash
# Compilation des sources
javac -cp "lib/*" src/**/*.java -d bin/

# Ou utilisation d'un IDE comme Eclipse/IntelliJ
```

### Exécution

```bash
# Démarrage normal
java -cp "bin:lib/*" Application

# Démarrage en mode debug
java -cp "bin:lib/*" Application --debug

# Démarrage avec correctifs
java -cp "bin:lib/*" Application --fix
```

## 🔧 Configuration

### Base de Données

- La base de données SQLite (`database.db`) est créée automatiquement
- Schéma initialisé au premier démarrage
- Sauvegarde automatique des données

### Enregistrements

- Stockés dans le dossier `recordings/`
- Chiffrés avec AES-256
- Métadonnées sauvegardées en base

### Logs

- Fichiers de logs dans le dossier `logs/`
- Niveaux : DEBUG, INFO, WARNING, ERROR, FATAL
- Rotation automatique des logs

## 🐛 Corrections Récentes

### Problèmes Résolus

1. **Boutons d'action désactivés** : Correction des conditions d'activation après sélection
2. **Erreur "no such column: encryption_key"** : Gestion d'exception avec fallback
3. **Contrainte NOT NULL sur shared_date** : Ajout de la date de partage automatique
4. **Problème de lecture des anciens enregistrements** : Amélioration de la gestion des clés

### Améliorations de Sécurité

- Validation des clés de chiffrement
- Protection contre les accès non autorisés
- Gestion des erreurs de déchiffrement

## 📊 Structure de la Base de Données

### Tables Principales

- **users** : Informations utilisateur
- **recordings** : Métadonnées des enregistrements
- **shared_recordings** : Partages entre utilisateurs
- **user_keys** : Clés de chiffrement utilisateur

## 🧪 Tests

### Tests Unitaires

```bash
# Exécution des tests
java -cp "bin:lib/*:test/" org.junit.runner.JUnitCore TestSuite
```

### Tests Audio

- Fichiers de test dans `test_audio/`
- Tests d'enregistrement et de lecture
- Validation du chiffrement/déchiffrement

## 📈 Monitoring et Logs

### Système de Logs

- **LogManager** : Gestionnaire centralisé des logs
- Niveaux configurables
- Sortie console et fichier

### Diagnostic

- Interface de diagnostic intégrée
- Vérification de l'intégrité des services
- Tests de connectivité base de données

## 🤝 Contribution

### Standards de Code

- Respect des principes SOLID
- Documentation JavaDoc
- Tests unitaires obligatoires
- Gestion d'erreurs robuste

### Workflow Git

```bash
# Cloner le repository
git clone [URL_DU_REPO]

# Créer une branche feature
git checkout -b feature/nouvelle-fonctionnalite

# Commit et push
git add .
git commit -m "Description des modifications"
git push origin feature/nouvelle-fonctionnalite
```

## 📄 Licence

Ce projet est sous licence [À DÉFINIR]. Voir le fichier LICENSE pour plus de détails.

## 👨‍💻 Auteurs

- **Équipe de développement** - Développement initial et maintenance

## 🆘 Support

Pour toute question ou problème :

1. Vérifier les logs dans le dossier `logs/`
2. Consulter la documentation technique
3. Créer une issue sur le repository Git

## 🔄 Versions

### Version Actuelle : 2.0

- Interface utilisateur améliorée
- Système de chiffrement renforcé
- Partage d'enregistrements
- Corrections de bugs critiques

### Roadmap

- [ ] Support de nouveaux formats audio
- [ ] Interface web
- [ ] Synchronisation cloud
- [ ] API REST

---

**Note** : Ce projet est en développement actif. Les fonctionnalités peuvent évoluer rapidement.
