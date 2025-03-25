# Projet Missié Moustass

## Description
Application de gestion sécurisée de messages vocaux développée pour l'entreprise Barbichetz dans le contexte de la protection des communications confidentielles à l'île Maurice.

## Fonctionnalités
- Enregistrement de messages vocaux
- Lecture des messages
- Chiffrement des données avec AES
- Vérification d'intégrité avec SHA
- Interface utilisateur intuitive
- Stockage en base de données SQLite

## Structure du projet

### Packages
- `com.barbichetz.audio` : Gestion des enregistrements et lectures audio
- `com.barbichetz.security` : Chiffrement et déchiffrement des données
- `com.barbichetz.storage` : Persistance des données en base SQLite
- `main` : Classes principales de l'application

### Classes principales
- `AudioRecorder` : Interface utilisateur et point d'entrée de l'application
- `AudioFormatManager` : Gestion des formats audio
- `AudioPlayer` : Lecture des fichiers audio
- `AES` : Chiffrement et déchiffrement des données
- `SHA` : Vérification d'intégrité des messages

## Guide pour les développeurs

### Processus d'enregistrement
1. L'utilisateur se connecte à l'application
2. L'utilisateur démarre l'enregistrement (bouton "Record")
3. L'audio est capturé via le microphone
4. L'utilisateur arrête l'enregistrement (bouton "Stop")
5. Les données audio sont chiffrées avec AES
6. L'enregistrement est stocké en base de données

### Processus de lecture
1. L'utilisateur sélectionne un enregistrement dans la liste
2. Les données sont récupérées depuis la base
3. Les données sont déchiffrées
4. L'intégrité est vérifiée avec SHA
5. Le message audio est lu via le système audio

### Problèmes connus et solutions
1. **NullPointerException lors de la lecture**
   - Problème : Le format audio peut être null
   - Solution : Utilisation de `AudioFormatManager.getDefaultFormat()`
   
2. **Lecture accélérée des enregistrements**
   - Problème : Taux d'échantillonnage incorrect lors de la lecture
   - Solution : Utilisation de `adjustPlaybackSpeed()` dans `AudioPlayer`

## Principes SOLID appliqués
1. **Single Responsibility Principle**
   - Chaque classe a une responsabilité unique
   - Exemple : `AudioFormatManager` gère uniquement les formats audio
   
2. **Open/Closed Principle**
   - Classes ouvertes à l'extension, fermées à la modification
   - Exemple : Nouvelle fonctionnalité d'ajustement de vitesse sans modifier le code existant
   
3. **Liskov Substitution Principle**
   - Les sous-classes peuvent remplacer leurs classes parentes
   
4. **Interface Segregation Principle**
   - Interfaces spécifiques plutôt que génériques
   
5. **Dependency Inversion Principle**
   - Dépendances vers des abstractions plutôt que des implémentations

## Comment lancer l'application
1. Assurez-vous d'avoir Java 17+ installé
2. Exécutez la classe `main.AudioRecorder`
3. Utilisez l'identifiant utilisateur par défaut (1)

## Comment contribuer
1. Créez une branche pour votre fonctionnalité
2. Assurez-vous de respecter les principes SOLID
3. Documentez clairement votre code
4. Soumettez une pull request

## Contacts
Pour toute question, contacter l'équipe de développement MedSyncDev. 