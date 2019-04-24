package com.tamaq.courier.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Pair;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BaseTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.tamaq.courier.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;

public class PhotoHelper {

    public static final int CAMERA_KEY = 0;
    public static final int GALLERY_KEY = 1;

    private static String mCurrentPhotoPath;

    public static Bitmap getCurrentBitmapByLocalPath(String filePath) {
        return BitmapFactory.decodeFile(new File(filePath).getAbsolutePath());
    }

    public static String getGalleryPicturePath(Context context, Intent data) {
        String galleryPicturePath = "";

        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(data.getData(), filePathColumn, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            galleryPicturePath = cursor.getString(columnIndex);
            cursor.close();
        }

        if (galleryPicturePath != null) {
            return galleryPicturePath;
        } else {
            return FileHelper.getPath(context, data.getData());
        }
    }

    public static Intent prepareTakePhotoIntent(Context context) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            File photoFile = createImageFile(context);

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(context,
                        context.getString(R.string.system_path_fileprovider),
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    context.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                return takePictureIntent;
            }
        }
        return takePictureIntent;
    }

    private static File createImageFile(Context context) {
        File image = null;
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "TAMAQ_" + timeStamp + "_";
            File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            image = File.createTempFile(imageFileName, ".jpg", storageDir);

            mCurrentPhotoPath = image.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public static Single<Pair<String, Bitmap>> getSaveBitmapThumbToFileSingle(File file, Context context) {
        return Single.create(new SingleOnSubscribe<Pair<String, Bitmap>>() {
            @Override
            public void subscribe(SingleEmitter<Pair<String, Bitmap>> singleEmitter) throws Exception {
                Glide.with(context)
                        .load(file.getAbsolutePath())
                        .asBitmap()
                        .override(400, 400)
                        .into(new BaseTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                try {
                                    int lastSlashIndex = file.getAbsolutePath().lastIndexOf("/") + 1;
                                    String newName = file.getAbsolutePath().substring(lastSlashIndex).replace(".jpg", "");

                                    File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                                    storageDir.mkdirs();
                                    File compressedFile = File.createTempFile(newName, "_compressed.jpg", storageDir);

                                    FileOutputStream fos = new FileOutputStream(compressedFile);
                                    resource.compress(Bitmap.CompressFormat.JPEG, 75, fos);
                                    fos.flush();
                                    fos.close();

                                    String resultPath = compressedFile.getAbsolutePath();
                                    Bitmap compressedBitmap = BitmapFactory.decodeFile(resultPath, new BitmapFactory.Options());
                                    singleEmitter.onSuccess(new Pair<>(resultPath, compressedBitmap));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    singleEmitter.onSuccess(new Pair<>(file.getAbsolutePath(), resource));
                                }
                            }

                            @Override
                            public void getSize(SizeReadyCallback cb) {
                            }

                            @Override
                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                super.onLoadFailed(e, errorDrawable);
                                singleEmitter.onError(e);
                            }
                        });
            }
        });
    }

    public static String getCurrentPhotoPath() {
        return mCurrentPhotoPath;
    }

    public static void clearCurrentPhotoPath() {
        mCurrentPhotoPath = null;
    }
}
