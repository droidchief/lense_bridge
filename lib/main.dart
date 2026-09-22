import 'package:flutter/material.dart';
import 'package:lens_bridge/camera_view.dart';

void main() {
  runApp(const LensBridgeApp());
}

class LensBridgeApp extends StatelessWidget {
  const LensBridgeApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(colorScheme: .fromSeed(seedColor: Colors.deepPurple)),
      home: Scaffold(
        body: CameraView(),
      ),
    );
  }
}

