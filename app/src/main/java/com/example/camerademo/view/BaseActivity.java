package com.example.camerademo.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.camerademo.BuildConfig;
import com.example.camerademo.R;
import com.example.camerademo.util.Constant;
import com.example.camerademo.util.PhotoProcessing;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.IOException;

import static com.example.camerademo.util.Constant.BaseFolder;
import static com.example.camerademo.util.Constant.IMAGE_DIRECTORY_NAME_TEMP;
import static com.example.camerademo.util.Constant.IMAGE_REQUEST_CODE_ASK_PERMISSIONS;
import static com.example.camerademo.util.Constant.camera;
import static com.example.camerademo.util.Constant.selectedImagePatch;

/*class for storage related task with camera or gallery
 * todo crop image
 *
 * */


abstract class BaseActivity extends AppCompatActivity {
    private Dialog dialog_camera;

    // function for open Camera dialog with custom layout
    public void cameraAlert() {
        dialog_camera = new Dialog(this);
        dialog_camera.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_camera.setContentView(R.layout.custom_camera_dialog);
        dialog_camera.setCancelable(false);
        FrameLayout cam_cancel = dialog_camera.findViewById(R.id.cam_cancel);
        FrameLayout camera = dialog_camera.findViewById(R.id.camera);
        FrameLayout gallery = dialog_camera.findViewById(R.id.gallery);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calling open camera And c passed as a string to to identify camera request
                camFun("c");
                dialog_camera.dismiss();
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calling open gallery And g passed as a string to to identify gallery request
                camFun("g");
                dialog_camera.dismiss();
            }
        });

        cam_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dialog_camera.dismiss();
            }
        });
        if (!dialog_camera.isShowing()) {
            dialog_camera.show();
        }
    }

    public void camFun(String s) {
        if (s.equals("c")) {
            // checking camera permission
            if (cameraPermission()) {
                // checking storage permission
                if (showStorage()) {
                    // call for open camera
                    openCamera();
                }
            }
        } else if (s.equals("g")) {
            if (cameraPermission()) {
                if (showStorage()) {
                    // call for open gallery
                    openGallery();
                }
            }
        }
    }


    // function for open camera
    public void openCamera() {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Uri fileUri;
        final int sdk = android.os.Build.VERSION.SDK_INT;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //File file = new File(context.getFilesDir(), ImageManager.BaseFolder+module);
        //File file = new File(Environment.getExternalStorageDirectory(), "_" + System.currentTimeMillis() + ".jpg");

        if (sdk > Build.VERSION_CODES.LOLLIPOP) {
            File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File image = null;
            try {
                image = File.createTempFile(String.valueOf(System.currentTimeMillis()), ".jpg", storageDirectory);
            } catch (IOException e) {
                e.printStackTrace();
            }
            fileUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", image);
        } else {
            File file = new File(Environment.getExternalStorageDirectory(), IMAGE_DIRECTORY_NAME_TEMP + "/" + System.currentTimeMillis() + ".jpg");
            fileUri = Uri.fromFile(file);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, Constant.CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
        Constant.uriiii = fileUri;
    }

    // function for check camera permissions
    public boolean cameraPermission() {
        boolean res = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int hasWriteContactsPermission = this.checkSelfPermission(android.Manifest.permission.CAMERA);
            if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, IMAGE_REQUEST_CODE_ASK_PERMISSIONS);
                //return;
            } else {
                res = true;
            }
        } else {
            res = true;

        }

        return res;

    }

    // function for check storage permissions
    public boolean showStorage() {
        boolean res = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int hasWriteExternalStoragePermission = this.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int hasReadExternalStoragePermission = this.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE);
            if (hasWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED ||
                    hasReadExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, Constant.IMAGE_REQUEST_CODE_ASK_PERMISSIONS_STORAGE);
            } else {
                res = true;
            }
        } else {
            res = true;
        }
        return res;
    }


    // function for open gallery intent
    public void openGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, Constant.GET_IMAGE_GALLERY);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                //beginCrop(Constant.uriiii);
                imageSelect(Constant.uriiii);
            }

        } else if (requestCode == Constant.GET_IMAGE_GALLERY) {
            if (resultCode == RESULT_OK) {
                final Uri imageUri = data.getData();
                imageSelect(data.getData());
                //beginCrop(imageUri);
            }
        }


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                final Uri cropImagePath = result.getUri();

                //String image_path = PhotoProcessing.compressImage(getActivity(), cropImagePath.getPath(), "temp/"+"ProfileImage");
                if (selectedImagePatch.contains(camera)) {
                    DeleteDir(this, selectedImagePatch);
                }
                String image_path = PhotoProcessing.compressImage(this, cropImagePath.getPath(), selectedImagePatch);
                // imageSelect(image_path);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    // todo need to work on crop image
    public void beginCrop(Uri source) {
        //Uri destination = Uri.fromFile(new File(getActivity().getCacheDir(), "cropped"));
        try {
            if (source != null && !source.equals(Uri.EMPTY) && !source.equals("null")) {
                CropImage.activity(source).start(this, getClass());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // method to get path of capture image or path for gallery Image
    @CallSuper
    public void imageSelect(Uri imagePath) {

    }

    private void DeleteDir(Context context, String folder) {
        File[] listFile;
        File file = new File(context.getFilesDir() + "/" + BaseFolder + folder);
        if (file.exists()) {
            listFile = file.listFiles();
            for (int i = 0; i < listFile.length; i++) {
                listFile[i].delete();
            }
        }

    }


    // method to get result after permission allowed or denied
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == IMAGE_REQUEST_CODE_ASK_PERMISSIONS) {
            openCamera();
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
