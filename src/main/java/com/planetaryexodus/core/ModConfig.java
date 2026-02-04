package com.planetaryexodus.core;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * 模组配置管理器
 * 负责加载、保存和管理所有JSON配置文件
 */
public class ModConfig {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("PlanetaryExodus/Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    // 配置目录
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("planetary-exodus");
    
    // 配置文件
    private static final Path PLANET_CONFIG = CONFIG_DIR.resolve("planet.json");
    private static final Path CIVILIZATION_CONFIG = CONFIG_DIR.resolve("civilization.json");
    private static final Path DISASTER_CONFIG = CONFIG_DIR.resolve("disasters.json");
    private static final Path ROCKET_CONFIG = CONFIG_DIR.resolve("rockets.json");
    private static final Path PLAYER_CONFIG = CONFIG_DIR.resolve("player.json");
    
    // 配置数据
    private final JsonObject planetConfig;
    private final JsonObject civilizationConfig;
    private final JsonObject disasterConfig;
    private final JsonObject rocketConfig;
    private final JsonObject playerConfig;
    
    // 缓存配置对象
    private PlanetConfig planet;
    private CivilizationConfig civilization;
    private DisasterConfig disaster;
    private RocketConfig rocket;
    private PlayerConfig player;
    
    private ModConfig() {
        // 确保配置目录存在
        ensureConfigDirectory();
        
        // 加载或创建配置文件
        this.planetConfig = loadOrCreateConfig(PLANET_CONFIG, getDefaultPlanetConfig());
        this.civilizationConfig = loadOrCreateConfig(CIVILIZATION_CONFIG, getDefaultCivilizationConfig());
        this.disasterConfig = loadOrCreateConfig(DISASTER_CONFIG, getDefaultDisasterConfig());
        this.rocketConfig = loadOrCreateConfig(ROCKET_CONFIG, getDefaultRocketConfig());
        this.playerConfig = loadOrCreateConfig(PLAYER_CONFIG, getDefaultPlayerConfig());
        
        // 解析配置对象
        this.planet = parsePlanetConfig();
        this.civilization = parseCivilizationConfig();
        this.disaster = parseDisasterConfig();
        this.rocket = parseRocketConfig();
        this.player = parsePlayerConfig();
        
        LOGGER.info("配置加载完成，共 {} 个配置文件", 5);
    }
    
    /**
     * 加载配置
     */
    public static ModConfig load() {
        return new ModConfig();
    }
    
    /**
     * 获取行星配置
     */
    public PlanetConfig getPlanet() {
        return planet;
    }
    
    /**
     * 获取文明配置
     */
    public CivilizationConfig getCivilization() {
        return civilization;
    }
    
    /**
     * 获取灾难配置
     */
    public DisasterConfig getDisaster() {
        return disaster;
    }
    
    /**
     * 获取火箭配置
     */
    public RocketConfig getRocket() {
        return rocket;
    }
    
    /**
     * 获取玩家配置
     */
    public PlayerConfig getPlayer() {
        return player;
    }
    
    /**
     * 保存所有配置到文件
     */
    public void save() {
        try {
            Files.writeString(PLANET_CONFIG, GSON.toJson(planetConfig));
            Files.writeString(CIVILIZATION_CONFIG, GSON.toJson(civilizationConfig));
            Files.writeString(DISASTER_CONFIG, GSON.toJson(disasterConfig));
            Files.writeString(ROCKET_CONFIG, GSON.toJson(rocketConfig));
            Files.writeString(PLAYER_CONFIG, GSON.toJson(playerConfig));
            LOGGER.info("配置已保存到文件");
        } catch (IOException e) {
            LOGGER.error("保存配置时出错", e);
        }
    }
    
    // ========== 私有方法 ==========
    
    private void ensureConfigDirectory() {
        try {
            Files.createDirectories(CONFIG_DIR);
        } catch (IOException e) {
            LOGGER.error("创建配置目录失败", e);
        }
    }
    
    private JsonObject loadOrCreateConfig(Path path, JsonObject defaultConfig) {
        try {
            if (Files.exists(path)) {
                String content = Files.readString(path);
                return GSON.fromJson(content, JsonObject.class);
            } else {
                Files.writeString(path, GSON.toJson(defaultConfig));
                return defaultConfig;
            }
        } catch (IOException e) {
            LOGGER.error("加载配置文件失败: {}", path, e);
            return defaultConfig;
        }
    }
    
    // ========== 默认配置生成 ==========
    
    private JsonObject getDefaultPlanetConfig() {
        JsonObject config = new JsonObject();
        
        // 行星状态阈值
        JsonObject thresholds = new JsonObject();
        thresholds.addProperty("stable_max", 25);      // 0-25% 稳定
        thresholds.addProperty("strained_max", 50);    // 25-50% 负荷上升
        thresholds.addProperty("degraded_max", 75);    // 50-75% 恶化
        // 75-100% 崩溃边缘
        
        config.add("thresholds", thresholds);
        
        // 状态效果
        JsonObject effects = new JsonObject();
        
        // 稳定状态
        JsonObject stable = new JsonObject();
        stable.addProperty("crop_growth_multiplier", 1.0);
        stable.addProperty("energy_efficiency", 1.0);
        stable.addProperty("monster_spawn_multiplier", 1.0);
        effects.add("stable", stable);
        
        // 负荷上升状态
        JsonObject strained = new JsonObject();
        strained.addProperty("crop_growth_multiplier", 0.75);
        strained.addProperty("energy_efficiency", 0.9);
        strained.addProperty("monster_spawn_multiplier", 1.5);
        effects.add("strained", strained);
        
        // 恶化状态
        JsonObject degraded = new JsonObject();
        degraded.addProperty("crop_growth_multiplier", 0.5);
        degraded.addProperty("energy_efficiency", 0.8);
        degraded.addProperty("monster_spawn_multiplier", 2.0);
        degraded.addProperty("acid_rain_chance", 0.1);
        effects.add("degraded", degraded);
        
        // 崩溃边缘状态
        JsonObject collapsing = new JsonObject();
        collapsing.addProperty("crop_growth_multiplier", 0.2);
        collapsing.addProperty("energy_efficiency", 0.3);
        collapsing.addProperty("monster_spawn_multiplier", 3.0);
        collapsing.addProperty("acid_rain_chance", 0.3);
        collapsing.addProperty("radiation_damage_per_second", 0.5);
        effects.add("collapsing", collapsing);
        
        config.add("effects", effects);
        
        return config;
    }
    
    private JsonObject getDefaultCivilizationConfig() {
        JsonObject config = new JsonObject();
        
        // 迁移阶段
        JsonArray stages = new JsonArray();
        
        JsonObject stage1 = new JsonObject();
        stage1.addProperty("name", "立足与觉醒");
        stage1.addProperty("progress_required", 0);
        stage1.addProperty("duration_days", 10);
        stages.add(stage1);
        
        JsonObject stage2 = new JsonObject();
        stage2.addProperty("name", "仰望星空");
        stage2.addProperty("progress_required", 25);
        stage2.addProperty("duration_days", 20);
        stages.add(stage2);
        
        JsonObject stage3 = new JsonObject();
        stage3.addProperty("name", "方舟计划");
        stage3.addProperty("progress_required", 50);
        stage3.addProperty("duration_days", 30);
        stages.add(stage3);
        
        JsonObject stage4 = new JsonObject();
        stage4.addProperty("name", "启程之时");
        stage4.addProperty("progress_required", 75);
        stage4.addProperty("duration_days", 60);
        stages.add(stage4);
        
        config.add("stages", stages);
        
        // 里程碑
        JsonArray milestones = new JsonArray();
        
        JsonObject milestone1 = new JsonObject();
        milestone1.addProperty("name", "第一把铁镐");
        milestone1.addProperty("progress_reward", 5);
        milestones.add(milestone1);
        
        JsonObject milestone2 = new JsonObject();
        milestone2.addProperty("name", "地下生态农场");
        milestone2.addProperty("progress_reward", 10);
        milestones.add(milestone2);
        
        JsonObject milestone3 = new JsonObject();
        milestone3.addProperty("name", "轨道电梯基座");
        milestone3.addProperty("progress_reward", 15);
        milestones.add(milestone3);
        
        config.add("milestones", milestones);
        
        return config;
    }
    
    private JsonObject getDefaultDisasterConfig() {
        JsonObject config = new JsonObject();
        
        // 灾难类型
        JsonArray disasters = new JsonArray();
        
        JsonObject radiation = new JsonObject();
        radiation.addProperty("type", "RADIATION");
        radiation.addProperty("chance_per_day", 0.1);
        radiation.addProperty("duration_minutes", 30);
        radiation.addProperty("damage_per_second", 0.2);
        disasters.add(radiation);
        
        JsonObject acidRain = new JsonObject();
        acidRain.addProperty("type", "ACID_RAIN");
        acidRain.addProperty("chance_per_day", 0.05);
        acidRain.addProperty("duration_minutes", 60);
        acidRain.addProperty("block_corrosion_chance", 0.01);
        disasters.add(acidRain);
        
        JsonObject earthquake = new JsonObject();
        earthquake.addProperty("type", "EARTHQUAKE");
        earthquake.addProperty("chance_per_day", 0.02);
        earthquake.addProperty("duration_minutes", 10);
        earthquake.addProperty("block_damage_chance", 0.05);
        disasters.add(earthquake);
        
        config.add("disasters", disasters);
        
        // 灾难触发条件
        JsonObject triggers = new JsonObject();
        triggers.addProperty("min_days_between_disasters", 2);
        triggers.addProperty("chance_multiplier_per_status_level", 0.5);
        config.add("triggers", triggers);
        
        return config;
    }
    
    private JsonObject getDefaultRocketConfig() {
        JsonObject config = new JsonObject();
        
        // 火箭类型
        JsonArray rockets = new JsonArray();
        
        JsonObject smallRocket = new JsonObject();
        smallRocket.addProperty("type", "SMALL");
        smallRocket.addProperty("max_fuel", 1000);
        smallRocket.addProperty("fuel_consumption_per_tick", 1);
        smallRocket.addProperty("max_passengers", 2);
        smallRocket.addProperty("build_time_hours", 24);
        rockets.add(smallRocket);
        
        JsonObject mediumRocket = new JsonObject();
        mediumRocket.addProperty("type", "MEDIUM");
        mediumRocket.addProperty("max_fuel", 5000);
        mediumRocket.addProperty("fuel_consumption_per_tick", 3);
        mediumRocket.addProperty("max_passengers", 10);
        mediumRocket.addProperty("build_time_hours", 72);
        rockets.add(mediumRocket);
        
        JsonObject largeRocket = new JsonObject();
        largeRocket.addProperty("type", "LARGE");
        largeRocket.addProperty("max_fuel", 20000);
        largeRocket.addProperty("fuel_consumption_per_tick", 10);
        largeRocket.addProperty("max_passengers", 50);
        largeRocket.addProperty("build_time_hours", 168);
        rockets.add(largeRocket);
        
        config.add("rockets", rockets);
        
        // 飞行阶段
        JsonObject stages = new JsonObject();
        stages.addProperty("launch_duration_ticks", 200);      // 10秒
        stages.addProperty("orbit_insertion_ticks", 600);      // 30秒
        stages.addProperty("cruise_ticks_per_block", 2);       // 每格2tick
        stages.addProperty("landing_duration_ticks", 400);     // 20秒
        config.add("stages", stages);
        
        return config;
    }
    
    private JsonObject getDefaultPlayerConfig() {
        JsonObject config = new JsonObject();
        
        // 职业配置
        JsonArray roles = new JsonArray();
        
        JsonObject industrial = new JsonObject();
        industrial.addProperty("role", "INDUSTRIAL_ENGINEER");
        industrial.addProperty("mining_speed_bonus", 0.2);
        industrial.addProperty("furnace_speed_bonus", 0.3);
        industrial.addProperty("energy_efficiency_bonus", 0.1);
        roles.add(industrial);
        
        JsonObject aerospace = new JsonObject();
        aerospace.addProperty("role", "AEROSPACE_ENGINEER");
        aerospace.addProperty("rocket_build_speed_bonus", 0.25);
        aerospace.addProperty("fuel_efficiency_bonus", 0.15);
        aerospace.addProperty("launch_success_bonus", 0.1);
        roles.add(aerospace);
        
        JsonObject scientist = new JsonObject();
        scientist.addProperty("role", "SCIENTIST");
        scientist.addProperty("research_speed_bonus", 0.3);
        scientist.addProperty("disaster_warning_advance_minutes", 10);
        scientist.addProperty("experiment_success_bonus", 0.2);
        roles.add(scientist);
        
        JsonObject logistics = new JsonObject();
        logistics.addProperty("role", "LOGISTICS_COORDINATOR");
        logistics.addProperty("inventory_capacity_bonus", 0.5);
        logistics.addProperty("transfer_speed_bonus", 0.25);
        logistics.addProperty("storage_efficiency_bonus", 0.2);
        roles.add(logistics);
        
        JsonObject civilian = new JsonObject();
        civilian.addProperty("role", "CIVILIAN");
        civilian.addProperty("general_efficiency_bonus", 0.05);
        roles.add(civilian);
        
        config.add("roles", roles);
        
        return config;
    }
    
    // ========== 配置对象解析 ==========
    
    private PlanetConfig parsePlanetConfig() {
        return new PlanetConfig(planetConfig);
    }
    
    private CivilizationConfig parseCivilizationConfig() {
        return new CivilizationConfig(civilizationConfig);
    }
    
    private DisasterConfig parseDisasterConfig() {
        return new DisasterConfig(disasterConfig);
    }
    
    private RocketConfig parseRocketConfig() {
        return new RocketConfig(rocketConfig);
    }
    
    private PlayerConfig parsePlayerConfig() {
        return new PlayerConfig(playerConfig);
    }
    
    // ========== 配置对象类 ==========
    
    public static class PlanetConfig {
        public final int stableThreshold;
        public final int strainedThreshold;
        public final int degradedThreshold;
        
        public final Map<String, Double> stableEffects;
        public final Map<String, Double> strainedEffects;
        public final Map<String, Double> degradedEffects;
        public final Map<String, Double> collapsingEffects;
        
        PlanetConfig(JsonObject json) {
            JsonObject thresholds = json.getAsJsonObject("thresholds");
            this.stableThreshold = thresholds.get("stable_max").getAsInt();
            this.strainedThreshold = thresholds.get("strained_max").getAsInt();
            this.degradedThreshold = thresholds.get("degraded_max").getAsInt();
            
            JsonObject effects = json.getAsJsonObject("effects");
            this.stableEffects = parseEffects(effects.getAsJsonObject("stable"));
            this.strainedEffects = parseEffects(effects.getAsJsonObject("strained"));
            this.degradedEffects = parseEffects(effects.getAsJsonObject("degraded"));
            this.collapsingEffects = parseEffects(effects.getAsJsonObject("collapsing"));
        }
        
        private Map<String, Double> parseEffects(JsonObject json) {
            Map<String, Double> map = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                map.put(entry.getKey(), entry.getValue().getAsDouble());
            }
            return map;
        }
    }
    
