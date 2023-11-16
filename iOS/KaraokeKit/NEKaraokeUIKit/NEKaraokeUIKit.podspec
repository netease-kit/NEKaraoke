#
# Be sure to run `pod lib lint NEKaraokeUIKit.podspec' to ensure this is a
# valid spec before submitting.
#
# Any lines starting with a # are optional, but their use is encouraged
# To learn more about a Podspec see https://guides.cocoapods.org/syntax/podspec.html
#

Pod::Spec.new do |s|
  s.name             = 'NEKaraokeUIKit'
  s.version          = '1.4.0'
  s.summary          = 'A short description of NEKaraokeUIKit.'
  s.homepage         = YXConfig.homepage
  s.license          = YXConfig.license
  s.author           = YXConfig.author
  s.ios.deployment_target = YXConfig.deployment_target
  
  if ENV["USE_SOURCE_FILES"] == "true"
    s.source = { :git => "https://github.com/netease-kit/" }
    
    s.source_files = 'NEKaraokeUIKit/Classes/**/*'
    s.resource = 'NEKaraokeUIKit/Assets/**/*'
    s.dependency NEKaraokeKit.name
    s.dependency Masonry.name
    s.dependency BlocksKit.name
    s.dependency NELyricUIKit.name
    s.dependency NEAudioEffectUIKit.name
    s.dependency NEAudioEffectKit.name
    s.dependency NECopyrightedMedia.name
    s.dependency LottieOC.name, LottieOC.version
    s.dependency MJRefresh.name
    s.dependency SDWebImage.name
    s.dependency NEPitchUIKit.name
    s.dependency NECoreKit.name
    s.dependency NESocialUIKit.name
    s.dependency NECommonUIKit.name
    s.dependency NEUIKit.name
  else
    
  end
  YXConfig.pod_target_xcconfig(s)
end
