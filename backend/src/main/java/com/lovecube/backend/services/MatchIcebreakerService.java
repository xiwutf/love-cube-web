package com.lovecube.backend.services;

import com.lovecube.backend.entity.MatchIcebreakerAnswer;
import com.lovecube.backend.entity.MatchIcebreakerQuestion;
import com.lovecube.backend.repository.MatchIcebreakerAnswerRepository;
import com.lovecube.backend.repository.MatchIcebreakerQuestionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class MatchIcebreakerService {

    private static final int QUESTIONS_PER_MATCH = 3;

    private final MatchIcebreakerQuestionRepository questionRepository;
    private final MatchIcebreakerAnswerRepository answerRepository;

    public MatchIcebreakerService(
            MatchIcebreakerQuestionRepository questionRepository,
            MatchIcebreakerAnswerRepository answerRepository
    ) {
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
    }

    public static String buildPairKey(Long userIdA, Long userIdB) {
        long a = Math.min(userIdA, userIdB);
        long b = Math.max(userIdA, userIdB);
        return a + "_" + b;
    }

    public Map<String, Object> getSession(Long userId, Long peerUserId) {
        if (peerUserId == null || peerUserId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "参数不合法");
        }
        String pairKey = buildPairKey(userId, peerUserId);
        List<MatchIcebreakerQuestion> all = questionRepository.findByEnabledOrderBySortNoAsc(1);
        List<MatchIcebreakerQuestion> picked = pickQuestions(all, pairKey);

        List<MatchIcebreakerAnswer> myAnswers = answerRepository.findByMatchPairKeyAndUserId(pairKey, userId);
        List<MatchIcebreakerAnswer> peerAnswers = answerRepository.findByMatchPairKeyAndUserId(pairKey, peerUserId);

        Map<Long, String> myMap = new HashMap<>();
        myAnswers.forEach(a -> myMap.put(a.getQuestionId(), a.getAnswerText()));
        Map<Long, String> peerMap = new HashMap<>();
        peerAnswers.forEach(a -> peerMap.put(a.getQuestionId(), a.getAnswerText()));

        boolean myDone = myAnswers.size() >= picked.size();
        boolean peerDone = peerAnswers.size() >= picked.size();

        List<Map<String, Object>> questions = new ArrayList<>();
        for (MatchIcebreakerQuestion q : picked) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", q.getId());
            row.put("questionText", q.getQuestionText());
            row.put("myAnswer", myMap.get(q.getId()));
            row.put("peerAnswer", (myDone && peerDone) ? peerMap.get(q.getId()) : null);
            questions.add(row);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pairKey", pairKey);
        result.put("peerUserId", peerUserId);
        result.put("questions", questions);
        result.put("myCompleted", myDone);
        result.put("peerCompleted", peerDone);
        result.put("canViewPeerAnswers", myDone && peerDone);
        return result;
    }

    @Transactional
    public Map<String, Object> submitAnswers(Long userId, Long peerUserId, List<Map<String, Object>> answers) {
        if (peerUserId == null || answers == null || answers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请填写回答");
        }
        String pairKey = buildPairKey(userId, peerUserId);
        int saved = 0;
        for (Map<String, Object> item : answers) {
            Long questionId = parseLong(item.get("questionId"));
            String text = String.valueOf(item.getOrDefault("answerText", "")).trim();
            if (questionId == null || text.isBlank() || text.length() > 500) continue;

            Optional<MatchIcebreakerAnswer> existing = answerRepository
                    .findByMatchPairKeyAndUserIdAndQuestionId(pairKey, userId, questionId);
            MatchIcebreakerAnswer answer = existing.orElseGet(MatchIcebreakerAnswer::new);
            answer.setMatchPairKey(pairKey);
            answer.setUserId(userId);
            answer.setQuestionId(questionId);
            answer.setAnswerText(text);
            answerRepository.save(answer);
            saved++;
        }
        return Map.of("saved", saved, "session", getSession(userId, peerUserId));
    }

    private List<MatchIcebreakerQuestion> pickQuestions(List<MatchIcebreakerQuestion> all, String pairKey) {
        if (all.size() <= QUESTIONS_PER_MATCH) {
            return all;
        }
        List<MatchIcebreakerQuestion> copy = new ArrayList<>(all);
        int seed = pairKey.hashCode();
        Collections.shuffle(copy, new Random(seed));
        return copy.subList(0, QUESTIONS_PER_MATCH);
    }

    private Long parseLong(Object value) {
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }
}
