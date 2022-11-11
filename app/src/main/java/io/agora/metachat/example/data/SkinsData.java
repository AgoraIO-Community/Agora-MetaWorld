package io.agora.metachat.example.data;

import android.content.Context;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.metachat.example.R;
import io.agora.metachat.example.metachat.MetaChatContext;
import io.agora.metachat.example.models.GarmentType;
import io.agora.metachat.example.models.RoleInfo;
import io.agora.metachat.example.models.SkinInfo;
import io.agora.metachat.example.models.TabEntity;
import io.agora.metachat.example.utils.MMKVUtils;
import io.agora.metachat.example.utils.MetaChatConstants;

/**
 * 换装数据
 */
public class SkinsData {


    public static final String KEY_WOMEN_CLOTHING = "women_clothing";
    public static final String KEY_MAN_CLOTHING = "man_clothing";

    public static final String KEY_WOMEN_SHOES = "women_shoes";
    public static final String KEY_MAN_SHOES = "man_shoes";

    public static final String KEY_WOMEN_TROUSERS = "women_trousers";
    public static final String KEY_MAN_TROUSERS = "man_trousers";

    public static final String KEY_WOMEN_HAIRPIN = "women_hairpin";
    public static final String KEY_MAN_HAIRPIN = "man_hairpin";

    //顶部tab选中的状态图片
    public static Map<String, TabEntity> TAB_ENTITY_WOMEN = new HashMap<String, TabEntity>() {
        {
            put(KEY_WOMEN_CLOTHING, new TabEntity(R.mipmap.clothes_icon1, R.mipmap.clothes_icon));
            put(KEY_WOMEN_SHOES, new TabEntity(R.mipmap.shoose_icon1, R.mipmap.shoose_icon));
            put(KEY_WOMEN_TROUSERS, new TabEntity(R.mipmap.trousers_icon1, R.mipmap.trousers_icon));
            put(KEY_WOMEN_HAIRPIN, new TabEntity(R.mipmap.hairpin_icon1, R.mipmap.hairpin_icon));
        }

    };

    //顶部tab选中的状态图片
    public static Map<String, TabEntity> TAB_ENTITY_MAN = new HashMap<String, TabEntity>() {
        {
            put(KEY_MAN_CLOTHING, new TabEntity(R.mipmap.clothes_icon1, R.mipmap.clothes_icon));
            put(KEY_MAN_SHOES, new TabEntity(R.mipmap.shoose_icon1, R.mipmap.shoose_icon));
            put(KEY_MAN_TROUSERS, new TabEntity(R.mipmap.trousers_icon1, R.mipmap.trousers_icon));
            put(KEY_MAN_HAIRPIN, new TabEntity(R.mipmap.hairpin_icon1, R.mipmap.hairpin_icon));
        }

    };

    //女士衣服图片
    private final static int[] womenClothesIconArray = {
            R.mipmap.clothes1, R.mipmap.clothes2,
            R.mipmap.clothes3,};
    //男士衣服图片
    private final static int[] manClothesIconArray = {
            R.mipmap.man_clothes1, R.mipmap.man_clothes2,
            R.mipmap.man_clothes3,};
    //眼镜图片
    private final static int[] glassesIconArray = {
            R.mipmap.glasses1, R.mipmap.glasses2,
            R.mipmap.glasses3, R.mipmap.glasses4, R.mipmap.glasses5, R.mipmap.glasses6, R.mipmap.glasses1, R.mipmap.glasses2,
            R.mipmap.glasses3, R.mipmap.glasses4, R.mipmap.glasses5, R.mipmap.glasses6};
    //发卡图片
    private final static int[] womenHairpinIconArray = {
            R.mipmap.hairpin1, R.mipmap.hairpin2,};

