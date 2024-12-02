package com.backMob.Entity;

import com.backMob.Services.FirestoreService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User extends FirestoreService.BaseEntity {
    private String code;
    private String mail;
    private String nom;
    private String prenom;
    private double solde;
    private String telephone;
    private String type;
}
