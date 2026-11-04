#!/usr/bin/env ruby
# NoDopamine.xcodeproj 를 새로 만드는 스크립트.
#
# 보통은 이미 만들어진 NoDopamine.xcodeproj 를 열기만 하면 돼서 실행할 필요가 없어요.
# 파일을 추가/삭제해서 프로젝트를 다시 만들어야 할 때만 쓰세요:
#   gem install xcodeproj
#   ruby scripts/generate_project.rb

require 'xcodeproj'
require 'fileutils'

# 한글이 들어간 설정값을 제대로 읽고 쓰기 위해
Encoding.default_external = Encoding::UTF_8
Encoding.default_internal = Encoding::UTF_8

ROOT = File.expand_path('..', __dir__)
PROJECT_PATH = File.join(ROOT, 'NoDopamine.xcodeproj')
IOS_VERSION = '17.0'

FileUtils.rm_rf(PROJECT_PATH)
project = Xcodeproj::Project.new(PROJECT_PATH)
project.root_object.attributes['LastSwiftUpdateCheck'] = '1600'
project.root_object.attributes['LastUpgradeCheck'] = '1600'
project.root_object.development_region = 'ko'
project.root_object.known_regions = %w[ko en Base]

# 모든 타깃이 Config.xcconfig 값을 쓰도록 연결
config_ref = project.main_group.new_file('Config.xcconfig')
project.build_configurations.each do |config|
  config.base_configuration_reference = config_ref
end

def swift_files(dir)
  Dir.glob(File.join(ROOT, dir, '**', '*.swift')).sort
end

# 폴더 구조 그대로 Xcode 그룹을 만들고, 파일을 넣어요.
def add_files(project, dir, paths)
  group = project.main_group.find_subpath(dir, true)
  group.set_source_tree('<group>')
  group.set_path(dir)
  paths.map do |abs|
    rel = abs.sub(File.join(ROOT, dir) + '/', '')
    parts = rel.split('/')
    sub = group
    parts[0..-2].each do |p|
      sub = sub.children.find { |c| c.display_name == p && c.isa == 'PBXGroup' } ||
            sub.new_group(p, p)
    end
    sub.files.find { |f| f.path == parts.last } || sub.new_reference(parts.last)
  end
end

shared_refs = add_files(project, 'Shared', swift_files('Shared'))

common_settings = {
  'SWIFT_VERSION' => '5.0',
  'TARGETED_DEVICE_FAMILY' => '1',
  'IPHONEOS_DEPLOYMENT_TARGET' => IOS_VERSION,
  'CODE_SIGN_STYLE' => 'Automatic',
  'MARKETING_VERSION' => '1.0',
  'CURRENT_PROJECT_VERSION' => '1',
  'GENERATE_INFOPLIST_FILE' => 'YES',
  'SWIFT_EMIT_LOC_STRINGS' => 'YES',
}

# ── 1) 앱 본체 ─────────────────────────────────────────
app = project.new_target(:application, 'NoDopamine', :ios, IOS_VERSION)
app_refs = add_files(project, 'NoDopamine', swift_files('NoDopamine'))
app.add_file_references(app_refs + shared_refs)
app_group = project.main_group['NoDopamine']
assets = app_group.new_reference('Assets.xcassets')
app.resources_build_phase.add_file_reference(assets)
app_group.new_reference('Info.plist')
app_group.new_reference('NoDopamine.entitlements')

