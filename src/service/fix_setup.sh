#!/bin/bash

# Script de configuration pour les corrections

echo "=== Configuration des correctifs pour l'application Audio Recorder ==="

# Créer les répertoires nécessaires
mkdir -p bin/service bin/util bin/model

# Compilation des classes dans l'ordre des dépendances
echo "Compilation des classes..."
javac -d bin -encoding UTF-8 -cp "src:lib/*" src/model/User.java
javac -d bin -encoding UTF-8 -cp "src:lib/*" src/model/AudioRecording.java
javac -d bin -encoding UTF-8 -cp "src:lib/*" src/service/CryptographyService.java
javac -d bin -encoding UTF-8 -cp "src:lib/*" src/service/AudioRecordingService.java
javac -d bin -encoding UTF-8 -cp "src:lib/*" src/service/UserService.java
javac -d bin -encoding UTF-8 -cp "src:lib/*" src/service/AESCryptographyServiceFix.java
javac -d bin -encoding UTF-8 -cp "src:lib/*" src/service/UserServiceImplFix.java
javac -d bin -encoding UTF-8 -cp "src:lib/*" src/service/AudioRecordingServiceFixExtended.java
javac -d bin -encoding UTF-8 -cp "src:lib/*" src/service/FixDiagnosticInterface.java
javac -d bin -encoding UTF-8 -cp "src:lib/*" src/util/AudioTestFix.java

if [ $? -eq 0 ]; then
    echo "Compilation réussie!"
else
    echo "Erreur lors de la compilation"
    exit 1
fi

echo ""
echo "=== Options disponibles ==="
echo "1. Tester le service de cryptographie amélioré (diagnostic)"
echo "   java -cp \"bin:lib/*\" service.FixDiagnosticInterface"
echo ""
echo "2. Comparer l'audio avec et sans chiffrement"
echo "   java -cp \"bin:lib/*\" util.AudioTestFix"
echo ""
echo "3. Pour résoudre le problème de bruit audio:"
echo "   - Utiliser AESCryptographyServiceFix à la place de AESCryptographyService"
echo "   - Activer/désactiver le chiffrement avec le paramètre du constructeur"
echo ""
echo "4. Pour résoudre le problème d'affichage des utilisateurs:"
echo "   - Utiliser UserServiceImplFix qui implémente correctement getAllUsers()"
echo ""
echo "=== Installation terminée ===" 