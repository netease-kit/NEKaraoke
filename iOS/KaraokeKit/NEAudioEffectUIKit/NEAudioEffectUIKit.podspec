#
# Be sure to run `pod lib lint NEAudioEffectUIKit.podspec' to ensure this is a
# valid spec before submitting.
#
# Any lines starting with a # are optional, but their use is encouraged
# To learn more about a Podspec see https://guides.cocoapods.org/syntax/podspec.html
#
require_relative "../../PodConfigs/config_podspec.rb"
require_relative "../../PodConfigs/config_third.rb"
require_relative "../../PodConfigs/config_local_social.rb"

Pod::Spec.new do |s|
  s.name             = 'NEAudioEffectUIKit'
  s.version          = '1.4.0'
  s.summary          = 'A short description of NEAudioEffectUIKit.'
  s.homepage         = YXConfig.homepage
  s.license          = YXConfig.license
  s.author           = YXConfig.author
  s.ios.deployment_target = YXConfig.deployment_target
  
  if ENV["USE_SOURCE_FILES"] == "true"
    s.source = { :git => "https://github.com/netease-kit/" }
    s.source_files = 'NEAudioEffectUIKit/Classes/**/*'
    s.resource = 'NEAudioEffectUIKit/Assets/**/*'
    s.dependency Masonry.name
    s.dependency BlocksKit.name
    s.dependency NEAudioEffectKit.name
  else
    
  end
  YXConfig.pod_target_xcconfig(s)
end
