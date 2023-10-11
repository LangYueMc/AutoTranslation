# AutoTranslation

The following is a machine translation.

由于很多 Mod 只有英文，非英语母语玩家玩起来可能有些吃力，Mod 作者接受翻译 PR 的时间不定，且不一定有人翻译。为了解决这些痛点，开发了这个
Mod。

Since many mods are only in English, it may be difficult for non-English native players to play. The time it takes for
mod authors to accept translation PRs is uncertain, and there may not necessarily be someone to translate them. In order
to solve these pain points, I developed this Mod.

[更新日志](CHANGELOG.md)

[CHANGELOG](CHANGELOG_en.md)

# 功能

## 自动翻译无当前语言翻译的语言文件

自动翻译后加载，并不是官方加载资源包形式，在游戏目录下有个 AutoTranslation 文件夹，未翻译部分的原版文件和翻译文件都有，可以润色后打包成资源包;

## 屏幕翻译

这个功能需要在快捷键设置里设置快捷键后，打开要需要翻译的界面，然后按下快捷键，该功能应该能翻译大多数使用了原版 Screen 渲染机制的
Mod；

## 丰富的配置项

通过 Mod 配置菜单，可以配置 Mod 所需参数，具体可在游戏中查看。

## 游戏内指令

```
/auto_translation reload                    重载资源
/auto_translation confirm                   确认执行指令
/auto_translation pack_resource full        全量打包资源包
/auto_translation pack_resource increment   增量打包资源包
```

- - -

## Automatically translate language files that do not have the current language translation

load after automatic translation. It is not an official resource package. There is an Auto Translation folder in the
game directory. There are original files and translated files for the untranslated parts, which can be polished and
packaged into a resource package;

## Screen translation

This function requires setting the shortcut key in the shortcut key settings, opening the interface that needs to be
translated, and then pressing the shortcut key. This function should be able to translate most Mods that use the
original Screen rendering mechanism;

## Rich configuration items

Through the Mod configuration menu, you can configure the parameters required by the Mod, which can be viewed in the
game.

## In-game commands

```
/auto_translation reload                    Reload resources
/auto_translation confirm                   Confirm execution of commands
/auto_translation pack_resource full        Fully packaging resource
/auto_translation pack_resource increment   Incremental packaging resource
```

# 翻译 API (Translation API)

## Google

Mod 默认翻译引擎为 Google。

The default translation engine of the Mod is Google.

## 其他 (Other)

目前 Mod 仅集成了 Google 翻译，若需其他翻译 API，需等待开发，或者自行开发，仅需实现接口 `ITranslator`
，然后调用 `TranslatorManager.registerTranslator(String name, Supplier<ITranslator> getInstance)` 即可，然后通过配置项可切换翻译
API。

Currently, Mod only integrates Google Translator. If you need other translation APIs, you need to wait for development
or develop it yourself. You only need to implement the interface `ITranslator`, and then
call `TranslatorManager.registerTranslator(String name, Supplier<ITranslator> getInstance)`, and then The translation
API can be switched through configuration items.