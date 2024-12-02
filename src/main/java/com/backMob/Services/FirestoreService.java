package com.backMob.Services;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class FirestoreService {

    private final Firestore firestore;

    @Autowired
    public FirestoreService(Firestore firestore) {
        this.firestore = firestore;  // Firestore instance is injected here
    }


    /**
     * Crée un document et retourne l'objet créé
     * @param collection Nom de la collection
     * @param entity Objet à sauvegarder
     * @param responseType Type de retour souhaité
             * @return L'objet créé converti dans le type demandé
     */
    public <T> T create(String collection, Object entity, Class<T> responseType) {
        try {
            DocumentReference docRef = firestore.collection(collection).document();
            // On ajoute l'ID généré à l'objet si c'est possible
            log.info("Tentative de création du document: " );
            if (entity instanceof BaseEntity) {
                ((BaseEntity) entity).setId(docRef.getId());
            }

            log.error("Erreur lors de la création du document Firestore");
            ApiFuture<WriteResult> result = docRef.set(entity);
            result.get(); // Attendre la confirmation

            // Récupérer et convertir le document créé
            DocumentSnapshot snapshot = docRef.get().get();
            return snapshot.toObject(responseType);

        } catch (Exception e) {
            log.error("Erreur lors de la création dans {}: {}", collection, e.getMessage());
            throw new FirestoreOperationException("Erreur création document", e);

        }
    }

    /**
     * Récupère tous les documents d'une collection
     * @param collection Nom de la collection
     * @param responseType Type des objets à retourner
     * @return Liste des documents convertis
     */
    public <T> List<T> getAll(String collection, Class<T> responseType) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(collection).get();
            List<T> list = new ArrayList<>();

            for (DocumentSnapshot document : future.get().getDocuments()) {
                T item = document.toObject(responseType);
                if (item instanceof BaseEntity) {
                    ((BaseEntity) item).setId(document.getId());
                }
                list.add(item);
            }

            return list;
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de {}: {}", collection, e.getMessage());
            throw new FirestoreOperationException("Erreur récupération documents", e);
        }
    }

    /**
     * Recherche avec critères multiples
     * @param collection Nom de la collection
     * @param criteria Map des critères (field, value)
     * @param responseType Type des objets à retourner
     * @return Liste des documents correspondants
     */
    public <T> List<T> findByFields(String collection, Map<String, Object> criteria, Class<T> responseType) {
        try {
            Query query = firestore.collection(collection);

            // Application des critères
            for (Map.Entry<String, Object> criterion : criteria.entrySet()) {
                query = query.whereEqualTo(criterion.getKey(), criterion.getValue());
            }

            ApiFuture<QuerySnapshot> future = query.get();
            List<T> results = new ArrayList<>();

            for (DocumentSnapshot document : future.get().getDocuments()) {
                T item = document.toObject(responseType);
                if (item instanceof BaseEntity) {
                    ((BaseEntity) item).setId(document.getId());
                }
                results.add(item);
            }

            return results;
        } catch (Exception e) {
            log.error("Erreur lors de la recherche dans {}: {}", collection, e.getMessage());
            throw new FirestoreOperationException("Erreur recherche documents", e);
        }
    }

    /**
     * Met à jour un document existant
     * @param collection Nom de la collection
     * @param id ID du document
     * @param updates Données à mettre à jour
     * @param responseType Type de retour souhaité
     * @return Document mis à jour
     */
    public <T> T update(String collection, String id, Map<String, Object> updates, Class<T> responseType) {
        try {
            DocumentReference docRef = firestore.collection(collection).document(id);
            ApiFuture<WriteResult> future = docRef.update(updates);
            future.get(); // Attendre la confirmation

            // Récupérer la version mise à jour
            DocumentSnapshot updated = docRef.get().get();
            return updated.toObject(responseType);

        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour dans {}: {}", collection, e.getMessage());
            throw new FirestoreOperationException("Erreur mise à jour document", e);
        }
    }

    /**
     * Supprime un document
     * @param collection Nom de la collection
     * @param id ID du document à supprimer
     * @return true si supprimé avec succès
     */
    public boolean delete(String collection, String id) {
        try {
            ApiFuture<WriteResult> future = firestore.collection(collection).document(id).delete();
            future.get(); // Attendre la confirmation
            return true;
        } catch (Exception e) {
            log.error("Erreur lors de la suppression dans {}: {}", collection, e.getMessage());
            throw new FirestoreOperationException("Erreur suppression document", e);
        }
    }

    /**
     * Recherche par ID avec conversion dynamique
     */
    public <T> T findById(String collection, String id, Class<T> responseType) {
        try {
            DocumentSnapshot document = firestore.collection(collection).document(id).get().get();
            if (!document.exists()) {
                return null;
            }

            T item = document.toObject(responseType);
            if (item instanceof BaseEntity) {
                ((BaseEntity) item).setId(document.getId());
            }
            return item;

        } catch (Exception e) {
            log.error("Erreur lors de la recherche par ID dans {}: {}", collection, e.getMessage());
            throw new FirestoreOperationException("Erreur recherche document", e);
        }
    }

    /**
     * Exception personnalisée pour les opérations Firestore
     */
    public static class FirestoreOperationException extends RuntimeException {
        public FirestoreOperationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Classe de base pour les entités avec ID
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public abstract static class BaseEntity {
        private String id;
    }
}