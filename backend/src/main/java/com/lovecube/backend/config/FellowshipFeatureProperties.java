package com.lovecube.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "lovecube.fellowship")
public class FellowshipFeatureProperties {
    /**
     * 是否启用 VIP 付费能力（喜欢/访客名单锁定、滑卡付费引导等）。
     * 暂关时非 VIP 用户也可查看完整互动名单。
     */
    private boolean vipCommerceEnabled = false;
}
