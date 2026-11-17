// Renders the Dice Poker app icon (opaque PNG, no alpha) — same design as the Android adaptive icon.
// Usage: swift scripts/make_app_icon.swift iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/AppIcon.png 1024
import CoreGraphics
import Foundation
import ImageIO
import UniformTypeIdentifiers

let args = CommandLine.arguments
let out = args[1]
let px = Int(args[2])!
let s = CGFloat(px)

let space = CGColorSpace(name: CGColorSpace.sRGB)!
let ctx = CGContext(data: nil, width: px, height: px, bitsPerComponent: 8, bytesPerRow: 0,
                    space: space, bitmapInfo: CGImageAlphaInfo.noneSkipLast.rawValue)!
func rgb(_ hex: UInt32, _ a: CGFloat = 1) -> CGColor {
    CGColor(colorSpace: space, components: [CGFloat((hex >> 16) & 0xFF) / 255, CGFloat((hex >> 8) & 0xFF) / 255, CGFloat(hex & 0xFF) / 255, a])!
}

// Flip to a top-left origin so the geometry matches the Android vector (108 unit canvas)
ctx.translateBy(x: 0, y: s)
ctx.scaleBy(x: 1, y: -1)
let u = s / 108

// Felt background
let felt = CGGradient(colorsSpace: space, colors: [rgb(0x2F8A5B), rgb(0x17573A)] as CFArray, locations: [0, 1])!
ctx.drawRadialGradient(felt, startCenter: CGPoint(x: s / 2, y: s / 2), startRadius: 0,
                       endCenter: CGPoint(x: s / 2, y: s / 2), endRadius: 76 * u, options: [.drawsAfterEndLocation])

// iOS shows the icon full-bleed, so the die can be a bit larger than in the Android safe zone
let scale: CGFloat = 1.18
ctx.translateBy(x: s / 2, y: s / 2)
ctx.rotate(by: -12 * .pi / 180)
ctx.scaleBy(x: scale, y: scale)
ctx.translateBy(x: -s / 2, y: -s / 2)

let die = CGRect(x: 29 * u, y: 29 * u, width: 50 * u, height: 50 * u)
let diePath = CGPath(roundedRect: die, cornerWidth: 10 * u, cornerHeight: 10 * u, transform: nil)

// Soft shadow (shadow offsets are in device space, where y points up)
ctx.saveGState()
ctx.setShadow(offset: CGSize(width: 1.5 * u, height: -4 * u), blur: 6 * u, color: rgb(0x000000, 0.45))
ctx.addPath(diePath)
ctx.setFillColor(rgb(0xFFFFFF))
ctx.fillPath()
ctx.restoreGState()

// Body gradient
ctx.saveGState()
ctx.addPath(diePath)
ctx.clip()
let body = CGGradient(colorsSpace: space, colors: [rgb(0xFFFFFF), rgb(0xE2E4EA)] as CFArray, locations: [0, 1])!
ctx.drawLinearGradient(body, start: CGPoint(x: die.minX, y: die.minY), end: CGPoint(x: die.maxX, y: die.maxY), options: [])
ctx.restoreGState()

// Pips (five)
ctx.setFillColor(rgb(0x1A1A2E))
let r = 4.6 * u
for (fx, fy) in [(0.27, 0.27), (0.73, 0.27), (0.5, 0.5), (0.27, 0.73), (0.73, 0.73)] {
    let c = CGPoint(x: die.minX + die.width * fx, y: die.minY + die.height * fy)
    ctx.fillEllipse(in: CGRect(x: c.x - r, y: c.y - r, width: 2 * r, height: 2 * r))
}

let image = ctx.makeImage()!
let dest = CGImageDestinationCreateWithURL(URL(fileURLWithPath: out) as CFURL, UTType.png.identifier as CFString, 1, nil)!
CGImageDestinationAddImage(dest, image, nil)
CGImageDestinationFinalize(dest)
print("wrote \(out)")
