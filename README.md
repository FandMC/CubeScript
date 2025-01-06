# CubeScript - 自定义脚本引擎

> **插件名称**：CubeScript  
> **适用平台**：Minecraft 服务器端（需对接对应 API）  

## 简介
**CubeScript** 是一个专为 Minecraft 打造的自定义脚本引擎插件。它借鉴了 JavaScript 的部分语法风格，并进行了针对性扩展和简化。通过 CubeScript，你可以使用类似 JS 的语法来对玩家、方块、世界等进行操控，实现快速开发与定制化游戏玩法。

## 特色功能

1. **专属关键字**  
   - 使用 `var` 声明自定义变量（支持 `int` / `String` / `boolean`）。  
   - 采用 `if`、`for`、`def`、`cm` 等关键字做流程控制、函数定义、执行脚本等。  

2. **严格的执行块 `cm`**  
   - 所有执行指令都必须写在 `cm:` 块里才能生效，让逻辑与执行分离更清晰。  

3. **灵活循环**  
   - `for (range(N)) {}` 支持固定次数的循环。  
   - `for (Player.hp < 1) {}` 基于条件执行。  
   - `for (True) {}` 无限循环，配合 `break` 打造灵活脚本结构。  

4. **变量自增**  
   - 支持 `++` 操作，例如：  
     ```plaintext
     cm:
         - hp++;
     ```

5. **与 Minecraft 对接**  
   - 通过 `Player.name`、`Player.hp`、`Block.xxx` 等接口来读写游戏内对象属性（与服务端 API 对接后生效）。

## 安装
1. 将 **CubeScript** 的 Jar 文件放入你的 Minecraft 服务器插件目录（如 `plugins/` 文件夹）。  
2. 重启或启动服务器，检查控制台是否显示 **CubeScript** 插件加载成功。  
3. （可选）在配置文件中自定义脚本路径、日志级别等参数。

## 使用方法

1. **创建脚本文件**  
   - 在服务器指定的脚本目录下（例如 `plugins/CubeScript/scripts`），创建一个 `.cs`（或自定义后缀）脚本文件。  
   - 按照 CubeScript 语法编写脚本逻辑（可包含变量声明、函数定义、cm 执行块等）。

2. **编辑脚本示例**  
   ```plaintext
   var int hp = 5;
   var String playerName = "Steve";
   var boolean canHeal = true;

   def checkPlayer {
       if (Player.name == playerName) {
           cm:
               - hp++;
               - Player.hp = 20;
       }
   }

   cm:
       - checkPlayer;

