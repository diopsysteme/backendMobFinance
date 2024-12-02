package com.backMob.Repository;

import com.backMob.Entity.Schedule;
import com.backMob.Services.FirestoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class ScheduleRepository {
    private final FirestoreService firestoreService;
    private static final String COLLECTION = "scheduled_transfers";

    public ScheduleRepository(FirestoreService firestoreService) {
        this.firestoreService = firestoreService;
    }

    public Schedule create(Schedule schedule) {
        return firestoreService.create(COLLECTION, schedule, Schedule.class);
    }

    public List<Schedule> findAll() {
        return firestoreService.getAll(COLLECTION, Schedule.class);
    }

    public Schedule findById(String id) {
        return firestoreService.findById(COLLECTION, id, Schedule.class);
    }

    public List<Schedule> findByUserId(String userId) {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("userId", userId);
        return firestoreService.findByFields(COLLECTION, criteria, Schedule.class);
    }

    public List<Schedule> findActiveSchedules() {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("active", true);
        return firestoreService.findByFields(COLLECTION, criteria, Schedule.class);
    }

    public List<Schedule> findByFrequency(String frequency) {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("frequency", frequency);
        return firestoreService.findByFields(COLLECTION, criteria, Schedule.class);
    }

    public Schedule update(String id, Map<String, Object> updates) {
        return firestoreService.update(COLLECTION, id, updates, Schedule.class);
    }

    public boolean delete(String id) {
        return firestoreService.delete(COLLECTION, id);
    }
}