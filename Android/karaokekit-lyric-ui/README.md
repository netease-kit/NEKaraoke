# README
通过本文档，您可以在自己的工程中快速接入歌词组件

# 集成方式

在app.build.gradle文件中添加如下代码：
```
    // todo
<!--    implementation 'com.netease.yunxin.kit.lyric:lyrickit-ui:1.0.1'-->
    implementation(project(":copyrightedmedia:copyrightedmedia"))
```


# 如何使用？

以QRC歌词格式为例
```
1、通过parseQrcLyricInputStream将歌词文件转成字符串content，字符串格式如下:
[ti:]
[ar:]
[al:]
[by:QQ音乐动态歌词]
[offset:0]
[11640,3810]最(11640,360)近(12000,300)一(12300,240)直(12540,330)很(12870,300)好(13170,240)心(13440,630)情(14070,1380)
[16260,3000]不(16260,270)知(16560,300)道(16860,210)什(17070,330)么(17400,330)原(17730,300)因(18030,1230)
[20850,3450]我(20850,210)现(21060,390)在(21450,270)这(21720,330)一(22050,270)种(22320,270)心(22590,630)情(23220,1080)
[25440,3390]我(25440,210)想(25650,360)要(26010,300)唱(26310,330)给(26640,330)你(26970,210)听(27180,1650)

2、通过initWithContent将字符串解析成NELyric模型
NELyric.initWithContent(content, NELyric.NELyricType.NELyricTypeQrc);

3、通过loadWithLyricModel设置歌词模型
lyricView.loadWithLyricModel(lyric);//设置整首
或者
 lyricView.loadWithLyricModel(lyric, startTime, endTime);//设置片段

4、通过setLyricMode设置逐字展示
lyricView.setLyricMode(NELyricView.LyricMode.WordByWord);

5、通过update方法刷新当前时间戳刷新UI
lyricView.update(current);
业务上需要启动一个定时器，每30ms去刷新当前时间戳，不断地刷新UI
```

# API
## 解析歌词

```
    /**
      * 初始化
      *
      * @param content 歌词内容
      * @param type    歌词格式
      * @return
      */
     public static NELyric initWithContent(String content, NELyricType type) {
        
     }
```

参数描述

| 参数  | 类型 | 描述 |
| :------: | :------: | :------: |
|  content | String |   歌词内容
|  type | NELyricType |   歌词格式

返回值说明
歌词模型

## 设置歌词

```
  /**
     * 设置歌词
     *
     * @param model 歌词对象
     */
    public void loadWithLyricModel(NELyric model) {
        
    }
```

参数描述

| 参数  | 类型 | 描述 |
| :------: | :------: | :------: |
|  model | NELyric |   歌词模型

## 设置歌词（片段）

```
    /**
     * 设置歌词
     *
     * @param model     歌词对象
     * @param startTime 开始时间
     * @param endTime   结束时间
     */
    public void loadWithLyricModel(NELyric model, long startTime, long endTime) {
        
    }
```

参数描述

| 参数  | 类型 | 描述 |
| :------: | :------: | :------: |
|  model | NELyric |   歌词模型
|  startTime | long |   开始时间
|  endTime | long |   结束时间


## 设置歌词展示模式

```
    /**
     * 设置歌词展示模式
     *
     * @param lyricMode 逐行or逐字
     */
    public void setLyricMode(LyricMode lyricMode) {

    }
```

参数描述

| 参数  | 类型 | 描述 |
| :------: | :------: | :------: |
|  lyricMode | LyricMode |   LineByLine 逐行，WordByWord逐字


## 更新当前时间戳

```
    /**
     * 更新当前时间戳
     *
     * @param currentTimeMillis 当前时间戳,单位ms
     */
    public void update(long currentTimeMillis) {
            
    }
```

参数描述

| 参数  | 类型 | 描述 |
| :------: | :------: | :------: |
|  currentTimeMillis | long |   当前时间戳




