# V1.0.0-beta.8 (2023-10-04)

## 新增

1. 兼容 Adaptive Tooltips 等工具提示修复的 Mod
2. 修复由于 Mixin 失败导致 Forge 启动崩溃
3. 修复由于在 Minecraft.instance 尚未赋值前就调用 getInstance() 导致的空指针异常

   forge 也不知道哪里出的问题，竟然 instance 赋值那么晚

# V1.0.0-beta.7 (2023-10-04)

## 新增

1. FTB 兼容
2. 优化屏幕翻译渲染性能

# V1.0.0-beta.6 (2023-10-04)

## 修复

1. 修复由于 Mixin 早于 Minecraft 加载 options.txt 导致的空指针问题
2. 修复忽略原版屏幕不生效的问题

# V1.0.0-beta.4 (2023-10-03)

## 新增

1. 添加重载 AutoTranslation 资源命令 /auto_translation reload
2. 添加屏幕翻译图标配置项
3. 补充 Patchouli 翻译
4. 添加英语特征配置项，不符合特征的不翻译
5. 优化代码逻辑

## 修复

1. 修复一个可能导致游戏启动失败的 mixin

# V1.0.0-alpha.3 (2023-09-27)

## 新增

1. 游戏世界内渲染的文字关闭翻译（如 title，F3，聊天栏）
2. 添加屏幕翻译忽略原版屏幕开关
3. 添加屏幕翻译图标

## 修复

1. 与其他 mixin 了资源加载，或调用了 ClientLanguage.loadFrom 的 Mod 的兼容性
2. 翻译语言文件时错误的替换掉了内容里格式为 (.*): 的 (.*) 部分
3. 添加原文时 String.format 占位符格式化异常

# V1.0.0-alpha.2 (2023-09-07)

## 新增

1. 资源加载时翻译所有未翻译文本
2. 指定屏幕按下快捷键翻译