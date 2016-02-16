package com.oakkub.chat.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import com.oakkub.chat.managers.Contextor;

/**
 * Created by OaKKuB on 1/23/2016.
 */
public class UriUtil {

    private static final String TAG = UriUtil.class.getSimpleName();

    public static String getPath(Uri uri) {
        Context context = Contextor.getInstance().getContext();

        if (Build.VERSION.SDK_INT >= 19) {
            String documentPath = getPathForV19AndUp(uri);
            if (!documentPath.isEmpty()) return documentPath;
        }

        String[] projection = new String[] {
                MediaStore.Files.FileColumns.DATA
        };

        String path;
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) {
            path = uri.getPath();
            return path;
        }

        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
        path = cursor.getString(columnIndex);
        cursor.close();

        return (path == null || path.isEmpty()) ? uri.getPath() : path;
    }

    @TargetApi(19)
    public static String getPathForV19AndUp(Uri contentUri) {
        Context context = Contextor.getInstance().getContext();

        if (!DocumentsContract.isDocumentUri(context, contentUri)) return "";

//        Log.d(TAG, "getPathForV19AndUp: path:" + contentUri.getPath());
        String wholeID = DocumentsContract.getDocumentId(contentUri);
//        Log.d(TAG, "getPathForV19AndUp: whole document id:" + wholeID);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];
//        Log.d(TAG, "getPathForV19AndUp: document id:" + id);
        String[] column = { MediaStore.Images.Media.DATA };
//        Log.d(TAG, "getPathForV19AndUp: column:" + column[0]);

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";
//        Log.d(TAG, "getPathForV19AndUp: selection:" + sel);

        // example: SELECT _id FROM {URI} WHERE _id = {id}
        Cursor cursor = context.getContentResolver().
                query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        column, sel, new String[]{ id }, null);

        if (cursor == null) {
            return "";
        }

        String filePath = "";
        int columnIndex = cursor.getColumnIndexOrThrow(column[0]);
        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }

        cursor.close();
        return filePath;
    }

}
