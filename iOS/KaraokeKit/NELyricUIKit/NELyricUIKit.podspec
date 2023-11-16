#
# Be sure to run `pod lib lint NELyricUIKit.podspec' to ensure this is a
# valid spec before submitting.
#
# Any lines starting with a # are optional, but their use is encouraged
# To learn more about a Podspec see https://guides.cocoapods.org/syntax/podspec.html
#

Pod::Spec.new do |s|
  s.name             = 'NELyricUIKit'
  s.version          = '1.4.0'
  s.summary          = 'A short description of NELyricUIKit.'
  s.homepage         = YXConfig.homepage
  s.license          = YXConfig.license
  s.author           = YXConfig.author
  s.ios.deployment_target = YXConfig.deployment_target
  
  if ENV["USE_SOURCE_FILES"] == "true"
    s.source = { :git => "https://github.com/netease-kit/" }
    
    s.source_files = 'NELyricUIKit/Classes/**/*'
    s.project_header_files = 'NELyricUIKit/Classes/Project/*.h'
    s.dependency NECopyrightedMedia.name
    s.dependency YYText.name
    s.dependency Masonry.name
    s.dependency BlocksKit.name
  else
    
  end
  YXConfig.pod_target_xcconfig(s)
end
