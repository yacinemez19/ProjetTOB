# Projet long - TOB
## Sujet choisi : Logiciel de montage vidéo

### Dossier doc 
Chaque dossier contient le code latex permettant de générer "nom du fichier".pdf

### SDK
Le projet fonctionne actuellement avec Amazon Corretto 21.

### Structure du projet
Comme librairie pour gérer le vidéo nous utiliserons Gstream c’est très modulaire et plutôt performant, et pour l'interface JavaFX.

Notre architecture repose sur plusieurs couches afin de faciliter l'intégration de GStreamer dans une application JavaFX :

- GStreamer (librairie bas niveau à installer sur la machine)
- Une API Java pour manipuler GStreamer sans gérer les détails bas niveau
- Une API Java pour intégrer GStreamer dans JavaFX
- JavaFX pour l'interface graphique

### Installation 

Pour installer les packages Java j'ai fait un zip avec les jar que j'ai utilisé : https://drive.google.com/file/d/1asHt_QlrIhION2OYhZEofzvkUA2bWqwi/view?usp=sharing
il faut les ajouter dans le projet dans un dossier `./packages/`. Notez que la mon zip JavaFX est configuré pour mac. 

Suivez les instructions selon votre platforme.

## Windows 

La première étape est d'avoir WSL. Vous pouvez l'installer dans un terminal avec la commande suivante. 
```
wsl --install
```

On va émuler le projet dans un docker mais pour l'interface graphique il faut que Docker puisse communiquer avec celle de Windows. Pour ça il faut installer *VcXsrv*. Pour ça vous pouvez suivre ce tuto : https://www.youtube.com/watch?v=4SZXbl9KVsw.

Une fois cela fait allez à la racine du projet et tapez 
```bash
docker build -t ProjetTOB .
```
Ensuite pour le serveur VcXsrv il faut récupérer l'ip local du pc. Vous pouvez la trouver en tapant 
```
ipconfig
```
Et vous chercher l'ip en face de IPv4. Une fois cela fait vous pouvez lancer le docker en tapant (il faut remplacer <IP> par l'IP que vous venez de copier : 

```bash
docker run -it -e DISPLAY=<IP>:0.0 -v /tmp/.X11-unix:/tmp/.X11-unix ProjetTOB
```

## Linux/MacOS

Pour Gstreamer il faut aller sur le site et télécharger la version adaptée a votre machine : https://gstreamer.freedesktop.org/download/



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
--module-path "/Users/alex/Documents/Scolarité/N7/Annee_1/Java/Projet Long TOB/packages/javafx-sdk-21.0.6/lib" --add-modules javafx.controls,javafx.graphics,javafx.fxml,javafx.media 
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
   --module-path "C:\javafx-sdk-21.0.6\lib" --add-modules javafx.controls,javafx.graphics,javafx.fxml,javafx.media 
   ```

#### Pour macOS/Linux :

   ```bash
   --module-path "/chemin/vers/javafx-sdk-21.0.6/lib" --add-modules javafx.controls,javafx.graphics,javafx.fxml,javafx.media 
   ```