    public static class CivilizationConfig {
        public final List<MigrationStage> stages;
        public final List<Milestone> milestones;
        
        CivilizationConfig(JsonObject json) {
            this.stages = new ArrayList<>();
            JsonArray stagesArray = json.getAsJsonArray("stages");
            for (JsonElement element : stagesArray) {
                JsonObject stageObj = element.getAsJsonObject();
                stages.add(new MigrationStage(
                    stageObj.get("name").getAsString(),
                    stageObj.get("progress_required").getAsInt(),
                    stageObj.get("duration_days").getAsInt()
                ));
            }
            
            this.milestones = new ArrayList<>();
            JsonArray milestonesArray = json.getAsJsonArray("milestones");
            for (JsonElement element : milestonesArray) {
                JsonObject milestoneObj = element.getAsJsonObject();
                milestones.add(new Milestone(
                    milestoneObj.get("name").getAsString(),
                    milestoneObj.get("progress_reward").getAsInt()
                ));
            }
        }
        
        public static class MigrationStage {
            public final String name;
            public final int progressRequired;
            public final int durationDays;
            
            MigrationStage(String name, int progressRequired, int durationDays) {
                this.name = name;
                this.progressRequired = progressRequired;
                this.durationDays = durationDays;
            }
        }
        
        public static class Milestone {
            public final String name;
            public final int progressReward;
            