    //男士发卡图片
    private final static int[] manHairpinIconArray = {
            R.mipmap.hairpin3, R.mipmap.hairpin4,};
    //女士鞋子图片
    private final static int[] womenShoesIconArray = {
            R.mipmap.shooes3, R.mipmap.shooes4,};
    //男士鞋子
    private final static int[] manShoesIconArray = {
            R.mipmap.shooes2, R.mipmap.shooes5,};
    //女士裤子图片
    private final static int[] womenTrousersIconArray = {
            R.mipmap.trousers1, R.mipmap.trousers2,
    };
    //男士裤子图片
    private final static int[] manTrousersIconArray = {
            R.mipmap.trousers3, R.mipmap.trousers4,};


    //女士衣服
    private final static List<SkinInfo> womenClothingList = new ArrayList<>();
    //男士衣服
    private final static List<SkinInfo> manClothingList = new ArrayList<>();

    //女士鞋子
    private final static List<SkinInfo> womenShoesList = new ArrayList<>();
    //男士鞋子
    private final static List<SkinInfo> manShoesList = new ArrayList<>();

    //女士裤子
    private final static List<SkinInfo> womenTrousersList = new ArrayList<>();
    //男士裤子
    private final static List<SkinInfo> manTrousersList = new ArrayList<>();

    //女士发卡
    private final static List<SkinInfo> womenHairpinList = new ArrayList<>();
    //男士发卡
    private final static List<SkinInfo> manHairpinList = new ArrayList<>();

    static {
        initWomenClothes();
        initManClothes();

        initWomenShoes();
        initManShoes();

        initWomenTrousers();
        initManTrousers();

        initWomenHairpin();
        initManHairpin();
    }

    /**
     * 初始化换装数据
     */
    public static void initSkinsData(String name, int gender) {
        RoleInfo roleInfo = MetaChatContext.getInstance().getRoleInfo();
        List<RoleInfo> roleInfos = MetaChatContext.getInstance().getRoleInfos();
        if (roleInfo == null) {
            List<SkinInfo> skinInfoList = new ArrayList<>();
            if (gender == MetaChatConstants.GENDER_WOMEN) {
                skinInfoList.add(womenClothingList.get(0));
                skinInfoList.add(womenShoesList.get(0));
                skinInfoList.add(womenTrousersList.get(0));
                skinInfoList.add(womenHairpinList.get(0));
            } else {
                skinInfoList.add(manClothingList.get(0));
                skinInfoList.add(manShoesList.get(0));
                skinInfoList.add(manTrousersList.get(0));
                skinInfoList.add(manHairpinList.get(0));
            }

            if (null == roleInfos) {
                roleInfos = new ArrayList<>(1);
            }

            roleInfo = new RoleInfo(skinInfoList, name, gender);
            roleInfos.add(roleInfo);

            MMKVUtils.getInstance().putValue(MetaChatConstants.MMKV_ROLE_INFO, JSONArray.toJSONString(roleInfos));
        }

        String jsonObject = (String) JSONObject.toJSONString(roleInfo);
        MetaChatContext.getInstance().sendSceneMessage(jsonObject);
    }

    /**
     * 初始化女装数据
     */
    public static void initWomenClothes() {
        SkinInfo skinInfo;
        for (int i = 0; i < womenClothesIconArray.length; i++) {
            skinInfo = new SkinInfo(KEY_WOMEN_CLOTHING + i, i, womenClothesIconArray[i], MetaChatConstants.GENDER_WOMEN, 0, false, GarmentType.WOMEN_CLOTHING);
            womenClothingList.add(skinInfo);
        }
    }

    /**
     * 初始化男装数据
     */
    public static void initManClothes() {
        SkinInfo skinInfo;
        for (int i = 0; i < manClothesIconArray.length; i++) {
            skinInfo = new SkinInfo(KEY_MAN_CLOTHING + i, i, manClothesIconArray[i], MetaChatConstants.GENDER_MAN, 0, false, GarmentType.MAN_CLOTHING);
            manClothingList.add(skinInfo);
        }
    }

