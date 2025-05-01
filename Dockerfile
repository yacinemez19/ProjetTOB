FROM ubuntu:22.04

# Installe Java + JavaFX + GStreamer + dépendances GUI
RUN apt-get update && \
    apt-get install -y wget gnupg unzip libgtk-3-0 libxtst6 libxrender1 libgl1-mesa-glx && \
    wget -O- https://apt.corretto.aws/corretto.key | gpg --dearmor | tee /etc/apt/trusted.gpg.d/corretto.gpg > /dev/null && \
    echo "deb https://apt.corretto.aws stable main" | tee /etc/apt/sources.list.d/corretto.list && \
    apt-get update && \
    apt-get install -y java-21-amazon-corretto-jdk \
    wget unzip \
    libgtk-3-0 libxtst6 libxrender1 libgl1-mesa-glx \
    gstreamer1.0-tools \
    gstreamer1.0-plugins-base \
    gstreamer1.0-plugins-good \
    gstreamer1.0-plugins-bad \
    gstreamer1.0-plugins-ugly \
    gstreamer1.0-libav \
    libgstreamer1.0-dev \
    libgstreamer-plugins-base1.0-dev \
    && apt-get clean

# Installer JavaFX SDK
RUN wget https://download2.gluonhq.com/openjfx/21.0.6/openjfx-21.0.6_linux-x64_bin-sdk.zip && \
    unzip openjfx-21.0.6_linux-x64_bin-sdk.zip && \
    mv javafx-sdk-21.0.6 /opt/javafx && \
    rm openjfx-21.0.6_linux-x64_bin-sdk.zip

ENV PATH="${PATH}:/opt/javafx/bin"
ENV JAVAFX_LIB="/opt/javafx/lib"

# Ajoute app dans le conteneur
WORKDIR /app
COPY ./src/ ./src/
COPY ./videos/ ./videos/
COPY packages/ ./libs

# Compiler tous les .java
RUN find src -name "*.java" > sources.txt && \
    javac -encoding UTF-8 -cp ".:libs/*:/opt/javafx/lib/*" -d . @sources.txt

RUN cp -r src/ressources ./ressources

# Commande de lancement (JavaFX a besoin du module path)
CMD ["java", "--module-path", "/opt/javafx/lib", "--add-modules", "javafx.controls,javafx.fxml,javafx.media", "-cp", ".:libs/*:/opt/javafx/lib/*", "Main"]