            Milestone(String name, int progressReward) {
                this.name = name;
                this.progressReward = progressReward;
            }
        }
    }
    
    public static class DisasterConfig {
        public final List<DisasterTypeConfig> disasters;
        public final double minDaysBetweenDisasters;
        public final double chanceMultiplierPerStatusLevel;
        
        DisasterConfig(JsonObject json) {
            this.disasters = new ArrayList<>();
            JsonArray disastersArray = json.getAsJsonArray("disasters");
            for (JsonElement element : disastersArray) {
                JsonObject disasterObj = element.getAsJsonObject();
                disasters.add(new DisasterTypeConfig(
                    disasterObj.get("type").getAsString(),
                    disasterObj.get("chance_per_day").getAsDouble(),
                    disasterObj.get("duration_minutes").getAsInt(),
                    disasterObj.has("damage_per_second") ? disasterObj.get("damage_per_second").getAsDouble() : 0,
                    disasterObj.has("block_corrosion_chance") ? disasterObj.get("block_corrosion_chance").getAsDouble() : 0,
                    disasterObj.has("block_damage_chance") ? disasterObj.get("block_damage_chance").getAsDouble() : 0
                ));
            }
            
            JsonObject triggers = json.getAsJsonObject("triggers");
            this.minDaysBetweenDisasters = triggers.get("min_days_between_disasters").getAsDouble();
            this.chanceMultiplierPerStatusLevel = triggers.get("chance_multiplier_per_status_level").getAsDouble();
        }
        
