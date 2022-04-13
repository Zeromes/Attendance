package com.example.attendance.ui.profile.faceRecognize

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.attendance.R
import com.example.attendance.utils.BitmapUtils.getBitmap
import com.example.attendance.utils.MatJsonUtils.matFromJson
import com.example.attendance.utils.MatJsonUtils.matToJson
import com.google.common.util.concurrent.ListenableFuture
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.FaceDetectorYN
import org.opencv.objdetect.FaceRecognizerSF
import org.opencv.objdetect.FaceRecognizerSF.FR_NORM_L2
import java.io.File
import java.io.FileOutputStream
import java.nio.file.FileStore

class FaceRecognitionActivity : AppCompatActivity() {
    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>
    private var faceDetector : FaceDetectorYN? = null
    private var faceRecognizer : FaceRecognizerSF? = null

    private var tempFaceFeature : Mat? = null

    private var l2ScoreThreshold : Double = 0.6

    private var faceScore : Int = 0
    private val faceScoreThreshold : Int = 10
    private var recognizedFailedTime : Int = 0
    private var recognizedFailedTimeThreshold : Int = 3

    private var recognizeFinished = false

    private fun bindPreviewAndSetAnalysis(cameraProvider : ProcessCameraProvider) {
        //创建一个CameraX的Preview对象
        var preview : Preview = Preview.Builder()//获取Builder
            .build()//并build
        //创建一个CameraSelector对象
        var cameraSelector : CameraSelector = CameraSelector.Builder()//获取Builder
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)//设置镜头
            .build()//并build
        //构建preview。调用preview对象的setSurfaceProvider方法，将相机画面赋给previewView
        preview.setSurfaceProvider(findViewById<PreviewView>(R.id.previewView).surfaceProvider)

