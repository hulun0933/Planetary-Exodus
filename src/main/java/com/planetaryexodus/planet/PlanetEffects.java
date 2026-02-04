package com.planetaryexodus.planet;

import com.planetaryexodus.core.ModConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 行星效果应用器
 * 负责根据行星状态应用各种游戏效果
 */
public class PlanetEffects {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("PlanetaryExodus/Effects");
    
    /**
     * 应用行星状态效果
     * @param server 服务器实例
     * @param status 当前行星状态
     * @param config 行星配置
     */
    public void applyEffects(MinecraftServer server, PlanetStatus status, ModConfig.PlanetConfig config) {
        if (server == null) return;
        
        Map<String, Double> effects = getEffectsForStatus(status, config);
        if (effects == null || effects.isEmpty()) {
            return;
        }
        
        // 应用作物生长效果
        applyCropGrowthEffect(server, effects);
        
        // 应用能源效率效果
        applyEnergyEfficiencyEffect(server, effects);
        
        // 应用怪物生成效果
        applyMonsterSpawnEffect(server, effects);
        
        // 应用特殊效果（如酸雨、辐射等）
        applySpecialEffects(server, status, effects);
        
        LOGGER.debug("应用行星状态效果: {} ({}个效果)", status.getFormattedString(), effects.size());
    }
    
    /**
     * 获取指定状态的效果配置
     */
    private Map<String, Double> getEffectsForStatus(PlanetStatus status, ModConfig.PlanetConfig config) {
        switch (status) {
            case STABLE:
                return config.stableEffects;
            case STRAINED:
                return config.strainedEffects;
            case DEGRADED:
                return config.degradedEffects;
            case COLLAPSING:
                return config.collapsingEffects;
            default:
                return config.stableEffects;
        }
    }
    
    /**
     * 应用作物生长效果
     */
    private void applyCropGrowthEffect(MinecraftServer server, Map<String, Double> effects) {
        if (effects.containsKey("crop_growth_multiplier")) {
            double multiplier = effects.get("crop_growth_multiplier");
            
            for (ServerWorld world : server.getWorlds()) {
                // 调整随机刻速度来模拟作物生长速度变化
                GameRules.IntRule randomTickSpeed = world.getGameRules().get(GameRules.RANDOM_TICK_SPEED);
                int baseSpeed = 3; // 默认值
                int adjustedSpeed = (int) (baseSpeed * multiplier);
                
                // 确保在合理范围内
                adjustedSpeed = Math.max(1, Math.min(10, adjustedSpeed));
                
                // 设置游戏规则（这会影响所有随机刻，不仅仅是作物）
                // 注意：在实际实现中，可能需要更精细的控制
                world.getGameRules().get(GameRules.RANDOM_TICK_SPEED).set(adjustedSpeed, server);
            }
        }
    }
    
    /**
     * 应用能源效率效果
     */
    private void applyEnergyEfficiencyEffect(MinecraftServer server, Map<String, Double> effects) {
        if (effects.containsKey("energy_efficiency")) {
            double efficiency = effects.get("energy_efficiency");
            
            // 这里可以修改熔炉等设备的处理速度
            // 实际实现需要与具体的能源系统集成
            LOGGER.debug("能源效率调整为: {}%", efficiency * 100);
        }
    }
    
    /**
     * 应用怪物生成效果
     */
    private void applyMonsterSpawnEffect(MinecraftServer server, Map<String, Double> effects) {
        if (effects.containsKey("monster_spawn_multiplier")) {
            double multiplier = effects.get("monster_spawn_multiplier");
            
            for (ServerWorld world : server.getWorlds()) {
                // 调整怪物生成限制
                // 实际实现需要修改怪物生成算法
                LOGGER.debug("世界 {} 的怪物生成倍率: {}", world.getRegistryKey().getValue(), multiplier);
            }
        }
    }
    