        public static class DisasterTypeConfig {
            public final String type;
            public final double chancePerDay;
            public final int durationMinutes;
            public final double damagePerSecond;
            public final double blockCorrosionChance;
            public final double blockDamageChance;
            
            DisasterTypeConfig(String type, double chancePerDay, int durationMinutes, 
                             double damagePerSecond, double blockCorrosionChance, double blockDamageChance) {
                this.type = type;
                this.chancePerDay = chancePerDay;
                this.durationMinutes = durationMinutes;
                this.damagePerSecond = damagePerSecond;
                this.blockCorrosionChance = blockCorrosionChance;
                this.blockDamageChance = blockDamageChance;
            }
        }
    }
    
    public static class RocketConfig {
        public final List<RocketTypeConfig> rocketTypes;
        public final int launchDurationTicks;
        public final int orbitInsertionTicks;
        public final int cruiseTicksPerBlock;
        public final int landingDurationTicks;
        
        RocketConfig(JsonObject json) {
            this.rocketTypes = new ArrayList<>();
            JsonArray rocketsArray = json.getAsJsonArray("rockets");
            for (JsonElement element : rocketsArray) {
                JsonObject rocketObj = element.getAsJsonObject();
                rocketTypes.add(new RocketTypeConfig(
                    rocketObj.get("type").getAsString(),
                    rocketObj.get("max_fuel").getAsInt(),
                    rocketObj.get("fuel_consumption_per_tick").getAsInt(),
                    rocketObj.get("max_passengers").getAsInt(),
                    rocketObj.get("build_time_hours").getAsInt()
                ));
            }
            
            JsonObject stages = json.getAsJsonObject("stages");
            this.launchDurationTicks = stages.get("launch_duration_ticks").getAsInt();
            this.orbitInsertionTicks = stages.get("orbit_insertion_ticks").getAsInt();
            this.cruiseTicksPerBlock = stages.get("cruise_ticks_per_block").getAsInt();
            this.landingDurationTicks = stages.get("landing_duration_ticks").getAsInt();
        }
        