        val imageAnalysis = ImageAnalysis.Builder()
            // enable the following line if RGBA output is needed.
            //.setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            //.setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalysis.setAnalyzer(mainExecutor, ImageAnalysis.Analyzer { imageProxy ->
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            // insert your code here.
            // Log.i("imageProxyHeight",imageProxy.height.toString())
            // Log.i("imageProxyWidth",imageProxy.width.toString())
            if(!recognizeFinished){
                //进行人脸识别
                if(faceDetector == null || faceRecognizer == null){//首次运行，准备文件和识别器
                    //准备文件
                    val dis = resources.openRawResource(R.raw.face_detection_yunet_2021dec)
                    val detectorDir = getDir("onnxModel", MODE_PRIVATE)
                    val mDetectorFile = File(detectorDir, "face_detection_yunet_2021dec.onnx")
                    val dOS = FileOutputStream(mDetectorFile)
                    val dBuffer = ByteArray(4096)
                    var dBytesRead: Int
                    while (dis.read(dBuffer).also { dBytesRead = it } != -1) {
                        dOS.write(dBuffer, 0, dBytesRead)
                    }
                    dis.close()
                    dOS.close()
                    val ris = resources.openRawResource(R.raw.face_recognition_sface_2021dec)
                    val recognizerDir = getDir("onnxModel", MODE_PRIVATE)
                    val mRecognizerFile = File(recognizerDir, "face_recognition_sface_2021dec.onnx")
                    val rOS = FileOutputStream(mRecognizerFile)
                    val rBuffer = ByteArray(4096)
                    var rBytesRead: Int
                    while (ris.read(rBuffer).also { rBytesRead = it } != -1) {
                        rOS.write(rBuffer, 0, rBytesRead)
                    }
                    ris.close()
                    rOS.close()

                    faceDetector = FaceDetectorYN.create(
                        mDetectorFile.absolutePath,
                        "",
                        org.opencv.core.Size(imageProxy.height.toDouble(), imageProxy.width.toDouble())//旋转了90度
                    )
                    Log.i("faceDetector","初始化了faceDetector，width：${imageProxy.width.toDouble()}，height：${imageProxy.height.toDouble()}")
                    faceRecognizer = FaceRecognizerSF.create(mRecognizerFile.absolutePath,"")
                    Log.i("faceDetector","初始化了faceRecognizer")
                }
                else{
                    val faceDetected : Mat = Mat()
                    val image : Mat = Mat()

                    //从imageProxy获取Bitmap
                    @ExperimentalGetImage
                    val imageBitmap : Bitmap? = getBitmap(imageProxy)
                    //将Bitmap转为Mat
                    Utils.bitmapToMat(imageBitmap,image)
                    //将色彩空间转为BGR
                    Imgproc.cvtColor(image, image, Imgproc.COLOR_RGBA2RGB)
                    //进行人脸检测并将人脸数据保存进face的Mat
                    faceDetector!!.detect(image,faceDetected)

                    //Log.i("faceDetector",face!!.rows().toString())

                    if(faceDetected!!.rows()!=1){
                        //Log.i("faceDetector","未检测到人脸")
                        faceScore = 0
                        findViewById<TextView>(R.id.faceRecognizeNoteTextView).setText(R.string.face_detect_not_found_note)
                    }
                    else{

                        //Log.i("faceDetector","检测到人脸！得分：${faceDetected[0,14][0]}")
                        findViewById<TextView>(R.id.faceRecognizeNoteTextView).setText(R.string.face_detect_detecting_note)
                        if(faceDetected[0,14][0]>0.99){//得分较高
                            //对人脸进行特征提取
                            //进行人脸对齐
                            val face : Mat = Mat()
                            faceRecognizer!!.alignCrop(image,faceDetected.row(0),face)
                            //获取人脸特征
                            if(tempFaceFeature == null){//首次获取特征
                                val faceFeature : Mat = Mat()
                                faceRecognizer!!.feature(face,faceFeature)
                                tempFaceFeature = faceFeature
                            }
                            else{//和之前获取的特征进行对比
                                val faceFeature : Mat = Mat()
                                faceRecognizer!!.feature(face,faceFeature)
                                val l2Score =  faceRecognizer!!.match(faceFeature, tempFaceFeature, FR_NORM_L2)
                                //Log.i("faceDetector","与上一次获得的人脸的相似得分：${l2Score}")
                                if(l2Score <= l2ScoreThreshold) {
                                    // 同一个人
                                    tempFaceFeature = faceFeature
                                    faceScore += 1
                                    if(faceScore > faceScoreThreshold){//确认是可用的数据
                                        //Log.i("faceDetector","人脸特征转码成的JSON：${matToJson(tempFaceFeature!!)}")
                                        //返回给上一个activity
                                        //获取模式
                                        val mode = intent.getStringExtra("mode")
                                        if(mode == "getFeature"){//返回一个特征Mat
                                            val intent = Intent().apply {
                                                putExtra("feature",matToJson(tempFaceFeature!!))
                                            }
                                            setResult(1,intent)
                                            recognizeFinished = true
                                            finish()
                                        }
                                        else if(mode == "matchFeature"){//需要返回一个识别结果时
                                            val originFeature = intent.getStringExtra("originFeature")
                                            val score =  faceRecognizer!!.match(matFromJson(originFeature), tempFaceFeature, FR_NORM_L2)
                                            Log.i("matchFeature","源数据：${originFeature}")
                                            Log.i("matchFeature","检测到的数据：${matToJson(tempFaceFeature!!)}")
                                            Log.i("matchFeature","对比分数：${score}")
                                            //对比来源脸
                                            if(score<l2ScoreThreshold){//符合
                                                Log.i("matchFeature","符合")
                                                val intent = Intent().apply {
                                                    putExtra("matchResult","ture")
                                                }
                                                setResult(2,intent)
                                                recognizeFinished = true
                                                finish()
                                            }
                                            else{//不符合
                                                //重置识别
                                                recognizedFailedTime += 1
                                                faceScore = 0
                                                tempFaceFeature = null
                                                if(recognizedFailedTime > recognizedFailedTimeThreshold){//不符合次数超过阈值
                                                    val intent = Intent().apply {
                                                        putExtra("matchResult","false")
                                                    }
                                                    setResult(2,intent)
                                                    recognizeFinished = true
                                                    finish()
                                                }
                                                Log.i("matchFeature","不符合")
                                            }
                                        }
                                    }
                                } else {
                                    // 不同人
                                    faceScore = 0
                                    tempFaceFeature = null
                                }
                            }
                        }
                        else{
                            faceScore = 0
                        }
                    }
                }
            }
            // after done, release the ImageProxy object
            imageProxy.close()
        })
        //将调用传入的cameraProvider，绑定到当前页面生命周期，并传入要使用的CameraSelector和Preview对象。
        var camera = cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, imageAnalysis, preview)

    }
    private fun setPreview(){
        //获取一个监听对象
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {//创建监听事件
            //从监听对象中get到cameraProvider
            val cameraProvider = cameraProviderFuture.get()
            //用cameraProvider设置界面
            bindPreviewAndSetAnalysis(cameraProvider)
        }, //接下来传入一个Executor
            ContextCompat.getMainExecutor(this))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_recognition)
        //检查权限
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            requestPermissions(arrayOf(Manifest.permission.CAMERA),1)
        }
        else{
            setPreview()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.i("cv", "Internal OpenCV library not found. Using OpenCV Manager for initialization")
        } else {
            Log.i("cv", "OpenCV library found inside package. Using it!")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    //Toast.makeText(this, "已经获取相机权限！", Toast.LENGTH_LONG).show()
                    setPreview()
                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    Toast.makeText(this, "相机权限被拒绝！", Toast.LENGTH_LONG).show()
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }
}