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

# This description is used to generate tags and improve search results.
#   * Think: What does it do? Why did you write it? What is the focus?
#   * Try to keep it short, snappy and to the point.
#   * Write the description between the DESC delimiters below.
#   * Finally, don't worry about the indent, CocoaPods strips it!

  s.description      = <<-DESC
TODO: Add long description of the pod here.
                       DESC

  s.homepage         = 'https://github.com/Ginger/NEKaraokeUIKit'
  # s.screenshots     = 'www.example.com/screenshots_1', 'www.example.com/screenshots_2'
  s.license          = { :type => 'MIT', :file => 'LICENSE' }
  s.author           = { 'Ginger' => 'jinjie03@corp.netease.com' }
  s.source           = { :git => 'https://github.com/Ginger/NEKaraokeUIKit.git', :tag => s.version.to_s }
  # s.social_media_url = 'https://twitter.com/<TWITTER_USERNAME>'

  s.ios.deployment_target = '10.0'

  s.source_files = 'NEKaraokeUIKit/Classes/**/*'
  
  s.resource_bundles = {
     'NEKaraokeUIKit' => ['NEKaraokeUIKit/Assets/**/*']
  }

  # s.public_header_files = 'Pod/Classes/**/*.h'
  # s.frameworks = 'UIKit', 'MapKit'
  s.dependency 'NEKaraokeKit', '1.4.0'
  s.dependency 'Masonry'
  s.dependency 'BlocksKit'
  s.dependency 'libextobjc'
  s.dependency 'NELyricUIKit', '1.4.0'
  s.dependency 'M80AttributedLabel'
  s.dependency 'NEAudioEffectUIKit', '1.4.0'
  s.dependency 'NEAudioEffectKit', '1.4.0'
  s.dependency 'NECopyrightedMedia', '1.4.0'
  s.dependency 'lottie-ios', '2.5.3'
  s.dependency 'Toast'
  s.dependency 'MJRefresh'
  s.dependency 'SDWebImage'
  s.dependency 'NEPitchUIKit', '1.4.0'
  s.dependency 'YXAlog_iOS'
#  s.dependency 'NEPitchKit_framework'
end
