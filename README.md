# [![HuskHomes Banner](images/banner-graphic.png)](https://github.com/WiIIiam278/HuskHomes2)

这是 [HuskHomeGUI](https://github.com/WiIIiam278/HuskHomesGUI) 的分支, 我将为它修复各种错误和添加功能. 但不保证遵循规范? 

### 此分支

- 链接
  - [功能预览和详细信息](https://github.com/ApliNi/HuskHomesGUI/releases)
  - [计划中的](https://github.com/ApliNi/HuskHomesGUI/issues)
  - [已完成的](https://github.com/ApliNi/HuskHomesGUI/issues?q=is%3Aissue+is%3Aclosed)


- 错误修复
  - 主分支上的错误
  - [修改传送点名称后会丢失图标](https://github.com/ApliNi/HuskHomesGUI/issues/5)


- 新功能
  - 添加配置文件和各种配置项
  - [允许将物品拖动到传送点上以完成图标设置](https://github.com/ApliNi/HuskHomesGUI/issues/2)
  - 编辑页面和铁砧菜单


**config-zh_cn.yml**
```yaml
config-version: 0


menu:

  size: 6 # 2 ~ 6

  title:
    HOME: '%1% 的传送点'
    PUBLIC_HOME: '开放的传送点'
    WARP: '地标'

  theme-item:
    HOME-item: ORANGE_STAINED_GLASS_PANE
    PUBLIC_HOME-item: LIME_STAINED_GLASS_PANE
    WARP-item: LIGHT_BLUE_STAINED_GLASS_PANE

  item:
    default-item: WHITE_STAINED_GLASS
    name: '§a%1%'
    description: '§7%1%'
    description-var1-no: '' # 没有描述
    space: ''
    Left: ''
    Right: ''
    Shift: ''
    # 创建phome的玩家的名称
    PUBLIC_HOME: 
      owner-name: '§7[由 §a%1% §7创建]'

  pagination:
    FIRST:
      title: '§b首页 \[1\]'
      item: REPEATER

    PREVIOUS:
      title: '§a上一页 \[%prevpage%\]'
      item: ARROW

    NEXT:
      title: '§a下一页 \[%nextpage%\]'
      item: SPECTRAL_ARROW

    LAST:
      title: '§b尾页 \[%pages%\]'
      item: REPEATER

    INFO:
      enable: true
      title: '§f操作说明:'
      # '§7Left Click: Teleport\nRight Click: Edit\nShift Click: Set icon'
      message:
        A: '§7  - 左键: 传送'
        B: '§7  - 右键: 编辑'
        C: '§7  - Shift + 点击: 设置图标'
      item: OAK_SIGN


edit-menu:
  title:
    HOME: '编辑传送点: %1%'
    WARP: '编辑地标: %1%'

  theme-item:
    HOME: LIME_STAINED_GLASS_PANE
    WARP: LIME_STAINED_GLASS_PANE

  button:
    BACK:
      item: ORANGE_STAINED_GLASS_PANE
      text: '§b返回'

    Update-location:
      item: OAK_BOAT
      text: '§b更新位置'

    Update-name:
      item: NAME_TAG
      text: '§a修改名称'
      anvil-menu:
        title: '修改名称: %1%'

    Update-description:
      item: WRITABLE_BOOK
      text: '§a修改描述'
      anvil-menu:
        title: '修改描述: %1%'

    INFO:
      name: '§a%1%'
      description: '§7%1%'
      world: '§7  - 世界: %1%'
      server: '' #'Server: %1%'
      coordinate: '§7  - 位置: %x% %y% %z%'

    Update-privacy:
      item: NETHER_STAR
      text: '§6切换开放状态'

    del:
      item: BARRIER
      text: '§c删除'
      text-2: '§7使用右键点击'


chat:
  updated-icon: '§bIpacEL §f> §a已添加图标到 §b%1%'
```
