// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

// MARK: - PathNode

protocol PathNode {
  var pathOutput: PathOutputNode { get }
}

extension PathNode where Self: AnimatorNode {
  var outputNode: NodeOutput {
    pathOutput
  }
}
