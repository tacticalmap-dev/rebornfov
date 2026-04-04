# RebornFov 配置指南

本文档说明本模组在 `Minecraft 1.20.1 + Forge 47.x` 下的配置方式。

## 1. 通用配置（Common）

文件位置：

`config/rebornfov-common.toml`

说明：首次启动游戏并加载模组后自动生成。

可配置项：

- `globalAmountMultiplier`
  - 含义：对所有兵站补给条目的单次刷新数量做全局倍率。
  - 默认值：`1.0`
  - 范围：`0.0 ~ 64.0`
- `globalIntervalMultiplier`
  - 含义：对所有兵站补给条目的刷新间隔做全局倍率。
  - 默认值：`1.0`
  - 范围：`0.1 ~ 120.0`
  - 示例：设为 `2.0` 表示刷新更慢（间隔翻倍）。
- `maxTeleportDistance`
  - 含义：玩家可操作基地传送列表的最大距离。
  - 默认值：`64`
  - 范围：`1 ~ 512`
- `defaultPreset`
  - 含义：新放置 `fov` 方块时默认选择的预设 ID。
  - 默认值：`"default"`
  - 规则：必须与 `config/rebornfov/pre/` 中某个 `.json` 文件名一致（不含扩展名）。

## 2. 兵站预设配置（Preset）

目录位置：

`config/rebornfov/pre/`

规则：

- 每个 `.json` 文件就是一个预设。
- 预设 ID = 文件名（去掉 `.json`）。
- 可在游戏内对 `fov` 方块使用 `Shift + 右键` 打开预设列表选择。

---

## 3. 推荐格式（entries 数组）

这是最推荐、可读性最高的写法：

```json
{
  "displayName": "前线基础补给",
  "entries": [
    {
      "item": "minecraft:bread",
      "amount": 8,
      "intervalSeconds": 120
    },
    {
      "item": "minecraft:arrow",
      "amount": 16,
      "intervalSeconds": 60
    }
  ]
}
```

字段说明：

- `displayName`：预设显示名（可选）。
- `item`：物品 ID（必须是有效物品）。
- `amount`：单次刷新数量，未填写默认 `1`。
- `intervalSeconds`：刷新间隔（秒），未填写默认 `60`。
- `interval`：可作为 `intervalSeconds` 的兼容别名。

---

## 4. 兼容格式 A（按物品 ID 映射对象）

```json
{
  "displayName": "对象映射格式",
  "minecraft:bread": {
    "amount": 8,
    "intervalSeconds": 120
  },
  "minecraft:arrow": {
    "amount": 16,
    "interval": 60
  }
}
```

---

## 5. 兼容格式 B（按物品 ID 映射数组/字符串）

```json
{
  "displayName": "简写格式",
  "minecraft:bread": [8, 120],
  "minecraft:arrow": "16-60"
}
```

说明：

- 数组格式 `[数量, 间隔秒]`。
- 字符串格式支持分隔符 `-`, `,`, `:`, 空格（示例：`"16-60"`、`"16,60"`）。

## 6. 常见问题

- 预设不显示：
  - 检查 JSON 是否合法。
  - 检查文件后缀是否为 `.json`。
  - 检查物品 ID 是否有效。
- `defaultPreset` 不生效：
  - 检查 `defaultPreset` 是否与某个预设文件名一致。
- 刷新太快/太慢：
  - 先调整预设中的 `intervalSeconds`，再用 `globalIntervalMultiplier` 做全局微调。

