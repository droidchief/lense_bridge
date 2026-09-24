import 'package:flutter/services.dart';

class CameraController {
  static const MethodChannel _channel = MethodChannel(
    'lensbridge/camera_controller',
  );

  Future<void> switchLens() async {
    _channel.invokeMethod('switchLens');
  }

  Future<void> startCamera() async {
    _channel.invokeMethod('startCamera');
  }

  Future<void> stopCamera() async {
    _channel.invokeMethod('stopCamera');
  }
}