        public static class RocketTypeConfig {
            public final String type;
            public final int maxFuel;
            public final int fuelConsumptionPerTick;
            public final int maxPassengers;
            public final int buildTimeHours;
            
            RocketTypeConfig(String type, int maxFuel, int fuelConsumptionPerTick, 
                           int maxPassengers, int buildTimeHours) {
                this.type = type;
                this.maxFuel = maxFuel;
                this.fuelConsumptionPerTick = fuelConsumptionPerTick;
                this.maxPassengers = maxPassengers;
                this.buildTimeHours = buildTimeHours;
            }
        }
    }
    
    public static class PlayerConfig {
        public final Map<String, RoleConfig> roles;
        
        PlayerConfig(JsonObject json) {
            this.roles = new HashMap<>();
            JsonArray rolesArray = json.getAsJsonArray("roles");
            for (JsonElement element : rolesArray) {
                JsonObject roleObj = element.getAsJsonObject();
                String roleName = roleObj.get("role").getAsString();
                roles.put(roleName, new RoleConfig(roleObj));
            }
        }
        
        public static class RoleConfig {
            public final String role;
            public final Map<String, Double> bonuses;
            
            RoleConfig(JsonObject json) {
                this.role = json.get("role").getAsString();
                this.bonuses = new HashMap<>();
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    if (!entry.getKey().equals("role")) {
                        bonuses.put(entry.getKey(), entry.getValue().getAsDouble());
                    }
                }
            }
        }
    }
}