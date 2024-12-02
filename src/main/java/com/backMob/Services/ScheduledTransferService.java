package com.backMob.Services;

import com.backMob.Entity.Schedule;
import com.backMob.Entity.User;
import com.backMob.Repository.ScheduleRepository;
import com.backMob.Repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ScheduledTransferService {
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;

    public ScheduledTransferService(ScheduleRepository scheduleRepository, UserRepository userRepository, TransactionService transactionService) {
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
        this.transactionService = transactionService;
    }

    @Scheduled(fixedRate = 80000) // Exécution toutes les 800 millisecondes
    public void executeScheduledTransfers() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Début de l'exécution des transferts planifiés à {}", now);

        // Récupérer les transferts actifs
        List<Schedule> schedulesToExecute = scheduleRepository.findActiveSchedules();
        log.info("Nombre de transferts planifiés à exécuter : {}", schedulesToExecute.size());

        for (Schedule schedule : schedulesToExecute) {
            try {
                log.info("Traitement du transfert planifié avec ID: {}", schedule.getId());

                // Gestion des planifications sans nextExecution
                if (schedule.getNextExecution() == null || schedule.getNextExecution().isEmpty()) {
                    // Initialiser la prochaine exécution à maintenant pour une première exécution immédiate
                    schedule.setNextExecution(now.format(DateTimeFormatter.ISO_DATE_TIME));
                    scheduleRepository.update(schedule.getId(),
                            Map.of("nextExecution", schedule.getNextExecution()));
                    log.info("Initialisation de nextExecution pour le planning ID: {} à {}",
                            schedule.getId(), schedule.getNextExecution());
                }

                // Conversion de la chaîne nextExecution en LocalDateTime
                LocalDateTime nextExecutionDate;
                try {
                    nextExecutionDate = LocalDateTime.parse(schedule.getNextExecution(),
                            DateTimeFormatter.ISO_DATE_TIME);
                } catch (Exception e) {
                    log.error("Format de date invalide pour le planning ID: {} (nextExecution: {}).",
                            schedule.getId(), schedule.getNextExecution());
                    // Réinitialiser la prochaine exécution en cas de date invalide
                    nextExecutionDate = now;
                    schedule.setNextExecution(now.format(DateTimeFormatter.ISO_DATE_TIME));
                    scheduleRepository.update(schedule.getId(),
                            Map.of("nextExecution", schedule.getNextExecution()));
                }

                // Vérification de la date de la prochaine exécution
                if (nextExecutionDate.isAfter(now)) {
                    log.info("Le planning ID {} n'est pas encore prêt pour l'exécution (nextExecution : {}).",
                            schedule.getId(), nextExecutionDate);
                    continue;
                }

                // Récupérer l'expéditeur
                User sender = userRepository.findById(schedule.getUserId());
                if (sender == null) {
                    log.warn("Expéditeur introuvable pour le planning ID: {}", schedule.getId());
                    continue;
                }

                log.info("Expéditeur trouvé: {}", sender.getPrenom());

                // Exécution des transactions pour chaque contact
                boolean allTransactionsSuccessful = true;
                for (String contactPhone : schedule.getContacts()) {
                    User receiver = userRepository.findByTelephone(contactPhone);
                    if (receiver != null) {
                        log.info("Receveur trouvé pour le numéro: {}", contactPhone);
                        try {
                            transactionService.processTransaction(
                                    sender, receiver, schedule.getType(), schedule.getMontant(), "scheduled"
                            );
                            log.info("Transaction exécutée entre {} et {}",
                                    sender.getPrenom(), receiver.getPrenom());
                        } catch (Exception e) {
                            log.error("Échec de la transaction pour le contact: {}", contactPhone, e);
                            allTransactionsSuccessful = false;
                        }
                    } else {
                        log.error("Receveur introuvable pour le numéro: {}", contactPhone);
                        allTransactionsSuccessful = false;
                    }
                }

                // Mise à jour de la prochaine exécution seulement si toutes les transactions ont réussi
                if (allTransactionsSuccessful) {
                    String nextExecution = calculateNextExecution(schedule);
                    schedule.setNextExecution(nextExecution);
                    log.info("Prochaine exécution prévue pour le planning ID {} : {}",
                            schedule.getId(), nextExecution);
                    scheduleRepository.update(schedule.getId(),
                            Map.of("nextExecution", nextExecution));
                } else {
                    log.warn("Certaines transactions ont échoué pour le planning ID: {}. " +
                            "La prochaine exécution sera retentée plus tard.", schedule.getId());
                }

            } catch (Exception e) {
                log.error("Erreur lors du traitement du transfert pour le planning ID: {}",
                        schedule.getId(), e);
            }
        }

        log.info("Fin de l'exécution des transferts planifiés à {}", LocalDateTime.now());
    }

    private String calculateNextExecution(Schedule schedule) {
        log.info("Calcul de la prochaine exécution pour le planning ID: {}", schedule.getId());

        LocalDateTime nextExecution = LocalDateTime.now();
        switch (schedule.getFrequency()) {
            case "DAILY":
                nextExecution = nextExecution.plusDays(1);
                break;
            case "WEEKLY":
                nextExecution = nextExecution.plusWeeks(1);
                break;
            case "MONTHLY":
                nextExecution = nextExecution.plusMonths(1);
                break;
            case "EVERY_X_DAYS":
                nextExecution = nextExecution.plusDays(schedule.getIntervalDays());
                break;
            default:
                throw new IllegalArgumentException("Type de fréquence inconnu: " + schedule.getFrequency());
        }

        // Retourner la prochaine exécution sous forme de chaîne ISO-8601
        return nextExecution.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

}