// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

/// The mode of the gradient.
typedef NS_ENUM(NSInteger, NEGradientViewMode) {
  /// A linear gradient.
  NEGradientViewModeLinear,
  /// A radial gradient.
  NEGradientViewModeRadial,
};

/// The direction of the gradient.
typedef NS_ENUM(NSInteger, NEGradientViewDirection) {
  /// The gradient is vertical.
  NEGradientViewDirectionVertical,
  /// The gradient is horizontal
  NEGradientViewDirectionHorizontal,
};

/// Simple view for drawing gradients and borders.
@interface NEGradientView : UIView

/// An optional array of `UIColor` objects used to draw the gradient. If the
/// value is `nil`, the `backgroundColor` will be drawn instead of a gradient.
/// The default is `nil`.
@property(nonatomic, strong) NSArray<UIColor *> *colors;

/// An array of `UIColor` objects used to draw the dimmed gradient. If the value
/// is `nil`, `colors` will be converted to grayscale. This will use the same
/// `locations` as `colors`. If length of arrays don't match, bad things will
/// happen. You must make sure the number of dimmed colors equals the number of
/// regular colors.
///
/// The default is `nil`.
@property(nonatomic, strong) NSArray<UIColor *> *dimmedColors;

/// Automatically dim gradient colors when prompted by the system (i.e. when an
/// alert is shown).
///
/// The default is `true`.
@property(nonatomic, assign) bool automaticallyDims;

/// An optional array of `CGFloat`s defining the location of each gradient stop.
///
/// The gradient stops are specified as values between `0` and `1`. The values
/// must be monotonically increasing. If `nil`, the stops are spread uniformly
/// across the range.
///
/// Defaults to `nil`.
@property(nonatomic, strong) NSArray<NSNumber *> *locations;

/// The mode of the gradient. The default is `NEGradientViewModeLinear`.
@property(nonatomic, assign) NEGradientViewMode mode;

/// The direction of the gradient. Only valid for the `NEGradientViewModeLinear`
/// mode. The default is `NEGradientViewDirectionVertical`.
@property(nonatomic, assign) NEGradientViewDirection direction;

/// 1px borders will be drawn instead of 1pt borders. The default is `true`.
@property(nonatomic, assign) bool drawsThinBorders;

/// The top border color. The default is `nil`.
@property(nonatomic, strong) UIColor *topBorderColor;

/// The right border color. The default is `nil`.
@property(nonatomic, strong) UIColor *rightBorderColor;

///  The bottom border color. The default is `nil`.
@property(nonatomic, strong) UIColor *bottomBorderColor;

/// The left border color. The default is `nil`.
@property(nonatomic, strong) UIColor *leftBorderColor;

@end

NS_ASSUME_NONNULL_END
