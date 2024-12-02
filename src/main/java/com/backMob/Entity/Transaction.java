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

public class Transaction extends FirestoreService.BaseEntity {


        private String date;
    private double montant;
    private String senderId;
    private String receiverId;
    private double frais;
    private String motif;
    private double soldeSender;
    private double soldeReceiver;
    private String status;
    private String type;

    // Constructeur
    public Transaction(LocalDateTime date, double frais, String senderId, String receiverId,
                       double montant, String motif, double soldeSender, double soldeReceiver,
                       String status, String type) {
        this.date = date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);  // Convertir LocalDateTime en String
        this.montant = montant;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.frais = frais;
        this.motif = motif;
        this.soldeSender = soldeSender;
        this.soldeReceiver = soldeReceiver;
        this.status = status;
        this.type = type;
    }

    // Getter et Setter

}