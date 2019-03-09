package tk.alltrue.filemanager;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MainActivity extends ListActivity {

    private List<String> mPathList = null;
    private String root;
    private TextView mPathTextView;

    private String mCurrentPath;
    private Comparator<? super File> mComparator;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        mPathTextView = (TextView) findViewById(R.id.textViewPath);
        mComparator = fileComparatorByAlphabetically;

        root = Environment.getExternalStorageDirectory().getPath();
        Log.d("roottt", root);
        getDir(root);
    }

    private void getDir(String dirPath) {
        mCurrentPath = dirPath;

        File sdcard_files_and_folders[] = new File(dirPath).listFiles();
        for (File fileOrFolder: sdcard_files_and_folders) {
            // do any thing that you want, add them to list or...
            Log.i("FILE", fileOrFolder.toString());
        }

        mPathTextView.setText("Path: " + dirPath);
        List<String>
                itemList = new ArrayList<>();
        mPathList = new ArrayList<>();
        File file = new File(dirPath);
        File[] filesArray = file.listFiles();

        if (!dirPath.equals(root)) {
            itemList.add(root);
            mPathList.add(root);
            itemList.add("../");
            mPathList.add(file.getParent());
        }

        Arrays.sort(filesArray, mComparator);
        //Log.d("roott", filesArray[0].toString());
        for (File aFilesArray : filesArray) {
            file = aFilesArray;

            if (!file.isHidden() && file.canRead()) {
                mPathList.add(file.getPath());
                if (file.isDirectory()) {
                    itemList.add(file.getName() + "/");
                } else {
                    itemList.add(file.getName());
                }
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.list_item, itemList);
        setListAdapter(adapter);
    }

    public ArrayList<String> getFiles(String DirectoryPath) {
        ArrayList<String> MyFiles = new ArrayList<String>();
        File f = new File(DirectoryPath);

        f.mkdirs();
        File[] files = f.listFiles();
        if (files.length == 0)
            return null;
        else {
            for (int i=0; i<files.length; i++)
                MyFiles.add(files[i].getName());
        }

        return MyFiles;
    }
    Comparator<? super File> fileComparatorByAlphabetically = new Comparator<File>() {

        public int compare(File file1, File file2) {

            if (file1.isDirectory()) {
                if (file2.isDirectory()) {
                    return String.valueOf(file1.getName().toLowerCase())
                            .compareTo(file2.getName().toLowerCase());
                } else {
                    return -1;
                }
            } else {
                if (file2.isDirectory()) {
                    return 1;
                } else {
                    return String.valueOf(file1.getName().toLowerCase())
                            .compareTo(file2.getName().toLowerCase());
                }
            }
        }
    };

    Comparator<? super File> fileComparatorByLastModified = new Comparator<File>() {
        public int compare(File file1, File file2) {

            if (file1.isDirectory()) {
                if (file2.isDirectory()) {
                    return Long.valueOf(file1.lastModified()).compareTo(
                            file2.lastModified());
                } else {
                    return -1;
                }
            } else {
                if (file2.isDirectory()) {
                    return 1;
                } else {
                    return Long.valueOf(file1.lastModified()).compareTo(
                            file2.lastModified());
                }
            }
        }
    };

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // обработка нажатий на элементах списка
        File file = new File(mPathList.get(position));
        // если это папка
        if (file.isDirectory()) {
            if (file.canRead()) // если она доступна для просмотра, то заходим в неё
                getDir(mPathList.get(position));
        } else { // если элемент списка является файлом, то выводим его имя
            String exifAttribute = null;
            String filename = file.getName();
            String ext = filename.substring(filename.lastIndexOf('.') + 1,
                    filename.length());

            if (ext.equalsIgnoreCase("JPG")) {
                try {
                    ExifInterface exif = new ExifInterface(file.toString());
                    exifAttribute = getExif(exif);
                } catch (IOException e) {
                }
            }

            String fileInfo = "Абсолютный путь: " + file.getAbsolutePath()
                    + "\n" + "Путь: " + file.getPath() + "\n" + "Родитель: "
                    + file.getParent() + "\n" + "Имя: " + file.getName() + "\n"
                    + "Последнее изменение: " + new Date(file.lastModified());

            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_launcher_background)
                    .setTitle("[" + file.getName() + "]")
                    .setMessage(fileInfo + " " + exifAttribute) // информация о файле и EXIF, если JPG
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                }
                            }).show();
        }
    }

    private String getExif(ExifInterface exif) {
        String attribute = null;
        attribute += getTagString(ExifInterface.TAG_DATETIME, exif);
        attribute += getTagString(ExifInterface.TAG_FLASH, exif);
        attribute += getTagString(ExifInterface.TAG_GPS_LATITUDE, exif);
        attribute += getTagString(ExifInterface.TAG_GPS_LATITUDE_REF, exif);
        attribute += getTagString(ExifInterface.TAG_GPS_LONGITUDE, exif);
        attribute += getTagString(ExifInterface.TAG_GPS_LONGITUDE_REF, exif);
        attribute += getTagString(ExifInterface.TAG_IMAGE_LENGTH, exif);
        attribute += getTagString(ExifInterface.TAG_IMAGE_WIDTH, exif);
        attribute += getTagString(ExifInterface.TAG_MAKE, exif);
        attribute += getTagString(ExifInterface.TAG_MODEL, exif);
        attribute += getTagString(ExifInterface.TAG_ORIENTATION, exif);
        attribute += getTagString(ExifInterface.TAG_WHITE_BALANCE, exif);
        return attribute;
    }

    private String getTagString(String tag, ExifInterface exif) {
        return (tag + " : " + exif.getAttribute(tag) + " ");
    }

    public void onAlphabetClick(View v) {
        mComparator = fileComparatorByAlphabetically;
        getDir(mCurrentPath);
    }

    public void onDateModClick(View v) {
        mComparator = fileComparatorByLastModified;
        getDir(mCurrentPath);
    }
}
