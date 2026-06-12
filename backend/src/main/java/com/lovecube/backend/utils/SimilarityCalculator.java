package com.lovecube.backend.utils;

import com.lovecube.backend.models.User;

import java.util.Objects;


/*
用于计算用户之间的相似度。
 */
public class SimilarityCalculator
{
    public static double calculateSimilarity(User currentUser, User otherUser)
    {
        int currentAge = currentUser.getAge() == null ? 0 : currentUser.getAge();
        int otherAge = otherUser.getAge() == null ? 0 : otherUser.getAge();
        double ageSimilarity = 1 - (Math.abs(currentAge - otherAge) / 100.0);

        // 防止 location 或 occupation 为 null
        double citySimilarity = Objects.equals(currentUser.getLocation(), otherUser.getLocation()) ? 1 : 0;
        double occupationSimilarity = Objects.equals(currentUser.getOccupation(), otherUser.getOccupation()) ? 1 : 0;

        return 0.3 * ageSimilarity + 0.3 * citySimilarity + 0.4 * occupationSimilarity;
    }
}
