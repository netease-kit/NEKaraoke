// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEWebViewController.h"
#import <WebKit/WebKit.h>

@interface NEWebViewController ()

@property(strong, nonatomic) WKWebView *webview;
@property(strong, nonatomic) NSString *urlString;

@end

@implementation NEWebViewController

- (instancetype)initWithUrlString:(NSString *)urlString {
  self = [super init];
  if (self) {
    self.urlString = urlString;
  }
  return self;
}
- (void)viewDidLoad {
  [super viewDidLoad];
  [self.view addSubview:self.webview];
  [self loadWebView];
}

- (void)loadWebView {
  NSURLRequest *request = [[NSURLRequest alloc] initWithURL:[NSURL URLWithString:self.urlString]];
  [self.webview loadRequest:request];
}
- (WKWebView *)webview {
  if (!_webview) {
    _webview = [[WKWebView alloc] initWithFrame:self.view.frame];
  }
  return _webview;
}

@end
