# Projet long - TOB
## Sujet choisi : Logiciel de montage vidéo

### Dossier doc 
Chaque dossier contient le code latex permettant de générer "nom du fichier".pdf

### SDK
Le projet fonctionne actuellement avec Amazon Corretto 21.

### IntelliJ Configuration
Ajouter ça dans les VM options (On utilise la version 21.0.6.

```
--module-path "/chemin/vers/javaFX/SDK/javafx-sdk-21.0.6/lib" --add-modules javafx.controls
```

## 🛠️ Configuration JavaFX sur Eclipse

1. **Ouvrir Eclipse** et **charger votre projet JavaFX**.

2. Clic droit sur le projet → **Run As** → **Run Configurations...**

3. Dans la fenêtre qui s’ouvre :
   - Sélectionner votre classe principale (sous "Java Application" à gauche).
   - Aller dans l'onglet **Arguments**.

4. Dans la section **VM arguments**, ajouter la ligne suivante :

   ```bash
   --module-path "/chemin/vers/javafx-sdk-21.0.6/lib" --add-modules javafx.controls
