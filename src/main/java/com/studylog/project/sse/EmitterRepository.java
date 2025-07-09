package com.studylog.project.sse;

import com.studylog.project.user.UserEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class EmitterRepository {
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();
    public boolean existsByUser(Long userId) {
        return emitters.containsKey(userId);
    }
    public List<SseEmitter> findByUserId(Long userId) {
        return emitters.getOrDefault(userId, new ArrayList<>());
    }
    public void save(Long userId, SseEmitter emitter) {
        //유저에 해당하는 emitter 리스트 반환 후 추가
        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
    }
    public void deleteByUserId(Long userId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(userId);
        if (list != null) {
            list.remove(emitter);
            if(list.isEmpty()) {emitters.remove(userId);}
        }
    }
}
