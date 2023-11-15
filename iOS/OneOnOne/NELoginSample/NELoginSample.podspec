Pod::Spec.new do |s|
  s.name = 'NELoginSample'
  s.version = '1.0.0'
  s.summary = 'A short description of NELoginSample.'
  s.description = 'TODO: Add long description of the pod here.'
  s.homepage = 'https://github.com/mayajie@gmail.com/NELoginSample'
  s.license = {"type"=> "MIT", "file"=> "LICENSE"}
  s.author = '{"mayajie@gmail.com"=> "mayajie@corp.netease.com"}'
  s.source = {"git"=> "https://github.com/mayajie@gmail.com/NELoginSample.git", "tag"=> "1.0.0"}
  s.platforms = {"ios"=> "10.0"}
  s.swift_versions = '5.0'
  s.dependency 'NECommonKit'
  s.dependency 'SnapKit'
  s.dependency 'NECoreKit'
  s.source_files = 'NELoginSample/Classes/**/*'
  s.resources = 'NELoginSample/Assets/**/*'
  s.pod_target_xcconfig = {"EXCLUDED_ARCHS[sdk=iphonesimulator*]"=> "arm64", "BUILD_LIBRARY_FOR_DISTRIBUTION"=> "YES"}
  s.swift_version = '5.0'
end
