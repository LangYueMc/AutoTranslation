> The following is a machine translation. Welcome to correct grammatical errors.

# V1.1.0

2023-10-12

## Add

1. Set license.
2. Add commands for packaging resource:

```
/auto_translation confirm                   Confirm execution of commands
/auto_translation pack_resource full        Fully packaging resource
/auto_translation pack_resource increment   Incremental packaging resource
```

## Fixed

1. Fix module conflicts caused by using `@Redirect`.
2. Fix the forge loop call caused by irregular code.
3. Fix code logic errors.

# V1.0.5

2023-10-09

## Fixed

1. Fix the BUG in the 1.0.4 version code that caused the screen translation to fail (sorry for releasing without strict
   testing)

# V1.0.4

2023-10-09

## Changed

1. Ignore all capitalized English words in translation.
2. Add Forge ModListScreen to the screen translation blacklist.
3. Corrected the screen translation warning description. Screen translation may cause the game to crash. Try not to
   enable it.

## Fixed

1. Fixed the bug where the icon can still be clicked after being hidden.

# V1.0.3

2023-10-09

## New

1. Add icon to always display configuration items.
2. The content of the original input box is added to the blacklist and will no longer be translated.
3. Add ClothConfigScreen to the blacklist and no longer translate it.

## Fixed

1. Fix the tooltip not rendering on the screen caused by the 1.0.2 code error.

# V1.0.2

2023-10-07

## New

1. Refactor the icon rendering code to support 1.20.2

# V1.0.1

2023-10-06

## Fixed

1. Fixed the problem of incorrect translation of chat content during screen translation in multiplayer games.

# V1.0.0-beta.8

2023-10-04

## New

1. Compatible with tooltip repair Mods such as Adaptive Tooltips.
2. Fix Forge startup crash due to Mixin failure.
3. Fix the null pointer exception caused by calling getInstance() before Minecraft.instance has been assigned a value.

   Forge doesn’t know what the problem is. The instance is assigned so late.

# V1.0.0-beta.7

2023-10-04

## New

1. FTB compatible.
2. Optimize screen translation rendering performance.

# V1.0.0-beta.6

2023-10-04

## Fixed

1. Fix the null pointer problem caused by Mixin loading options.txt earlier than Minecraft.
2. Fixed the problem of ignoring the original screen not taking effect.

# V1.0.0-beta.4

2023-10-03

## New

1. Add reloaded AutoTranslation resource command /auto_translation reload
2. Add screen translation icon configuration item.
3. Supplementary translation of Patchouli.
4. Add English feature configuration items, and those that do not meet the features will not be translated.
5. Optimize code logic.

## Fixed

1. Fixed a mixin that could cause the game to fail to launch

# V1.0.0-alpha.3

2023-09-27

## New

1. Turn off translation of text rendered in the game world (such as Title, F3, chat bar).
2. Add screen translation to ignore the original screen switch.
3. Add screen translation icon.

## Fixed

1. Compatibility with other mixins that load resources or call ClientLanguage.loadFrom Mods.
2. When translating the language file, the (.\*) part in the content format (.\*): was mistakenly replaced.
3. String.format placeholder formatting exception when adding original text.

# V1.0.0-alpha.2

2023-09-07

## New

1. Translate all untranslated text when resources are loaded.
2. Press the shortcut key on the specified screen to translate.