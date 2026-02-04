package com.planetaryexodus.civilization;

import com.planetaryexodus.PlanetaryExodusMod;
import com.planetaryexodus.core.ModConfig;
import com.planetaryexodus.api.events.CivilizationProgressEvent;
import com.planetaryexodus.api.events.MilestoneAchievedEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 文明进度管理器
 * 负责管理整个文明的迁移进度、阶段和里程碑
 */
public class CivilizationManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("PlanetaryExodus/Civilization");
    private static final Identifier DATA_ID = new Identifier("planetaryexodus", "civilization_data");
    
    // 文明数据
    private int progress = 0;
    private int currentStageIndex = 0;
    private long lastUpdateTime = System.currentTimeMillis();
    private final Set<String> achievedMilestones = new HashSet<>();
    private final Map<UUID, PlayerContribution> playerContributions = new HashMap<>();
    
    // 配置
    private ModConfig.CivilizationConfig config;
    private List<MigrationStage> stages;
    private List<Milestone> milestones;
    
    // 迁移阶段
    private MigrationStage currentStage;
    
    public CivilizationManager() {
        reloadConfig();
        updateCurrentStage();
        LOGGER.info("文明进度管理器初始化完成，当前进度: {}%", progress);
    }
    
    /**
     * 重新加载配置
     */
    public void reloadConfig() {
        this.config = PlanetaryExodusMod.getInstance().getConfig().getCivilization();
        // 转换配置阶段为内部类
        this.stages = new ArrayList<>();
        for (ModConfig.CivilizationConfig.MigrationStage configStage : config.stages) {
            this.stages.add(new MigrationStage(configStage.name, configStage.progressRequired, configStage.durationDays));
        }
        // 转换配置里程碑为内部类
        this.milestones = new ArrayList<>();
        for (ModConfig.CivilizationConfig.Milestone configMilestone : config.milestones) {
            this.milestones.add(new Milestone(configMilestone.name, configMilestone.progressReward));
        }
        updateCurrentStage();
        LOGGER.info("文明配置已重新加载，共 {} 个阶段，{} 个里程碑", stages.size(), milestones.size());
    }
    
    /**
     * 更新当前阶段
     */
    private void updateCurrentStage() {
        for (int i = stages.size() - 1; i >= 0; i--) {
            if (progress >= stages.get(i).progressRequired) {
                currentStageIndex = i;
                currentStage = stages.get(i);
                return;
            }
        }
        currentStageIndex = 0;
        currentStage = stages.get(0);
    }
    
    /**
     * 增加文明进度
     * @param amount 增加的数量
     * @param player 贡献的玩家（可为null）
     * @param source 进度来源（如"milestone", "construction", "research"等）
     */
    public void addProgress(int amount, ServerPlayerEntity player, String source) {
        if (amount <= 0) return;
        
        int oldProgress = progress;
        progress = Math.min(100, progress + amount);
        
        // 记录玩家贡献
        if (player != null) {
            PlayerContribution contribution = playerContributions.computeIfAbsent(
                player.getUuid(), uuid -> new PlayerContribution(player.getName().getString())
            );
            contribution.addContribution(amount, source);
        }
        
        // 检查阶段变化
        boolean stageChanged = false;
        int oldStageIndex = currentStageIndex;
        updateCurrentStage();
        if (currentStageIndex != oldStageIndex) {
            stageChanged = true;
            LOGGER.info("文明进入新阶段: {} → {}", 
                stages.get(oldStageIndex).name, currentStage.name);
        }
        
        // 发布进度事件
        PlanetaryExodusMod.getInstance().getEventBus().publish(
            new CivilizationProgressEvent(oldProgress, progress, amount, source, player)
        );
        
        // 检查里程碑
        checkMilestones(null);
        
        LOGGER.debug("文明进度增加: {} (+{})，当前: {}%，阶段: {}", 
            oldProgress, amount, progress, currentStage.name);
    }
    
    /**
     * 检查里程碑
     */
    public void checkMilestones(MinecraftServer server) {
        for (Milestone milestone : milestones) {
            String milestoneId = milestone.name;
            
            if (!achievedMilestones.contains(milestoneId) && progress >= milestone.progressReward) {
                // 达到里程碑
                achievedMilestones.add(milestoneId);
                
                // 发布里程碑事件
                PlanetaryExodusMod.getInstance().getEventBus().publish(
                    new MilestoneAchievedEvent(milestone, progress)
                );
                
                // 通知所有玩家
                if (server != null) {
                    Text message = Text.translatable("civilization.milestone.achieved", 
                        Text.translatable("milestone." + milestoneId));
                    server.getPlayerManager().broadcast(message, false);
                }
                
                LOGGER.info("🎉 达到里程碑: {} (进度奖励: {})", milestoneId, milestone.progressReward);
            }
        }
    }
    
    /**
     * 服务器更新
     */
    public void update(MinecraftServer server) {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastUpdateTime;
        
        // 每秒检查一次
        if (elapsed >= 1000) {
            // 这里可以添加基于时间的进度检查
            // 例如：自动恶化、基于玩家活动的进度等
            
            lastUpdateTime = currentTime;
        }
    }
    
    /**
     * 获取文明进度
     */
    public int getProgress() {
        return progress;
    }
    
    /**
     * 获取当前阶段
     */
    public MigrationStage getCurrentStage() {
        return currentStage;
    }
    
    /**
     * 获取所有阶段
     */
    public List<MigrationStage> getStages() {
        return Collections.unmodifiableList(stages);
    }
    
    /**
     * 获取已完成的里程碑
     */
    public Set<String> getAchievedMilestones() {
        return Collections.unmodifiableSet(achievedMilestones);
    }
    
    /**
     * 获取玩家贡献
     */
    public Map<UUID, PlayerContribution> getPlayerContributions() {
        return Collections.unmodifiableMap(playerContributions);
    }
    
    /**
     * 获取指定玩家的贡献
     */
    public PlayerContribution getPlayerContribution(UUID playerId) {
        return playerContributions.get(playerId);
    }
    
    /**
     * 同步玩家数据
     */
    public void syncPlayerData(ServerPlayerEntity player) {
        // TODO: 实现网络同步
    }
    
    /**
     * 保存玩家数据
     */
    public void savePlayerData(ServerPlayerEntity player) {
        // TODO: 实现数据保存
    }
    
    /**
     * 加载数据
     */
    public void load() {
        // TODO: 实现数据加载
        LOGGER.info("加载文明进度数据...");
    }
    
    /**
     * 保存数据
     */
    public void save() {
        // TODO: 实现数据保存
        LOGGER.info("保存文明进度数据...");
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        playerContributions.clear();
        achievedMilestones.clear();
        LOGGER.info("文明进度管理器资源已清理");
    }
    
    /**
     * 玩家贡献记录
     */
    public static class PlayerContribution {
        private final String playerName;
        private int totalContribution = 0;
        private final Map<String, Integer> contributionsBySource = new HashMap<>();
        private long firstContributionTime = System.currentTimeMillis();
        private long lastContributionTime = System.currentTimeMillis();
        
        public PlayerContribution(String playerName) {
            this.playerName = playerName;
        }
        
        public void addContribution(int amount, String source) {
            totalContribution += amount;
            contributionsBySource.merge(source, amount, Integer::sum);
            lastContributionTime = System.currentTimeMillis();
        }
        
        public String getPlayerName() {
            return playerName;
        }
        
        public int getTotalContribution() {
            return totalContribution;
        }
        
        public Map<String, Integer> getContributionsBySource() {
            return Collections.unmodifiableMap(contributionsBySource);
        }
        
        public long getFirstContributionTime() {
            return firstContributionTime;
        }
        
        public long getLastContributionTime() {
            return lastContributionTime;
        }
        
        public double getContributionPercentage(int totalProgress) {
            if (totalProgress <= 0) return 0;
            return (double) totalContribution / totalProgress * 100;
        }
    }
    
    /**
     * 迁移阶段数据类
     */
    public static class MigrationStage {
        public final String name;
        public final int progressRequired;
        public final int durationDays;
        
        public MigrationStage(String name, int progressRequired, int durationDays) {
            this.name = name;
            this.progressRequired = progressRequired;
            this.durationDays = durationDays;
        }
        
        public Text getDisplayName() {
            return Text.translatable("stage." + name.toLowerCase().replace(" ", "_"));
        }
        
        public Text getDescription() {
            return Text.translatable("stage." + name.toLowerCase().replace(" ", "_") + ".desc");
        }
    }
    
    /**
     * 里程碑数据类
     */
    public static class Milestone {
        public final String name;
        public final int progressReward;
        
        public Milestone(String name, int progressReward) {
            this.name = name;
            this.progressReward = progressReward;
        }
        
        public Text getDisplayName() {
            return Text.translatable("milestone." + name.toLowerCase().replace(" ", "_"));
        }
        
        public Text getDescription() {
            return Text.translatable("milestone." + name.toLowerCase().replace(" ", "_") + ".desc");
        }
    }
}