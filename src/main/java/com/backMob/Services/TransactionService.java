package com.backMob.Services;

import com.backMob.Entity.Transaction;
import com.backMob.Entity.User;
import com.backMob.Repository.TransactionRepository;
import com.backMob.Repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public void processTransaction(User sender, User receiver, String type, double montant, String motif) {
        double frais = calculateFees(type, montant);
        double soldeSender = sender.getSolde();
        double soldeReceiver = receiver.getSolde();

        if (soldeSender < montant + frais) {
            log.error("Transaction échouée : Solde insuffisant pour l'utilisateur: " + sender.getId());
            saveFailedTransaction(sender, receiver, montant, type, frais, soldeSender, soldeReceiver, motif);
            return;
        }

        // Mise à jour des soldes en fonction du type
        switch (type) {
            case "TRANSFERT":
                sender.setSolde(soldeSender - (montant + frais));
                receiver.setSolde(soldeReceiver + montant);
                break;
            case "RETRAIT":
                receiver.setSolde(soldeReceiver - (montant + frais));
                break;
            case "DEPOT":
                sender.setSolde(soldeSender - (montant + frais));
                receiver.setSolde(soldeReceiver + montant);
                break;
            default:
                throw new IllegalArgumentException("Type de transaction invalide: " + type);
        }

        // Mise à jour des soldes dans la base de données
        userRepository.updateSolde(sender.getId(),soldeSender - (montant + frais));
        userRepository.updateSolde(receiver.getId(),soldeReceiver - (montant + frais));

        // Enregistrer la transaction
        Transaction transaction = new Transaction(
                 LocalDateTime.now(), frais, sender.getId(), receiver.getId(), montant,
                motif, sender.getSolde(), receiver.getSolde(), "SUCCESS", type
        );
        saveTransaction(transaction);

        log.info("Transaction réussie entre {} et {} pour un montant de {}", sender.getId(), receiver.getId(), montant);
    }

        private void saveFailedTransaction(User sender, User receiver, double montant, String type, double frais, double soldeSender, double soldeReceiver, String motif) {
            Transaction failedTransaction = new Transaction(
                     LocalDateTime.now(), frais, sender.getId(), receiver.getId(), montant,
                    motif, soldeSender, soldeReceiver, "FAILED", type
            );
        saveTransaction(failedTransaction);
    }

    private void saveTransaction(Transaction transaction) {
        transactionRepository.create(transaction);
    }

    private double calculateFees(String type, double montant) {
        // Logique pour calculer les frais
        return montant * 0.02; // Exemple : 2% du montant
    }
}