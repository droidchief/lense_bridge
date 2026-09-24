import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'camera_controller.dart';

class CameraView extends StatefulWidget {
  const CameraView({super.key});

  @override
  State<CameraView> createState() => _CameraViewState();
}

class _CameraViewState extends State<CameraView> {
  static const String viewType = 'lensbridge/camera_preview';
  final CameraController _controller = CameraController();

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        const Expanded(
          child: AndroidView(
            viewType: viewType,
            layoutDirection: TextDirection.ltr,
          ),
        ),
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: [
            ElevatedButton(
              onPressed: _controller.switchLens,
              child: const Text('Switch Lens'),
            ),
            ElevatedButton(
              onPressed: _controller.stopCamera,
              child: const Text('Stop'),
            ),
            ElevatedButton(
              onPressed: _controller.startCamera,
              child: const Text('Start'),
            ),
          ],
        ),
      ],
    );
  }
}