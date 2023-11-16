// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import UIKit
import IHProgressHUD
import NEUIKit
import NIMSDK
import NECoreKit
import NESocialUIKit
import NEKaraokeUIKit
import NELoginSample

@main
class AppDelegate: UIResponder, UIApplicationDelegate, UNUserNotificationCenterDelegate {
  
  var window: UIWindow?
  
  var reachability: NPTReachability?
  
  func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
    // Override point for customization after application launch.
    
    window = UIWindow(frame: UIScreen.main.bounds)
    
    let tab = UITabBarController()
    tab.tabBar.backgroundColor = UIColor(red: 0.965, green: 0.973, blue: 0.98, alpha: 1)
    
    let homeViewController: NPTHomeViewController = NPTHomeViewController()
    let home = NEUIBackNavigationController(rootViewController: homeViewController)
    
    home.tabBarItem.title = "Recreation".localized
    home.tabBarItem.image = UIImage(named: "home")
    let person = NEUIBackNavigationController(rootViewController: NPTPersonViewController())
    person.tabBarItem.title = "Personal_Center".localized
    person.tabBarItem.image = UIImage(named: "person")
    
    tab.viewControllers = [home, person]
    window?.rootViewController = tab
    window?.makeKeyAndVisible()
    
    IHProgressHUD.set(defaultStyle: .light)
    IHProgressHUD.set(defaultMaskType: .black)
    IHProgressHUD.set(maximumDismissTimeInterval: 1)
    
    setupReachability("163.com")
    startNotifier()
    checkFirstRun()
    baseLogin()
    return true
  }
  
  func baseInit(_ callback:((Bool)->Void)? = nil){
    let config = NELoginSampleConfig()
    config.appKey = Configs.AppKey
    config.appSecret = Configs.AppSecret
    var loginSampleExtras = Configs.extras
    loginSampleExtras["baseUrl"] = Configs.loginSampleBaseUrl
    config.extras = loginSampleExtras
    
    NELoginSample.getInstance().initialize(config) { code, msg, obj in
      if code == 0 {
        NELoginSample.getInstance().createAccount(nil, sceneType: .voiceRoom, userUuid: nil, imToken: nil) { code, msg, account in
          if code == 0{
            print("\(String(describing: account))")
            //获取账号成功
            userUuid = account?.userUuid ?? ""
            userToken = account?.userToken ?? ""
            userName = account?.userName ?? ""
            icon = account?.icon ?? ""
            
            let encoder = JSONEncoder()
            if let jsonData = try? encoder.encode(account) {
              if let jsonString = String(data: jsonData, encoding: .utf8) {
                UserDefaults.standard.setValue(jsonString, forKey: "userInfo")
              }
            }
            
            self.loginRoom(shouldInit: true,callback: callback)
          }else{
            callback?(false)
          }
          
        }
      }else{
        callback?(false)
      }
    }
  }
  
  func baseLogin(){
    UIApplication.shared.keyWindow?.addSubview(loginView)
    //获取本地存储数据
    let accountInfo = UserDefaults.standard.string(forKey: "userInfo")
    if let accountInfo = accountInfo {
      let decoder = JSONDecoder()
      if let jsonData = accountInfo.data(using: .utf8),
         let account:NemoAccount = try? decoder.decode(NemoAccount.self, from: jsonData) {
        userUuid = account.userUuid
        userToken = account.userToken
        userName = account.userName
        icon = account.icon ?? ""
        //存在本地数据，直接进行登录操作
        self.loginRoom(shouldInit: true) { loginSuccess in
          if(loginSuccess){
            DispatchQueue.main.async {
              self.loginView.removeFromSuperview()
            }
          }
        }
      }
    }
  }
  
  //MARK: 登录View
  lazy var loginView:NELoginSampleView = {
    let loginView = NELoginSampleView(frame: UIScreen.main.bounds)
    loginView.setIcon(UIImage(named: "voiceroom_login") ?? UIImage())
    loginView.setTitle("DisplayName".localized)
    loginView.loginCallBack = { _ in
      self.baseInit(){ loginSuccess in
        if(loginSuccess){
          DispatchQueue.main.async {
            self.loginView.removeFromSuperview()
          }
        }else{
          DispatchQueue.main.async {
            IHProgressHUD.showError(withStatus: "登录失败")
          }
        }
      }
    }
    return loginView
  }()
  
  func checkFirstRun() {
    // 值没有实际含义
    if let _ = UserDefaults.standard.value(forKey: "FirstRun") as? Bool {
    } else {
      userAgreementWindow.show()
    }
  }
}

