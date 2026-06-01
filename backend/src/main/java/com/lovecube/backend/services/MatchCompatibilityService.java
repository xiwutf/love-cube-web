package com.lovecube.backend.services;

import com.lovecube.backend.entity.MatchCompatibilityAnswer;
import com.lovecube.backend.entity.MatchCompatibilityQuestion;
import com.lovecube.backend.repository.MatchCompatibilityAnswerRepository;
import com.lovecube.backend.repository.MatchCompatibilityQuestionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class MatchCompatibilityService {

    private static final int QUESTIONS_PER_MATCH = 5;
    private static final Set<String> VALID_OPTIONS = Set.of("A", "B", "C", "D");

    private final MatchCompatibilityQuestionRepository questionRepository;
    private final MatchCompatibilityAnswerRepository answerRepository;

    public MatchCompatibilityService(
            MatchCompatibilityQuestionRepository questionRepository,
            MatchCompatibilityAnswerRepository answerRepository
    ) {
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
    }

    public Map<String, Object> getSession(Long userId, Long peerUserId) {
        if (peerUserId == null || peerUserId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "参数不合法");
        }
        String pairKey = MatchIcebreakerService.buildPairKey(userId, peerUserId);
        List<MatchCompatibilityQuestion> picked = pickQuestions(
                questionRepository.findByEnabledOrderBySortNoAsc(1), pairKey);

        List<MatchCompatibilityAnswer> myAnswers = answerRepository.findByMatchPairKeyAndUserId(pairKey, userId);
        List<MatchCompatibilityAnswer> peerAnswers = answerRepository.findByMatchPairKeyAndUserId(pairKey, peerUserId);

        Map<Long, String> myMap = new HashMap<>();
        myAnswers.forEach(a -> myMap.put(a.getQuestionId(), a.getSelectedOption()));
        Map<Long, String> peerMap = new HashMap<>();
        peerAnswers.forEach(a -> peerMap.put(a.getQuestionId(), a.getSelectedOption()));

        boolean myDone = myAnswers.size() >= picked.size();
        boolean peerDone = peerAnswers.size() >= picked.size();
        Integer score = (myDone && peerDone) ? calculateScore(picked, myMap, peerMap) : null;

        List<Map<String, Object>> questions = new ArrayList<>();
        for (MatchCompatibilityQuestion q : picked) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", q.getId());
            row.put("questionText", q.getQuestionText());
            row.put("options", List.of(
                    optionRow("A", q.getOptionA()),
                    optionRow("B", q.getOptionB()),
                    optionRow("C", q.getOptionC()),
                    optionRow("D", q.getOptionD())
            ));
            row.put("myOption", myMap.get(q.getId()));
            row.put("peerOption", (myDone && peerDone) ? peerMap.get(q.getId()) : null);
            row.put("matched", (myDone && peerDone)
                    && Objects.equals(myMap.get(q.getId()), peerMap.get(q.getId())));
            questions.add(row);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pairKey", pairKey);
        result.put("peerUserId", peerUserId);
        result.put("questions", questions);
        result.put("myCompleted", myDone);
        result.put("peerCompleted", peerDone);
        result.put("compatibilityScore", score);
        result.put("canViewResult", myDone && peerDone);
        return result;
    }

    @Transactional
    public Map<String, Object> submitAnswers(Long userId, Long peerUserId, List<Map<String, Object>> answers) {
        if (peerUserId == null || answers == null || answers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请完成选择");
        }
        String pairKey = MatchIcebreakerService.buildPairKey(userId, peerUserId);
        int saved = 0;
        for (Map<String, Object> item : answers) {
            Long questionId = parseLong(item.get("questionId"));
            String option = String.valueOf(item.getOrDefault("selectedOption", "")).trim().toUpperCase();
            if (questionId == null || !VALID_OPTIONS.contains(option)) {
                continue;
            }
            Optional<MatchCompatibilityAnswer> existing = answerRepository
                    .findByMatchPairKeyAndUserIdAndQuestionId(pairKey, userId, questionId);
            MatchCompatibilityAnswer answer = existing.orElseGet(MatchCompatibilityAnswer::new);
            answer.setMatchPairKey(pairKey);
            answer.setUserId(userId);
            answer.setQuestionId(questionId);
            answer.setSelectedOption(option);
            answerRepository.save(answer);
            saved++;
        }
        return Map.of("saved", saved, "session", getSession(userId, peerUserId));
    }

    private int calculateScore(List<MatchCompatibilityQuestion> questions,
                               Map<Long, String> myMap,
                               Map<Long, String> peerMap) {
        if (questions.isEmpty()) {
            return 0;
        }
        int matched = 0;
        for (MatchCompatibilityQuestion q : questions) {
            if (Objects.equals(myMap.get(q.getId()), peerMap.get(q.getId()))) {
                matched++;
            }
        }
        return (int) Math.round(matched * 100.0 / questions.size());
    }

    private Map<String, String> optionRow(String key, String text) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("key", key);
        row.put("text", text);
        return row;
    }

    private List<MatchCompatibilityQuestion> pickQuestions(List<MatchCompatibilityQuestion> all, String pairKey) {
        if (all.size() <= QUESTIONS_PER_MATCH) {
            return all;
        }
        List<MatchCompatibilityQuestion> copy = new ArrayList<>(all);
        Collections.shuffle(copy, new Random(pairKey.hashCode() + 17));
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
