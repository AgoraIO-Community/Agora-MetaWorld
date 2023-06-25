package io.agora.meta.example_voice_driver.utils;


import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;

import java.io.File;

import io.agora.meta.example_voice_driver.models.manifest.AaManifest;
import io.agora.meta.example_voice_driver.models.manifest.DressItemResource;
import io.agora.meta.example_voice_driver.models.manifest.DressResource;
import io.agora.meta.example_voice_driver.models.manifest.FaceBlendShape;
import io.agora.meta.example_voice_driver.models.manifest.FaceParameter;

public class DressAndFaceDataUtils {
    private final static String TAG = DressAndFaceDataUtils.class.getSimpleName();

    private static final String FILE_NAME_ASSET_MANIFEST = "AaManifest.txt";
    private static final String FILE_NAME_ICON_FOLDER = "Icons";
    private static final String FILE_NAME_ICONS_ZIP = FILE_NAME_ICON_FOLDER + ".zip";
    private static final String FILE_NAME_CATALOG_HASH = "catalog_1.0.0.hash";

    private volatile static DressAndFaceDataUtils mDressAndFaceDataUtils;

    private AaManifest mManifest;

    private String mIconsFilePath;

    private DressAndFaceDataUtils() {

    }

    public static DressAndFaceDataUtils getInstance() {
        if (null == mDressAndFaceDataUtils) {
            synchronized (DressAndFaceDataUtils.class) {
                if (null == mDressAndFaceDataUtils) {
                    mDressAndFaceDataUtils = new DressAndFaceDataUtils();
                }
            }
        }
        return mDressAndFaceDataUtils;
    }

    public void initData(String filePath) {
        mIconsFilePath = filePath + File.separator + FILE_NAME_ICON_FOLDER;
        if (mManifest == null) {
            String jsonStr = Utils.readFile(filePath + File.separator + FILE_NAME_ASSET_MANIFEST);
            if (!TextUtils.isEmpty(jsonStr)) {
                try {
                    mManifest = JSON.parseObject(jsonStr, AaManifest.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        Log.i(TAG, "initData manifest:" + mManifest);
        if (null != mManifest) {
            String catalogHash = Utils.readFile(filePath + File.separator + FILE_NAME_CATALOG_HASH);
            if (!catalogHash.equals(MMKVUtils.getInstance().getValue(MetaConstants.MMKV_CATALOG_HAS, ""))) {
                Log.i(TAG, "initData unzipIcons");
                unzipIcons(filePath);
                MMKVUtils.getInstance().putValue(MetaConstants.MMKV_CATALOG_HAS, catalogHash);
            }
        }
    }

    private void unzipIcons(String filePath) {
        File targetFolderPath = new File(filePath + File.separator + FILE_NAME_ICON_FOLDER);
        if (targetFolderPath.exists()) {
            Utils.deleteFile(targetFolderPath);
        }

        Utils.unzip(filePath + File.separator + FILE_NAME_ICONS_ZIP, filePath);
    }

    public DressItemResource[] getDressResources(String avatarType) {
        if (null != mManifest) {
            for (DressResource dressResource : mManifest.getDressResources()) {
                if (avatarType.equals(dressResource.getAvatar())) {
                    return dressResource.getResources();
                }
            }
        }
        return null;
    }

    public String getIconFilePath(String avatarType) {
        if (TextUtils.isEmpty(mIconsFilePath)) {
            return null;
        } else {
            return mIconsFilePath + File.separator + avatarType + File.separator;
        }
    }

    public FaceBlendShape[] getFaceBlendShapes(String avatarType) {
        if (null != mManifest) {
            for (FaceParameter faceParameter : mManifest.getFaceParameters()) {
                if (avatarType.equals(faceParameter.getAvatar())) {
                    return faceParameter.getBlendShape();
                }
            }
        }
        return null;
    }
}