app.build_configurations.each do |c|
  c.build_settings.merge!(common_settings)
  c.build_settings.merge!(
    'PRODUCT_NAME' => '$(TARGET_NAME)',
    'PRODUCT_BUNDLE_IDENTIFIER' => '$(BUNDLE_ID_PREFIX).nodopamine',
    'INFOPLIST_FILE' => 'NoDopamine/Info.plist',
    'CODE_SIGN_ENTITLEMENTS' => 'NoDopamine/NoDopamine.entitlements',
    'INFOPLIST_KEY_CFBundleDisplayName' => 'NoDopamine',
    'INFOPLIST_KEY_UILaunchScreen_Generation' => 'YES',
    'INFOPLIST_KEY_UIApplicationSceneManifest_Generation' => 'YES',
    'INFOPLIST_KEY_UIApplicationSupportsIndirectInputEvents' => 'YES',
    'INFOPLIST_KEY_UISupportedInterfaceOrientations' => 'UIInterfaceOrientationPortrait',
    'ASSETCATALOG_COMPILER_APPICON_NAME' => 'AppIcon',
    'ASSETCATALOG_COMPILER_GLOBAL_ACCENT_COLOR_NAME' => 'AccentColor',
    'ENABLE_PREVIEWS' => 'YES',
    'LD_RUNPATH_SEARCH_PATHS' => ['$(inherited)', '@executable_path/Frameworks'],
  )
end

embed_phase = app.new_copy_files_build_phase('Embed Foundation Extensions')
embed_phase.symbol_dst_subfolder_spec = :plug_ins

# ── 2) 확장 프로그램 3개 ────────────────────────────────
extensions = [
  { name: 'NoDopamineMonitor',      suffix: 'monitor',      display: 'NoDopamine 예약 감시' },
  { name: 'NoDopamineShield',       suffix: 'shield',       display: 'NoDopamine 차단 화면' },
  { name: 'NoDopamineShieldAction', suffix: 'shieldaction', display: 'NoDopamine 차단 버튼' },
]

extensions.each do |ext|
  target = project.new_target(:app_extension, ext[:name], :ios, IOS_VERSION)
  refs = add_files(project, ext[:name], swift_files(ext[:name]))
  target.add_file_references(refs + shared_refs)
  group = project.main_group[ext[:name]]
  group.new_reference('Info.plist')
  group.new_reference("#{ext[:name]}.entitlements")

  target.build_configurations.each do |c|
    c.build_settings.merge!(common_settings)
    c.build_settings.merge!(
      'PRODUCT_NAME' => '$(TARGET_NAME)',
      'PRODUCT_BUNDLE_IDENTIFIER' => "$(BUNDLE_ID_PREFIX).nodopamine.#{ext[:suffix]}",
      'INFOPLIST_FILE' => "#{ext[:name]}/Info.plist",
      'CODE_SIGN_ENTITLEMENTS' => "#{ext[:name]}/#{ext[:name]}.entitlements",
      'INFOPLIST_KEY_CFBundleDisplayName' => ext[:display],
      'SKIP_INSTALL' => 'YES',
      'LD_RUNPATH_SEARCH_PATHS' => ['$(inherited)', '@executable_path/Frameworks',
                                   '@executable_path/../../Frameworks'],
    )
  end

  app.add_dependency(target)
  build_file = embed_phase.add_file_reference(target.product_reference, true)
  build_file.settings = { 'ATTRIBUTES' => ['RemoveHeadersOnCopy'] }
end

# 부가 파일 (빌드에는 안 들어가고 Xcode 에서 보기 편하게)
project.main_group.new_file('README.md')

# xcodeproj 가 자동으로 넣는 Foundation.framework 참조는 특정 SDK 버전 경로에 묶여 있어서
# 다른 버전의 Xcode 에서 깨질 수 있어요. Swift 는 프레임워크를 자동으로 연결하니 지워요.
project.targets.each do |t|
  t.frameworks_build_phase.files.dup.each(&:remove_from_project)
end
project.frameworks_group.files.dup.each(&:remove_from_project)
project.frameworks_group.groups.dup.each(&:remove_from_project)

project.sort(groups_position: :above)
project.save

# 공유 스킴 — Xcode 에서 열자마자 ▶︎ 실행 버튼이 바로 동작하도록
scheme = Xcodeproj::XCScheme.new
scheme.configure_with_targets(app, nil, launch_target: true)
scheme.save_as(PROJECT_PATH, 'NoDopamine', true)

puts "✅ #{PROJECT_PATH} 생성 완료"
