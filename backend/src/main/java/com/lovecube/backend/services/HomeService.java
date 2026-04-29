package com.lovecube.backend.services;

import com.lovecube.backend.dto.UserFilterDTO;
import com.lovecube.backend.entity.Banner;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.BannerRepository;
import com.lovecube.backend.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HomeService {
    private final BannerRepository bannerRepository;
    private final UserRepository userRepository;
    private final UnifiedProfileService unifiedProfileService;

    public HomeService(
            BannerRepository bannerRepository,
            UserRepository userRepository,
            UnifiedProfileService unifiedProfileService
    ) {
        this.bannerRepository = bannerRepository;
        this.userRepository = userRepository;
        this.unifiedProfileService = unifiedProfileService;
    }

    public List<Banner> getBanners() {
        return bannerRepository.findByIsActiveTrueOrderBySortAsc();
    }

    public List<Map<String, Object>> getRecommends() {
        return userRepository.findRandomVisibleFellowshipUsers(10).stream()
                .filter(this::isVisibleInMatchPool)
                .map(this::convertUserToMap)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getNewcomers() {
        return userRepository.findNewcomersVisibleFellowshipUsers(10).stream()
                .filter(this::isVisibleInMatchPool)
                .map(this::convertUserToMap)
                .collect(Collectors.toList());
    }

    public List<User> searchUsers(String keyword, int page, int size) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findAll(PageRequest.of(page, size)).getContent();
        }
        String k = keyword.toLowerCase();
        return userRepository.findAll(PageRequest.of(page, size)).getContent().stream()
                .filter(user ->
                        (user.getUsername() != null && user.getUsername().toLowerCase().contains(k)) ||
                                (user.getLocation() != null && user.getLocation().toLowerCase().contains(k)) ||
                                (user.getOccupation() != null && user.getOccupation().toLowerCase().contains(k)))
                .collect(Collectors.toList());
    }

    public List<User> filterUsers(UserFilterDTO filterDTO) {
        return userRepository.findByAgeBetweenAndGenderAndLocation(
                filterDTO.getAgeRange().get(0),
                filterDTO.getAgeRange().get(1),
                "男".equals(filterDTO.getGender()) ? 1 : 2,
                filterDTO.getRegion()
        );
    }

    private Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> merged = unifiedProfileService.buildLegacyUserPayload(user);
        merged.put("userid", user.getUserid());
        merged.put("username", merged.getOrDefault("nickname", user.getUsername()));
        merged.put("profilePhoto", merged.getOrDefault("avatar", user.getProfilePhoto()));
        merged.put("avatar", merged.getOrDefault("avatar", user.getProfilePhoto()));
        merged.put("location", merged.getOrDefault("location", user.getLocation()));
        merged.put("occupation", merged.getOrDefault("occupation", user.getOccupation()));
        merged.put("bio", merged.getOrDefault("bio", user.getBio()));
        merged.put("lifePhotos", merged.getOrDefault("photos", List.of()));
        return merged;
    }

    private boolean isVisibleInMatchPool(User user) {
        return user != null
                && !"DISABLED".equalsIgnoreCase(user.getUserStatus())
                && Boolean.TRUE.equals(user.getFellowshipEnabled())
                && Boolean.TRUE.equals(user.getFellowshipMatchVisible());
    }
}