    /**
     * 应用特殊效果
     */
    private void applySpecialEffects(MinecraftServer server, PlanetStatus status, Map<String, Double> effects) {
        // 酸雨效果
        if (effects.containsKey("acid_rain_chance")) {
            double chance = effects.get("acid_rain_chance");
            if (chance > 0 && shouldTriggerEffect(server, chance)) {
                triggerAcidRain(server);
            }
        }
        
        // 辐射伤害效果
        if (effects.containsKey("radiation_damage_per_second")) {
            double damage = effects.get("radiation_damage_per_second");
            if (damage > 0) {
                applyRadiationDamage(server, damage);
            }
        }
        
        // 根据状态应用额外效果
        switch (status) {
            case DEGRADED:
                applyDegradedEffects(server);
                break;
            case COLLAPSING:
                applyCollapsingEffects(server);
                break;
        }
    }
    
    /**
     * 触发酸雨效果
     */
    private void triggerAcidRain(MinecraftServer server) {
        LOGGER.info("☔ 触发酸雨效果");
        
        for (ServerWorld world : server.getWorlds()) {
            // 在实际实现中，这里会：
            // 1. 改变天气为雨天
            // 2. 添加酸雨效果（腐蚀方块、伤害暴露的玩家等）
            // 3. 播放音效和粒子效果
            
            // 临时实现：记录日志
            if (world.isRaining()) {
                LOGGER.debug("世界 {} 正在下酸雨", world.getRegistryKey().getValue());
            }
        }
    }
    
    /**
     * 应用辐射伤害
     */
    private void applyRadiationDamage(MinecraftServer server, double damagePerSecond) {
        // 在实际实现中，这里会：
        // 1. 检查暴露在外的玩家
        // 2. 应用辐射伤害
        // 3. 添加辐射状态效果
        
        LOGGER.debug("应用辐射伤害: {}/秒", damagePerSecond);
        
        server.getPlayerManager().getPlayerList().forEach(player -> {
            if (isPlayerExposed(player)) {
                // 每5秒应用一次伤害（20tick * 5 = 100tick）
                if (server.getTicks() % 100 == 0) {
                    float damage = (float) (damagePerSecond * 5); // 5秒的伤害
                    // player.damage(player.getDamageSources().magic(), damage);
                    LOGGER.debug("玩家 {} 受到辐射伤害: {}", player.getName().getString(), damage);
                }
            }
        });
    }
    
    /**
     * 应用恶化状态效果
     */
    private void applyDegradedEffects(MinecraftServer server) {
        // 在实际实现中，这里会：
        // 1. 增加灾难触发概率
        // 2. 降低资源生成率
        // 3. 添加环境音效
        
        LOGGER.debug("应用恶化状态效果");
    }
    
    /**
     * 应用崩溃边缘状态效果
     */
    private void applyCollapsingEffects(MinecraftServer server) {
        // 在实际实现中，这里会：
        // 1. 频繁触发灾难
        // 2. 大幅降低所有效率
        // 3. 添加视觉和声音效果
        // 4. 显示倒计时
        
        LOGGER.debug("应用崩溃边缘状态效果");
        
        // 每10秒广播一次警告
        if (server.getTicks() % 200 == 0) {
            server.getPlayerManager().broadcast(
                net.minecraft.text.Text.literal("⚠️ 行星濒临崩溃！请加快迁移进度！"),
                false
            );
        }
    }
    
    /**
     * 检查玩家是否暴露在外
     */
    private boolean isPlayerExposed(net.minecraft.entity.player.PlayerEntity player) {
        // 简化实现：检查玩家是否在室外且没有遮挡
        if (player.getWorld().isRaining()) {
            return !player.isSubmergedInWater() && player.getWorld().isSkyVisible(player.getBlockPos());
        }
        return false;
    }
    
    /**
     * 检查是否应该触发效果
     */
    private boolean shouldTriggerEffect(MinecraftServer server, double chance) {
        // 基于服务器tick的随机检查
        return server.getTicks() % 100 == 0 && Math.random() < chance;
    }
    
    /**
     * 清理效果
     */
    public void cleanup() {
        LOGGER.info("行星效果应用器资源已清理");
    }
}