#
# Be sure to run `pod lib lint NEKaraokeKit.podspec' to ensure this is a
# valid spec before submitting.
#
# Any lines starting with a # are optional, but their use is encouraged
# To learn more about a Podspec see https://guides.cocoapods.org/syntax/podspec.html
#
require_relative "../../PodConfigs/config_podspec.rb"
require_relative "../../PodConfigs/config_third.rb"
require_relative "../../PodConfigs/config_local_social.rb"

Pod::Spec.new do |s|
  s.name             = 'NEKaraokeKit'
  s.version          = '1.4.0'
  s.summary          = 'A short description of NEKaraokeKit.'
  s.homepage         = YXConfig.homepage
  s.license          = YXConfig.license
  s.author           = YXConfig.author
  s.ios.deployment_target = YXConfig.deployment_target
  s.swift_version = YXConfig.swift_version

  if ENV["USE_SOURCE_FILES"] == "true"
    s.source = { :git => "https://github.com/netease-kit/" }
    
    s.source_files = 'NEKaraokeKit/Classes/**/*'
    s.dependency NERoomKit.Special_All
    s.dependency NECoreKit.name
    s.dependency NECopyrightedMedia.name
  else
    
  end
  YXConfig.pod_target_xcconfig(s)

end
