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

### 1. Ajouter les JARs JavaFX

1. Fais un clic droit sur ton projet dans l’**Explorer** Eclipse.
2. Sélectionne **Build Path** > **Configure Build Path**.
3. Va dans l’onglet **Libraries**.
4. Clique sur **Add External JARs…**.
5. Navigue jusqu’au dossier `lib` du SDK JavaFX (ex. `C:\javafx-sdk-21\lib`).
6. Sélectionne **tous les fichiers `.jar`** dans ce dossier et clique sur **Ouvrir**.
7. Clique sur **Apply and Close**.

---

### 2. Ajouter les options VM pour l'exécution

1. Va dans **Run > Run Configurations…**
2. Sélectionne ta configuration de lancement dans la liste à gauche.
3. Va dans l’onglet **Arguments**.
4. Dans la section **VM arguments**, ajoute :

#### Pour Windows :
   ```bash
   --module-path "C:\javafx-sdk-21.0.6\lib" --add-modules javafx.controls
   ```

#### Pour macOS/Linux :

   ```bash
   --module-path "/chemin/vers/javafx-sdk-21.0.6/lib" --add-modules javafx.controls
   ```
