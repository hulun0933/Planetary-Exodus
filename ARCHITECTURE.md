# Planetary Exodus 架构设计文档

## 架构方案：混合架构（模块化 + 事件驱动）

### 核心设计原则
1. **模块化分离** - 功能独立，易于维护和扩展
2. **事件驱动通信** - 松耦合，系统间通过事件通信
3. **数据驱动配置** - JSON配置，便于调整平衡
4. **网络同步优化** - 客户端-服务器状态同步
5. **渐进式复杂度** - 从简单核心开始，逐步添加功能

### 目录结构
```
src/main/java/com/planetaryexodus/
├── PlanetaryExodusMod.java          # 主入口
├── core/                           # 核心系统
│   ├── EventBus.java               # 事件总线
│   ├── ModConfig.java              # 配置管理
│   └── ModConstants.java           # 常量定义
├── planet/                         # 行星系统
│   ├── PlanetStatus.java           # 行星状态枚举
│   ├── PlanetStatusManager.java    # 状态管理
│   ├── PlanetEffects.java          # 状态效果应用
│   └── PlanetData.java             # 行星数据存储
├── civilization/                   # 文明系统
│   ├── CivilizationProgress.java   # 文明进度
│   ├── MigrationStage.java         # 迁移阶段
│   ├── Milestone.java              # 里程碑定义
│   └── CivilizationManager.java    # 文明管理
├── player/                         # 玩家系统
│   ├── PlayerRole.java             # 职业枚举
│   ├── PlayerRoleManager.java      # 职业管理
│   ├── PlayerData.java             # 玩家数据
│   └── RoleBenefits.java           # 职业增益
├── rocket/                         # 火箭系统
│   ├── RocketEntity.java           # 火箭实体
│   ├── RocketStage.java            # 飞行阶段
│   ├── RocketManager.java          # 火箭管理
│   └── RocketConstruction.java     # 建造系统
├── disaster/                       # 灾难系统
│   ├── DisasterType.java           # 灾难类型
│   ├── DisasterEvent.java          # 灾难事件
│   ├── DisasterManager.java        # 灾难管理
│   └── DisasterEffects.java        # 灾难效果
├── command/                        # 命令系统
│   ├── ExodusCommand.java          # 主命令
│   └── CommandRegistry.java        # 命令注册
├── network/                        # 网络系统
│   ├── ModPackets.java             # 数据包定义
│   ├── ClientSyncManager.java      # 客户端同步
│   └── ServerSyncManager.java      # 服务器同步
├── client/                         # 客户端
│   ├── ClientModInitializer.java   # 客户端入口
│   ├── HudRenderer.java            # HUD渲染
│   ├── GuiHandler.java             # GUI处理
│   └── ClientEvents.java           # 客户端事件
├── server/                         # 服务器端
│   ├── ServerModInitializer.java   # 服务器入口
│   └── ServerEvents.java           # 服务器事件
├── world/                          # 世界交互
│   ├── WorldEffects.java           # 世界效果
│   ├── StructureManager.java       # 结构管理
│   └── ResourceManager.java        # 资源管理
└── api/                            # API接口
    ├── events/                     # 事件定义
    │   ├── PlanetStatusChangedEvent.java
    │   ├── CivilizationProgressEvent.java
    │   ├── DisasterTriggeredEvent.java
    │   └── RocketLaunchEvent.java
    └── interfaces/                 # 接口定义
        ├── IProgressListener.java
        └── IEventSubscriber.java
```

### 事件系统设计
```
EventBus
  ├── subscribe(EventType, IEventSubscriber)
  ├── publish(Event)
  └── unsubscribe(EventType, IEventSubscriber)

事件类型:
  - PlanetStatusChangedEvent
  - CivilizationProgressEvent
  - DisasterTriggeredEvent
  - RocketLaunchEvent
  - PlayerRoleChangedEvent
  - MigrationStageAdvancedEvent
```

### 数据流向
```
玩家行动 → 事件系统 → 各模块处理 → 状态更新 → 网络同步 → 客户端渲染
```

### 配置系统
- `config/planetary-exodus/planet.json` - 行星状态配置
- `config/planetary-exodus/civilization.json` - 文明进度配置
- `config/planetary-exodus/disasters.json` - 灾难配置
- `config/planetary-exodus/rockets.json` - 火箭配置

### 网络同步策略
- 行星状态: 每5秒同步一次
- 文明进度: 每10秒同步一次
- 玩家职业: 登录时同步
- 火箭状态: 实时同步

### 性能优化
1. 使用缓存减少计算
2. 批量事件处理
3. 异步网络通信
4. 客户端预测渲染

### 扩展性考虑
1. 模块化设计便于添加新系统
2. 事件驱动便于第三方集成
3. 配置驱动便于平衡调整
4. API接口便于其他mod交互

### 实现优先级
1. 核心事件系统和行星状态
2. 文明进度和迁移阶段
3. 玩家职业系统
4. 灾难系统
5. 火箭系统
6. 网络同步
7. 客户端HUD和GUI
8. 命令系统
9. 配置系统
10. 性能优化

### 测试策略
1. 单元测试: 核心逻辑
2. 集成测试: 模块交互
3. 性能测试: 大规模事件处理
4. 兼容性测试: 与其他mod交互