package com.backMob.Repository;

import com.backMob.Entity.User;
import com.backMob.Services.FirestoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class UserRepository {
    private final FirestoreService firestoreService;
    private static final String COLLECTION = "users";

    public UserRepository(FirestoreService firestoreService) {
        this.firestoreService = firestoreService;
    }

    public User create(User user) {
        return firestoreService.create(COLLECTION, user, User.class);
    }

    public List<User> findAll() {
        return firestoreService.getAll(COLLECTION, User.class);
    }

    public User findById(String id) {
        return firestoreService.findById(COLLECTION, id, User.class);
    }

    public User findByTelephone(String telephone) {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("telephone", telephone);
        List<User> users = firestoreService.findByFields(COLLECTION, criteria, User.class);
        return users.isEmpty() ? null : users.get(0);
    }

    public User findByMail(String mail) {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("mail", mail);
        List<User> users = firestoreService.findByFields(COLLECTION, criteria, User.class);
        return users.isEmpty() ? null : users.get(0);
    }

    public List<User> findByType(String type) {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("type", type);
        return firestoreService.findByFields(COLLECTION, criteria, User.class);
    }

    public User update(String id, Map<String, Object> updates) {
        return firestoreService.update(COLLECTION, id, updates, User.class);
    }

    public boolean delete(String id) {
        return firestoreService.delete(COLLECTION, id);
    }

    public User updateSolde(String id, double newSolde) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("solde", newSolde);
            return firestoreService.update(COLLECTION, id, updates, User.class);
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du solde pour l'utilisateur avec ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Impossible de mettre à jour le solde", e);
        }
    }

}