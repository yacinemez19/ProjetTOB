package com;

import java.net.URI;

public interface VideoImporter {
    /**
     * Importe une vidéo à partir d'un chemin ou d'un URI.
     *
     * @param source Chemin ou URI de la vidéo à importer.
     * @return Un objet com.Clip représentant la vidéo importée.
     * @throws IllegalArgumentException Si le chemin est invalide ou si le fichier n'est pas une vidéo.
     */
    Clip importVideo(URI source) throws IllegalArgumentException;
}
