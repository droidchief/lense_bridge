import 'package:flutter/material.dart';

class CameraView extends StatelessWidget {
  const CameraView({super.key});

  static const String viewType = "lensbridge/camera_preview";

  @override
  Widget build(BuildContext context) {
    return AndroidView(viewType: viewType, layoutDirection: TextDirection.ltr);
  }
}
