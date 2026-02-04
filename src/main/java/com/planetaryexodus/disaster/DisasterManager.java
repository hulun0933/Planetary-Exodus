package com.planetaryexodus.disaster;

import com.planetaryexodus.PlanetaryExodusMod;
import com.planetaryexodus.api.events.DisasterTriggeredEvent;
import com.planetaryexodus.core.ModConfig;
import com.planetaryexodus.planet.PlanetStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 灾难管理器
 * 负责管理灾难的触发、执行和效果应用
 */
public class DisasterManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("PlanetaryExodus/Disaster");
    
    // 配置
    private ModConfig.DisasterConfig config;
    
    // 活动灾难
    private final List<ActiveDisaster> activeDisasters = new ArrayList<>();
    private final Map<DisasterType, Long> lastDisasterTimes = new HashMap<>();
    
    // 灾难触发概率缓存
    private final Map<DisasterType, Double> cachedProbabilities = new HashMap<>();
    private long lastProbabilityUpdate = 0;
    
    public DisasterManager() {
        reloadConfig();
        LOGGER.info("灾难管理器初始化完成，共 {} 种灾难类型", DisasterType.values().length);
    }
    
    /**
     * 重新加载配置
     */
    public void reloadConfig() {
        this.config = PlanetaryExodusMod.getInstance().getConfig().getDisaster();
        updateCachedProbabilities();
        LOGGER.info("灾难配置已重新加载，共 {} 种配置灾难", config.disasters.size());
    }
    
    /**
     * 更新灾难系统
     * @param server 服务器实例
     * @param planetStatus 当前行星状态
     */
    public void update(MinecraftServer server, PlanetStatus planetStatus) {
        if (server == null) return;
        
        long currentTime = System.currentTimeMillis();
        
        // 每30秒更新一次概率缓存
        if (currentTime - lastProbabilityUpdate > 30000) {
            updateCachedProbabilities();
            lastProbabilityUpdate = currentTime;
        }
        
        // 更新活动灾难
        updateActiveDisasters(server);
        
        // 检查是否应该触发新灾难
        checkForNewDisasters(server, planetStatus, currentTime);
        
        // 应用灾难效果
        applyDisasterEffects(server);
    }
    
    /**
     * 更新活动灾难
     */
    private void updateActiveDisasters(MinecraftServer server) {
        Iterator<ActiveDisaster> iterator = activeDisasters.iterator();
        while (iterator.hasNext()) {
            ActiveDisaster disaster = iterator.next();
            disaster.update(server);
            
            if (disaster.isFinished()) {
                LOGGER.info("灾难 {} 已结束", disaster.getType().getFormattedString());
                iterator.remove();
            }
        }
    }
    
    /**
     * 检查是否应该触发新灾难
     */
    private void checkForNewDisasters(MinecraftServer server, PlanetStatus planetStatus, long currentTime) {
        // 计算基础触发概率
        double baseChanceMultiplier = calculateBaseChanceMultiplier(planetStatus);
        
        // 检查每种灾难类型的触发条件
        for (ModConfig.DisasterConfig.DisasterTypeConfig disasterConfig : config.disasters) {
            DisasterType type = DisasterType.fromId(disasterConfig.type);
            if (type == null) continue;
            
            // 检查冷却时间
            if (!canTriggerDisaster(type, currentTime)) {
                continue;
            }
            
            // 计算实际触发概率
            double actualChance = disasterConfig.chancePerDay * baseChanceMultiplier;
            
            // 每天检查一次（现实时间）
            if (shouldCheckDisaster(currentTime) && Math.random() < actualChance) {
                triggerDisaster(server, type, disasterConfig);
                lastDisasterTimes.put(type, currentTime);
                LOGGER.info("触发灾难: {} (概率: {}%)", 
                    type.getFormattedString(), actualChance * 100);
            }
        }
    }
    
    /**
     * 触发灾难
     */
    private void triggerDisaster(MinecraftServer server, DisasterType type, 
                                ModConfig.DisasterConfig.DisasterTypeConfig config) {
        // 创建活动灾难
        ActiveDisaster disaster = new ActiveDisaster(type, config);
        activeDisasters.add(disaster);
        
        // 发布灾难触发事件
        PlanetaryExodusMod.getInstance().getEventBus().publish(
            new DisasterTriggeredEvent(type, config.durationMinutes)
        );
        
        // 通知所有玩家
        Text warning = Text.translatable("disaster.warning", 
            type.getDisplayName(),
            Text.translatable("disaster.countermeasure." + type.asString())
        );
        server.getPlayerManager().broadcast(warning, false);
        
        // 播放警告音效和视觉效果
        playDisasterEffects(server, type);
    }
    
    /**
     * 应用灾难效果
     */
    private void applyDisasterEffects(MinecraftServer server) {
        for (ActiveDisaster disaster : activeDisasters) {
            disaster.applyEffects(server);
        }
    }
    
    /**
     * 计算基础触发概率倍率
     */
    private double calculateBaseChanceMultiplier(PlanetStatus status) {
        // 根据行星状态调整触发概率
        double multiplier = 1.0;
        
        switch (status) {
            case STRAINED:
                multiplier *= 1.5;
                break;
            case DEGRADED:
                multiplier *= 3.0;
                break;
            case COLLAPSING:
                multiplier *= 5.0;
                break;
        }
        
        // 考虑最小灾难间隔
        multiplier *= config.chanceMultiplierPerStatusLevel;
        
        return multiplier;
    }
    
    /**
     * 检查是否可以触发灾难
     */
    private boolean canTriggerDisaster(DisasterType type, long currentTime) {
        Long lastTime = lastDisasterTimes.get(type);
        if (lastTime == null) {
            return true;
        }
        
        long hoursSinceLastDisaster = (currentTime - lastTime) / (1000 * 60 * 60);
        return hoursSinceLastDisaster >= config.minDaysBetweenDisasters * 24;
    }
    
    /**
     * 检查是否应该检查灾难触发
     */
    private boolean shouldCheckDisaster(long currentTime) {
        // 每分钟检查一次
        return currentTime % (1000 * 60) < 1000;
    }
    
    /**
     * 更新缓存的概率
     */
    private void updateCachedProbabilities() {
        cachedProbabilities.clear();
        for (ModConfig.DisasterConfig.DisasterTypeConfig disasterConfig : config.disasters) {
            DisasterType type = DisasterType.fromId(disasterConfig.type);
            if (type != null) {
                cachedProbabilities.put(type, disasterConfig.chancePerDay);
            }
        }
    }
    
    /**
     * 播放灾难效果
     */
    private void playDisasterEffects(MinecraftServer server, DisasterType type) {
        // 在实际实现中，这里会：
        // 1. 播放音效
        // 2. 显示粒子效果
        // 3. 添加屏幕效果
        // 4. 震动相机等
        
        LOGGER.debug("播放灾难效果: {}", type.getFormattedString());
        
        switch (type) {
            case EARTHQUAKE:
                // 地面震动效果
                break;
            case SUPER_STORM:
                // 风暴视觉效果
                break;
            case RADIATION:
                // 辐射视觉效果
                break;
            case ACID_RAIN:
                // 酸雨视觉效果
                break;
        }
    }
    
    /**
     * 获取活动灾难列表
     */
    public List<ActiveDisaster> getActiveDisasters() {
        return Collections.unmodifiableList(activeDisasters);
    }
    
    /**
     * 获取指定类型的活动灾难
     */
    public List<ActiveDisaster> getActiveDisasters(DisasterType type) {
        List<ActiveDisaster> result = new ArrayList<>();
        for (ActiveDisaster disaster : activeDisasters) {
            if (disaster.getType() == type) {
                result.add(disaster);
            }
        }
        return result;
    }
    
    /**
     * 检查是否有活动灾难
     */
    public boolean hasActiveDisasters() {
        return !activeDisasters.isEmpty();
    }
    
    /**
     * 获取灾难数量
     */
    public int getDisasterCount() {
        return activeDisasters.size();
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        activeDisasters.clear();
        lastDisasterTimes.clear();
        cachedProbabilities.clear();
        LOGGER.info("灾难管理器资源已清理");
    }
    
    /**
     * 活动灾难类
     */
    public static class ActiveDisaster {
        private final DisasterType type;
        private final ModConfig.DisasterConfig.DisasterTypeConfig config;
        private final long startTime;
        private long endTime;
        private boolean isFinished = false;
        
        public ActiveDisaster(DisasterType type, ModConfig.DisasterConfig.DisasterTypeConfig config) {
            this.type = type;
            this.config = config;
            this.startTime = System.currentTimeMillis();
            this.endTime = startTime + config.durationMinutes * 60 * 1000L;
        }
        
        /**
         * 更新灾难状态
         */
        public void update(MinecraftServer server) {
            if (System.currentTimeMillis() >= endTime) {
                isFinished = true;
                
                // 通知玩家灾难结束
                Text endMessage = Text.translatable("disaster.ended", type.getDisplayName());
                server.getPlayerManager().broadcast(endMessage, false);
            }
        }
        
        /**
         * 应用灾难效果
         */
        public void applyEffects(MinecraftServer server) {
            if (isFinished) return;
            
            // 应用伤害效果
            if (config.damagePerSecond > 0) {
                applyDamage(server, config.damagePerSecond);
            }
            
            // 应用方块腐蚀效果
            if (config.blockCorrosionChance > 0) {
                applyBlockCorrosion(server, config.blockCorrosionChance);
            }
            
            // 应用方块破坏效果
            if (config.blockDamageChance > 0) {
                applyBlockDamage(server, config.blockDamageChance);
            }
        }
        
        /**
         * 应用伤害效果
         */
        private void applyDamage(MinecraftServer server, double damagePerSecond) {
            server.getPlayerManager().getPlayerList().forEach(player -> {
                if (isPlayerAffected(player)) {
                    // 每5秒应用一次伤害
                    if (server.getTicks() % 100 == 0) {
                        float damage = (float) (damagePerSecond * 5);
                        // player.damage(player.getDamageSources().magic(), damage);
                        LOGGER.debug("玩家 {} 受到 {} 伤害: {}", 
                            player.getName().getString(), type.getDisplayName().getString(), damage);
                    }
                }
            });
        }
        
        /**
         * 应用方块腐蚀效果
         */
        private void applyBlockCorrosion(MinecraftServer server, double chance) {
            // 在实际实现中，这里会腐蚀特定类型的方块
            // 例如：酸雨腐蚀非石质/金属方块
            if (server.getTicks() % 200 == 0 && Math.random() < chance) {
                LOGGER.debug("应用方块腐蚀效果: {}", type.getFormattedString());
            }
        }
        
        /**
         * 应用方块破坏效果
         */
        private void applyBlockDamage(MinecraftServer server, double chance) {
            // 在实际实现中，这里会破坏特定类型的方块
            // 例如：地震破坏非基岩方块
            if (server.getTicks() % 200 == 0 && Math.random() < chance) {
                LOGGER.debug("应用方块破坏效果: {}", type.getFormattedString());
            }
        }
        
        /**
         * 检查玩家是否受影响
         */
        private boolean isPlayerAffected(net.minecraft.entity.player.PlayerEntity player) {
            // 简化实现：检查玩家是否在室外
            switch (type) {
                case RADIATION:
                case ACID_RAIN:
                case SUPER_STORM:
                    return player.getWorld().isSkyVisible(player.getBlockPos());
                case EARTHQUAKE:
                    return true; // 地震影响所有玩家
                default:
                    return false;
            }
        }
        
        public DisasterType getType() {
            return type;
        }
        
        public long getStartTime() {
            return startTime;
        }
        
        public long getEndTime() {
            return endTime;
        }
        
        public long getRemainingTime() {
            return Math.max(0, endTime - System.currentTimeMillis());
        }
        
        public boolean isFinished() {
            return isFinished;
        }
        
        public int getDurationMinutes() {
            return config.durationMinutes;
        }
        
        @Override
        public String toString() {
            return String.format("ActiveDisaster{type=%s, start=%s, remaining=%dms}", 
                type, new java.util.Date(startTime), getRemainingTime());
        }
    }
}