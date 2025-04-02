# Projet long - TOB
## Sujet choisi : Logiciel de montage vidéo

### Dossier doc 
Chaque dossier contient le code latex permettant de générer "nom du fichier".pdf

### SDK
Le projet fonctionne actuellement avec Amazon Corretto 21.

### ✅ Étapes d'installation de JavaFX

**Télécharger JavaFX SDK**

   Rendez-vous sur le site officiel de JavaFX (OpenJFX) :  
   👉 [https://gluonhq.com/products/javafx/](https://gluonhq.com/products/javafx/)

   - Choisissez votre système d’exploitation.
   - Téléchargez la version **SDK** (ex : `javafx-sdk-21.0.6.zip`).
   - Décompressez le dossier dans un emplacement de votre choix (par exemple `C:\javafx-sdk-21.0.6` ou `/home/votre_user/javafx-sdk-21.0.6`).

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
