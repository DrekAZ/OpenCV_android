package jp.tanaka.ex.opencv_face;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.android.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static jp.tanaka.ex.opencv_face.Add_Image.REQUEST_ACTION_PICK;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {

    private CameraBridgeViewBase mCameraView;
    DrawerLayout drawerLayout;
    ListView listView;
    CascadeClassifier mFaceDetector;
    Size mMinFaceSize;
    Mat CameraView;
    String[] str = {"カメラエフェクト変更", "顔変更"};
    Intent intent;
    Mat bitMap;
    Bitmap Bit_Add;
    Bitmap bitmap;
    Mat alpha;
    Size MatSize;
    int Estate;
    int Fstate;
    int state = 0;

    // ライブラリ初期化完了後に呼ばれるコールバック (onManagerConnected) //// TODO: 2017/03/22
    // public abstract class BaseLoaderCallback implements LoaderCallbackInterface
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                // 読み込みが成功したらカメラプレビューを開始
                case LoaderCallbackInterface.SUCCESS:
                    state = 1;
                    mCameraView.enableView();
                    mCameraView.setOnTouchListener(MainActivity.this);
                    mFaceDetector = setupFaceDetector();
                    //// TODO: 2017/03/29

                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) { //// TODO: 2017/03/26
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cv);


        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "Failed");
        } else {
            Log.e("OpenCV", "successfully built !");
        }

        Resources r = getResources();

        Intent inte = getIntent();
        Estate = inte.getIntExtra("Effect", 0);
        Fstate = inte.getIntExtra("Face", 0);
        //Bit_Add = (Bitmap) inte.getParcelableExtra("Bitmap");
        if (Fstate == 1 && state == 1) {
            bitmap = BitmapFactory.decodeResource(r, R.drawable.hhhkk);
            bitMap = BITMAP2MASK(bitmap);
        } else if (Fstate == 2) {
            bitmap = BitmapFactory.decodeResource(r, R.drawable.black);
            bitMap = BITMAP2MASK(bitmap);
        }

        mCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        mCameraView.setCvCameraViewListener(this);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        listView = (ListView) findViewById(R.id.drawer);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, str);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                String item = (String) listView.getItemAtPosition(position);
                Toast.makeText(MainActivity.this, item, Toast.LENGTH_SHORT).show();

                switch (position) {
                    case 0:
                        intent = new Intent(MainActivity.this, Option_Effect.class);
                        startActivity(intent);
                        break;
                    case 1:
                        intent = new Intent(MainActivity.this, Option_Face.class);
                        startActivity(intent);
                        break;
                }
            }
        });


    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (CameraView != null) {
                    takePicture(CameraView);
                } else {
                    Toast.makeText(this, "Can not TakePicture", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return true;
    }


    @Override
    public void onPause() {
        if (mCameraView != null) {
            mCameraView.disableView();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCameraView != null) {
            mCameraView.disableView();
        }
    }


    // // TODO: 2017/03/23

    private CascadeClassifier setupFaceDetector() {
        File cascadeFile = setupCascadeFile();
        if (cascadeFile == null) {
            return null;
        }

        CascadeClassifier detector = new CascadeClassifier(cascadeFile.getAbsolutePath());
        if (detector.empty()) {
            return null;
        }
        return detector;
    }


    // TODO TODO TODO TODO: 2017/03/22
    @Override
    public void onCameraViewStarted(int width, int height) {
        // Mat(int rows, int cols, int type)
        // rows(行): height, cols(列): width
        if (mMinFaceSize == null) {
            mMinFaceSize = new Size(height / 5, height / 5);
        }
        if (Fstate != 0) {
            onFaace(Fstate);
        }
        //mOutputFrame = new Mat(height, width, CvType.CV_8UC1); // TODO doodod
    }

    @Override
    public void onCameraViewStopped() {
        //mOutputFrame.release();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        // Cannyフィルタをかける
        // Imgproc.Canny(inputFrame.gray(), mOutputFrame, 80, 100); // whiteandBlack // TODO
        // ビット反転
        // Core.bitwise_not(mOutputFrame, mOutputFrame);
        // /return inputFrame.gray(); //inputFrame をそのまま return

        Mat rgba = inputFrame.rgba();
        if (mFaceDetector != null) {
            if (Fstate != 0) {

                MatOfRect faces = new MatOfRect();

                mFaceDetector.detectMultiScale(inputFrame.gray(), faces, 1.1, 2, 2, mMinFaceSize, new Size());
                Rect[] facesArray = faces.toArray();
                for (int i = 0; i < facesArray.length; i++) {
                    //Imgproc.rectangle(rgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3); //face TODO GOUSEI //tl()左上 br() 右下
                    Imgproc.resize(bitMap, bitMap, new Size(facesArray[i].width, facesArray[i].height));
                    rgba = BITMAP2MAT(bitMap, rgba, facesArray[i].tl(), facesArray[i].br()); //// TODO: 2017/03/29
                }
            }
        }
        if (Estate == 1) {
            Imgproc.cvtColor(rgba, rgba, Imgproc.COLOR_BGR2GRAY);
            //Imgproc.GaussianBlur(元画像(Mat), 出力画像(Mat), new Size(9, 9)(とか、ぼかす範囲), 8, 6);
        } else if (Estate == 2) {
            Imgproc.Canny(inputFrame.gray(), rgba, 25, 100);
        }
        CameraView = rgba;
        return rgba;
    }


    public File setupCascadeFile() {
        File cascadeFile = null;
        InputStream is = null;
        FileOutputStream os = null;
        File cascadeDir;

        cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
        try {

            cascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            if (!cascadeFile.exists()) {
                is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                os = new FileOutputStream(cascadeFile);
                byte[] buffer = new byte[4096];
                int readLen = 0;
                while ((readLen = is.read(buffer)) != -1) {
                    os.write(buffer, 0, readLen);
                }
            }
        } catch (IOException e) {
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    // do nothing
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (Exception e) {
                    // do nothing
                }
            }
        }
        return cascadeFile;
    }


    private String onFormatDate() { //// TODO: 2017/03/27
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd__HHmm", Locale.US);
        String imgPath = dateFormat.format(date) + ".jpg";
        return imgPath;
    }

    private void takePicture(Mat mat) { // // TODO: 2017/03/27
        Bitmap bmp = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        File FilePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/OpenCV_Face/"); //getpath !?
        String Path = onFormatDate();

        //Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2RGBA, 4);
        Utils.matToBitmap(mat, bmp);

        Save(bmp, FilePath, Path);
        Android_Date(Path);

        Toast.makeText(this, "TakePicture", Toast.LENGTH_SHORT).show();

        //mback.getHandler().post(new ImageStore(reader.acquireNextImage(), file));  /// TODO

    }

    //// TODO: 2017/03/27


    public void Save(Bitmap bmp, File mFile, String FileName) {

        File file = new File(mFile, FileName);

        try {
            if (!mFile.exists()) {
                mFile.mkdir();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            throw e;
        }

        Toast.makeText(this, file.toString(), Toast.LENGTH_SHORT).show();

        try {
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void Android_Date(String fileName) {
        ContentValues values = new ContentValues();
        ContentResolver contentResolver = getContentResolver();
        values.put(MediaStore.Images.Media.TITLE, fileName);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATA, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + "OpenCV_Face/" + fileName);
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Toast.makeText(this, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath(), Toast.LENGTH_LONG).show();
    }


    private Mat BITMAP2MASK(Bitmap bitmap) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        alpha = new Mat(mat.rows(), mat.cols(), mat.type(), new Scalar(0));
        return mat;
    }

    private Mat BITMAP2MAT(Mat mat1, Mat mat2, Point p1, Point p2) { //1:face, 2:back

        //mat2 = removeAlpha(mat2);
        Imgproc.resize(mat1, mat1, mat2.size());
        Imgproc.resize(alpha, alpha, mat2.size());

        Point[] src_p = new Point[]{
                new Point(0, 0),
                new Point(mat1.cols(), 0),
                new Point(mat1.cols(), mat1.rows() + 100),
        };
        Point[] dst_p = new Point[]{
                new Point(p1.x, p1.y),
                new Point(p2.x, p1.y),
                new Point(p2.x, p2.y),
        };

        //Log.e("PPPPPPPPPPPPPP",p1.toString());

        //Mat alpha0 = new Mat(mat2.rows(), mat2.cols(), mat1.type()); //
        Mat mat = Imgproc.getAffineTransform(new MatOfPoint2f(src_p), new MatOfPoint2f(dst_p));
        Imgproc.warpAffine(mat1, alpha, mat, mat2.size(), Imgproc.INTER_LINEAR, Core.BORDER_TRANSPARENT, new Scalar(0, 0, 0));
        //mat2 = overlayImage(mat2, alpha0); //

        Mat mm = new Mat(alpha.rows(), alpha.cols(), alpha.type());
        List<Mat> planes_rgba = new ArrayList<>();
        Core.split(alpha, planes_rgba);


        Core.merge(Arrays.asList(planes_rgba.get(0), planes_rgba.get(1), planes_rgba.get(2)), mm);
        Core.merge(Arrays.asList(planes_rgba.get(3), planes_rgba.get(3), planes_rgba.get(3)), alpha);

        mat2 = removeAlpha(mat2);

        //Core.bitwise_and(mat1, alpha, mat1); //マスク部分のimg1を取り出してimg1に入れる
        //Core.bitwise_not(alpha, alpha);//マスクを反転させる
        //Core.bitwise_and(mat2, alpha, mat2); //反転マスク部分のimg2を取り出してimg2にいれる
        //Core.bitwise_and(mat1, mat2, mat2);//img1とimg2を合成してimg3に入れる

        //Core.bitwise_or(mat1.mul(alpha),mat2.mul(alpha), mat2);
        Core.bitwise_and(mm, alpha, mm);
        Core.bitwise_not(alpha, alpha);
        //Imgproc.resize(alpha, alpha, mat2.size());
        Core.bitwise_and(mat2, alpha, mat2);
        Core.bitwise_or(mm, mat2, mat2);


        return mat2;
    }

    private Mat removeAlpha(Mat img_rgba) {
        List<Mat> planes_rgba = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            planes_rgba.add(new Mat());
        }
        Core.split(img_rgba, planes_rgba);

        List<Mat> planes_rgb = new ArrayList<>();
        planes_rgb.add(planes_rgba.get(0));
        planes_rgb.add(planes_rgba.get(1));
        planes_rgb.add(planes_rgba.get(2));
        Mat img_rgb = new Mat();
        Core.merge(planes_rgb, img_rgb);
        return img_rgb;
    }


    private void File2Bit() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        //これだとギャラリー専門が開きます。
        //Intent intent = new Intent(Intent.ACTION_PICK);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        //createChooserを使うと選択ダイアログのタイトルを変更する事ができます。
        startActivityForResult(Intent.createChooser(intent, "select"), REQUEST_ACTION_PICK);
        //デフォルトで「アプリ選択」と出ます。
        //startActivityForResult(intent, REQUEST_ACTION_PICK);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_ACTION_PICK) {
                try {
                    InputStream iStream = getContentResolver().openInputStream(data.getData());
                    bitmap = BitmapFactory.decodeStream(iStream);
                    iStream.close();
                    //Bitmapで普通に利用ができます。

                    Toast.makeText(this, "OK", Toast.LENGTH_SHORT).show();

                    //startActivity(intent);
                } catch (IOException e) {
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void onFaace(int a) {
        Resources r = getResources();
        switch (a) {
            case 1:
                bitmap = BitmapFactory.decodeResource(r, R.drawable.hhhkk);
                bitMap = BITMAP2MASK(bitmap);
                break;
            case 2:
                bitmap = BitmapFactory.decodeResource(r, R.drawable.black);
                bitMap = BITMAP2MASK(bitmap);
                break;
        }
    }

}


//private Mat Mask(Mat mat1, Mat mat2, Point p1, Point p2, Size Msize){ // 1:front 2:back}



