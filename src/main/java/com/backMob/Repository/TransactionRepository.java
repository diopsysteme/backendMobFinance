package com.backMob.Repository;

import com.backMob.Entity.Transaction;
import com.backMob.Services.FirestoreService;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Repository
@Slf4j
public class TransactionRepository {
    private final FirestoreService firestoreService;
    private static final String COLLECTION = "transactions";

    public TransactionRepository(FirestoreService firestoreService) {
        this.firestoreService = firestoreService;
    }

    public Transaction create(Transaction transaction) {
        if (transaction.getDate() == null) {
            transaction.setDate(String.valueOf(LocalDateTime.now()));
        }
        return firestoreService.create(COLLECTION, transaction, Transaction.class);
    }

    public List<Transaction> findAll() {
        return firestoreService.getAll(COLLECTION, Transaction.class);
    }

    public Transaction findById(String id) {
        return firestoreService.findById(COLLECTION, id, Transaction.class);
    }

    public List<Transaction> findByType(String type) {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("type", type);
        return firestoreService.findByFields(COLLECTION, criteria, Transaction.class);
    }

    public List<Transaction> findBySender(String senderId) {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("idSender", senderId);
        return firestoreService.findByFields(COLLECTION, criteria, Transaction.class);
    }

    public List<Transaction> findByReceiver(String receiverId) {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("idReceiver", receiverId);
        return firestoreService.findByFields(COLLECTION, criteria, Transaction.class);
    }

    public List<Transaction> findByStatut(String statut) {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("statut", statut);
        return firestoreService.findByFields(COLLECTION, criteria, Transaction.class);
    }

    public Transaction updateStatut(String id, String newStatut) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("statut", newStatut);
        return firestoreService.update(COLLECTION, id, updates, Transaction.class);
    }

    public boolean delete(String id) {
        return firestoreService.delete(COLLECTION, id);
    }

    // Méthode utilitaire pour trouver les transactions entre deux dates
    public List<Transaction> findBetweenDates(LocalDateTime start, LocalDateTime end) {
        // Note: Cette méthode nécessite une implémentation spécifique dans FirestoreService
        // pour gérer les requêtes de plage de dates
        try {
            Query query = FirestoreClient.getFirestore()
                    .collection(COLLECTION)
                    .whereGreaterThanOrEqualTo("date", start.toString())
                    .whereLessThanOrEqualTo("date", end.toString());

            List<Transaction> transactions = new ArrayList<>();
            for (QueryDocumentSnapshot doc : query.get().get().getDocuments()) {
                Transaction transaction = doc.toObject(Transaction.class);
                transaction.setId(doc.getId());
                transactions.add(transaction);
            }
            return transactions;
        } catch (Exception e) {
            log.error("Erreur lors de la recherche des transactions par date", e);
            throw new RuntimeException(e);
        }
    }
}