    /**
     * 初始化女鞋数据
     */

    public static void initWomenShoes() {
        SkinInfo skinInfo;
        for (int i = 0; i < womenShoesIconArray.length; i++) {
            skinInfo = new SkinInfo(KEY_WOMEN_SHOES + i, i, womenShoesIconArray[i], MetaChatConstants.GENDER_WOMEN, 0, false, GarmentType.WOMEN_SHOES);
            womenShoesList.add(skinInfo);
        }
    }

    /**
     * 初始化男鞋数据
     */
    public static void initManShoes() {
        SkinInfo skinInfo;
        for (int i = 0; i < manShoesIconArray.length; i++) {
            skinInfo = new SkinInfo(KEY_MAN_SHOES + i, i, manShoesIconArray[i], MetaChatConstants.GENDER_MAN, 0, false, GarmentType.MAN_SHOES);
            manShoesList.add(skinInfo);
        }
    }

    /**
     * 初始化女裤数据
     */
    public static void initWomenTrousers() {
        SkinInfo skinInfo;
        for (int i = 0; i < womenTrousersIconArray.length; i++) {
            skinInfo = new SkinInfo(KEY_WOMEN_TROUSERS + i, i, womenTrousersIconArray[i], MetaChatConstants.GENDER_WOMEN, 0, false, GarmentType.WOMEN_TROUSERS);
            womenTrousersList.add(skinInfo);
        }
    }

    /**
     * 初始化男裤数据
     */
    public static void initManTrousers() {
        SkinInfo skinInfo;
        for (int i = 0; i < manTrousersIconArray.length; i++) {
            skinInfo = new SkinInfo(KEY_MAN_TROUSERS + i, i, manTrousersIconArray[i], MetaChatConstants.GENDER_MAN, 0, false, GarmentType.MAN_TROUSERS);
            manTrousersList.add(skinInfo);
        }
    }


    /**
     * 初始化女士头发
     */
    public static void initWomenHairpin() {
        SkinInfo skinInfo;
        for (int i = 0; i < womenHairpinIconArray.length; i++) {
            skinInfo = new SkinInfo(KEY_WOMEN_HAIRPIN + i, i, womenHairpinIconArray[i], MetaChatConstants.GENDER_WOMEN, 0, false, GarmentType.WOMEN_HAIRPIN);
            womenHairpinList.add(skinInfo);
        }
    }


    /**
     * 初始化男裤数据
     */
    public static void initManHairpin() {
        SkinInfo skinInfo;
        for (int i = 0; i < manHairpinIconArray.length; i++) {
            skinInfo = new SkinInfo(KEY_MAN_HAIRPIN + i, i, manHairpinIconArray[i], MetaChatConstants.GENDER_MAN, 0, false, GarmentType.MAN_HAIRPIN);
            manHairpinList.add(skinInfo);
        }
    }

    public static List<SkinInfo> getDressInfo(String type) {
        if (KEY_WOMEN_CLOTHING.equalsIgnoreCase(type)) {
            return womenClothingList;
        } else if (KEY_WOMEN_SHOES.equalsIgnoreCase(type)) {
            return womenShoesList;
        } else if (KEY_WOMEN_TROUSERS.equalsIgnoreCase(type)) {
            return womenTrousersList;
        } else if (KEY_WOMEN_HAIRPIN.equalsIgnoreCase(type)) {
            return womenHairpinList;
        } else if (KEY_MAN_CLOTHING.equalsIgnoreCase(type)) {
            return manClothingList;
        } else if (KEY_MAN_SHOES.equalsIgnoreCase(type)) {
            return manShoesList;
        } else if (KEY_MAN_TROUSERS.equalsIgnoreCase(type)) {
            return manTrousersList;
        } else if (KEY_MAN_HAIRPIN.equalsIgnoreCase(type)) {
            return manHairpinList;
        }
        return null;
    }
}
