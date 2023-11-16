# NEKaraokeKit

> NEKaraokeKit 集成房间管理，麦位管理，融合IM、RTC等功能 支撑上层不同场景方案快速搭建应用解决方案。

## Change Log

[change log](CHANGELOG.md)

## 本地引用

### 其他Kit引用
如果是其他Kit引用NEKaraokeKit，就在对应Kit的podspec文件中添加依赖。

```
  s.dependency 'NEKaraokeKit'
```

由于podspec中无法通过路径来依赖本地的pod库，所以，需要在根目录的pod文件中找到对应的example工程来添加对该Kit的依赖。

```
  pod 'NEKaraokeKit', :path => 'KaraokeKit/NEKaraokeKit/NEKaraokeKit.podspec'
```

## Pod引用
- 打开 Podfile 文件，在对应的target中 添加pod依赖，具体内容如下
    pod 'NEKaraokeKit'
    
## 编译
- 在根目录执行pod install，运行NEKaraoke工程，确保本地工作正常。

- 打开项目根目录，运行build_frame.sh 脚本，具体执行命令如下
    sh build_frame.sh  --project Pods/Pods.xcodeproj  --targetName NEKaraokeKit --version x.x.x -z
- 完成上一步，根目录下会生成build目录，对应的frameWork即指定的 target frameWork
    
## 发布
- 将打包的zip发给具备admin sdk管理权限的同事
- 将zip上传到admin的NEKaraokeKit(上传SDK时自定义SDK种类填NEKaraoke)目录下，获得文件链接
- 打开项目根目录，进入到Podspecs文件夹 , 打开 NEKaraokeKit.podspec 文件，修改 s.source 内容（修改为编译步骤中打包完成的包上传地址），并确认版本号
- 打开终端 ，运行命令 pod trunk me
- 终端中 ，cd 到 项目根目录，再进入 Podspecs，运行命令 pod trunk push NEKaraokeKit.podspec
- 如发生错发，使用 pod trunk delete NEKaraokeKit xxx(版本号) 的命令来进行删除