/// 子线程串行去初始化
private let initQueue: DispatchQueue = .init(label: "com.party.init")

/// 初始化
extension AppDelegate {
  
  func initAllModules(_ appKey: String, extra: [String: String] = [String: String](), callback: @escaping (Int, String?) -> Void) {
    var onKaraokeInit = false
    
    func checkCallback() {
      if onKaraokeInit {
        callback(0, nil)
      }
    }
    initKaraoke(appKey, extras: extra) { code, msg, _ in
      if code != 0 {
        callback(code, msg)
      } else {
        initQueue.async {
          onKaraokeInit = true
          checkCallback()
        }
      }
    }
  }
  
  func initKaraoke(_ appKey:String ,extras: [String: String] = [String: String](), callback: @escaping (Int, String?, Any?) -> Void) {
    let config = NEKaraokeKitConfig()
    config.appKey = appKey
    var karaokeExtras = extras
    karaokeExtras["baseUrl"] = Configs.nemoBaseUrl
    config.extras = karaokeExtras
    NEKaraokeUIManager.sharedInstance().initialize(with: config, configId: Configs.karaokeConfigId, callback: callback)
    NEKaraokeUIManager.sharedInstance().delegate = self
  }
}

/// 登录
extension AppDelegate {
  
  public func loginRoom(shouldInit: Bool = false , callback:((Bool)->Void)? = nil ) {
    
    func login() {
      NEKaraokeUIManager.sharedInstance().login(withAccount:userUuid, token: userToken, nickname: userName) { code, msg, _ in
        if code == 0 {
          DispatchQueue.main.async {
            // 刷新头像与昵称
            IHProgressHUD.dismiss()
            NotificationCenter.default.post(name: NSNotification.Name("Logined"), object: nil, userInfo: ["nickname": userName, "avatar": icon ])
          }
          callback?(true)
        } else {
          DispatchQueue.main.async {
            IHProgressHUD.dismiss()
            IHProgressHUD.showError(withStatus: "Login_Failed".localized)
            print("登录失败 code:\(code) msg:\(String(describing: msg))")
          }
          callback?(true)
        }
      }
    }
    IHProgressHUD.show(withStatus: "Logging_In".localized)
    if shouldInit {
      initAllModules(Configs.AppKey, extra: Configs.extras) { code, msg in
        if (code != 0) {
          IHProgressHUD.dismiss()
          print("初始化失败 code:\(code) msg:\(String(describing: msg))")
          callback?(false)
        } else {
          login()
        }
      }
    } else {
      login()
    }
  }
}

/// 网络监听
extension AppDelegate {
  func setupReachability(_ hostName: String) {
    self.reachability = try? NPTReachability(hostname: hostName)
    reachability?.whenReachable = { reachability in
      
    }
    reachability?.whenUnreachable = { reachability in
      
    }
  }
  
  func startNotifier() {
    print("--- start notifier")
    do {
      try reachability?.startNotifier()
    } catch {
      return
    }
  }
  
  func stopNotifier() {
    print("--- stop notifier")
    reachability?.stopNotifier()
  }
  
  func checkNetwork(showHUD: Bool = true) -> Bool {
    if reachability?.connection == .cellular || reachability?.connection == .wifi {
      return true
    }
    if showHUD {
      IHProgressHUD.showError(withStatus: "Net_Error".localized)
    }
    return false
  }
}

extension AppDelegate: NEKaraokeUIDelegate {
  func inOtherRoom() -> Bool {
    false
  }
  
  func leaveOtherRoom(completion: (() -> Void)? = nil) {
    
  }
  
  func onKaraokeJoinRoom() {
  }
  
  func onKaraokeLeaveRoom() {
  }
  
  func onKaraokeClientEvent(_ event: NEKaraokeClientEvent) {
    switch event {
    case .kicOut,.forbidden:
      IHProgressHUD.showError(withStatus: "Kick_Out".localized)
      DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
        if let tab = UIApplication.shared.keyWindow?.rootViewController as? UITabBarController,
           let home = tab.viewControllers?[0] as? UINavigationController,
           let person = tab.viewControllers?[2] as? UINavigationController{
          home.popToRootViewController(animated: false)
          person.popToRootViewController(animated: false)
          tab.selectedIndex = 0
        }
        UserDefaults.standard.setValue("", forKey: "userInfo")
        UIApplication.shared.keyWindow?.addSubview(self.loginView)
        print("账号退出登录,请重新启动")
      }
    case .loggedIn:
      print("登录成功")
    default: break
    }
  }
}
