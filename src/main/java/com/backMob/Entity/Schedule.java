package com.backMob.Entity;

import com.backMob.Services.FirestoreService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Schedule extends FirestoreService.BaseEntity {
    private boolean active;
    private List<String> contacts;
    private String frequency;
    private int intervalDays;
    private double montant;
    private String type;
    private String userId;
    private String nextExecution;  // Utilisation d'un String pour stocker LocalDateTime

    // Constructeur
    public Schedule(boolean active, List<String> contacts, String frequency, int intervalDays,
                    double montant, String type, String userId, LocalDateTime nextExecution) {
        this.active = active;
        this.contacts = contacts;
        this.frequency = frequency;
        this.intervalDays = intervalDays;
        this.montant = montant;
        this.type = type;
        this.userId = userId;
        // Conversion de LocalDateTime en String
        this.nextExecution = nextExecution.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

}
