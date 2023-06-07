//
//  CameraManager.swift
//  MetaChatDemo
//
//  Created by ZhouRui on 2023/3/14.
//

import Foundation
import UIKit
import AVFoundation

class CameraManager: NSObject {
    
    private let captureSession = AVCaptureSession()
    private let sessionQueue = DispatchQueue(label: "cameraManager.sessionQueue")
    private var videoDeviceInput: AVCaptureDeviceInput!
    private var videoDataOutput: AVCaptureVideoDataOutput!
    private let videoDataOutputQueue = DispatchQueue(label: "cameraManager.videoDataOutputQueue")
    private var previewLayer: AVCaptureVideoPreviewLayer!
    
    override init() {
        super.init()
        checkCameraAuthorizationStatus()
        configureSession()
    }
    
    private func checkCameraAuthorizationStatus() {
        let status = AVCaptureDevice.authorizationStatus(for: .video)
        switch status {
        case .authorized:
            // The user has previously granted access to the camera.
            return
        case .notDetermined:
            // The user has not yet been asked for camera access.
            sessionQueue.suspend()
            AVCaptureDevice.requestAccess(for: .video) { [weak self] granted in
                guard let self = self else { return }
                if !granted {
                    //
                }
                self.sessionQueue.resume()
            }
        case .denied, .restricted:
            // The user has previously denied or restricted access.
            return
        @unknown default:
            fatalError("Unknown authorization status")
        }
    }
    
    private func configureSession() {
        sessionQueue.async {
            self.captureSession.beginConfiguration()
            self.captureSession.sessionPreset = .high
            
            guard let videoDevice = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .front) else {
                fatalError("No video device found")
            }
            
            do {
                let videoDeviceInput = try AVCaptureDeviceInput(device: videoDevice)
                if self.captureSession.canAddInput(videoDeviceInput) {
                    self.captureSession.addInput(videoDeviceInput)
                    self.videoDeviceInput = videoDeviceInput
                } else {
                    fatalError("Could not add video device input to the session")
                }
                
                let videoDataOutput = AVCaptureVideoDataOutput()
                videoDataOutput.videoSettings = [
                    (kCVPixelBufferPixelFormatTypeKey as String): NSNumber(value: kCVPixelFormatType_32BGRA)
                ]
                videoDataOutput.alwaysDiscardsLateVideoFrames = true
                videoDataOutput.setSampleBufferDelegate(self, queue: self.videoDataOutputQueue)
                if self.captureSession.canAddOutput(videoDataOutput) {
                    self.captureSession.addOutput(videoDataOutput)
                    self.videoDataOutput = videoDataOutput
                } else {
                    fatalError("Could not add video data output to the session")
                }
                
                self.previewLayer = AVCaptureVideoPreviewLayer(session: self.captureSession)
                self.previewLayer.videoGravity = .resizeAspectFill
                self.previewLayer.connection?.videoOrientation = .landscapeRight
            } catch {
                fatalError("Could not create video device input: \(error)")
            }
            
            self.captureSession.commitConfiguration()
        }
    }
    
    func startRunning() {
        sessionQueue.async {
            if !self.captureSession.isRunning {
                self.captureSession.startRunning()
            }
        }
    }
    
    func stopRunning() {
        sessionQueue.async {
            if self.captureSession.isRunning {
                self.captureSession.stopRunning()
            }
        }
    }
        
    func getPreviewLayer() -> AVCaptureVideoPreviewLayer? {
        return previewLayer
    }
}

extension CameraManager: AVCaptureVideoDataOutputSampleBufferDelegate {
    func captureOutput(_ output: AVCaptureOutput, didOutput sampleBuffer: CMSampleBuffer, from connection: AVCaptureConnection) {
        
    }
}
