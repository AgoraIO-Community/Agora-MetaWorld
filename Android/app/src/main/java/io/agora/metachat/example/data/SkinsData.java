package io.agora.metachat.example.data;

import java.util.ArrayList;
import java.util.List;

import io.agora.metachat.example.R;
import io.agora.metachat.example.models.GarmentType;
import io.agora.metachat.example.models.SkinInfo;
import io.agora.metachat.example.models.TabEntity;
import io.agora.metachat.example.utils.MetaChatConstants;

/**
 * 换装数据
 */
public class SkinsData {
    public static final String KEY_WOMEN_CLOTHING = "women_clothing";
    public static final String KEY_MAN_CLOTHING = "man_clothing";

    public static final String KEY_WOMEN_HAIRPIN = "women_hairpin";
    public static final String KEY_MAN_HAIRPIN = "man_hairpin";

    public static final String KEY_WOMEN_SHOES = "women_shoes";
    public static final String KEY_MAN_SHOES = "man_shoes";

    public static final String KEY_WOMEN_TROUSERS = "women_trousers";
    public static final String KEY_MAN_TROUSERS = "man_trousers";


    //顶部tab选中的状态图片
    public static List<TabEntity> TAB_ENTITY_WOMEN = new ArrayList<TabEntity>() {
        {
            add(new TabEntity(KEY_WOMEN_CLOTHING, R.drawable.tops_icon_checked, R.drawable.tops_icon));
            add(new TabEntity(KEY_WOMEN_HAIRPIN, R.drawable.hair_icon_checked, R.drawable.hair_icon));
            add(new TabEntity(KEY_WOMEN_SHOES, R.drawable.shoes_icon_checked, R.drawable.shoes_icon));
            add(new TabEntity(KEY_WOMEN_TROUSERS, R.drawable.lower_icon_checked, R.drawable.lower_icon));
        }

    };

    //顶部tab选中的状态图片
    public static List<TabEntity> TAB_ENTITY_MAN = new ArrayList<TabEntity>() {
        {
            add(new TabEntity(KEY_MAN_CLOTHING, R.drawable.tops_icon_checked, R.drawable.tops_icon));
            add(new TabEntity(KEY_MAN_HAIRPIN, R.drawable.hair_icon_checked, R.drawable.hair_icon));
            add(new TabEntity(KEY_MAN_SHOES, R.drawable.shoes_icon_checked, R.drawable.shoes_icon));
            add(new TabEntity(KEY_MAN_TROUSERS, R.drawable.lower_icon_checked, R.drawable.lower_icon));
        }

    };

    //女士衣服图片
    private final static int[] womenClothesIconArray = {
            R.drawable.girl_tops1, R.drawable.girl_tops2, R.drawable.girl_tops3, R.drawable.girl_tops4,};
    //男士衣服图片
    private final static int[] manClothesIconArray = {
            R.drawable.boy_tops1, R.drawable.boy_tops2,};

    //发卡图片
    private final static int[] womenHairpinIconArray = {
            R.drawable.girl_hair1, R.drawable.girl_hair2, R.drawable.girl_hair3, R.drawable.girl_hair4,};

    //男士发卡图片
    private final static int[] manHairpinIconArray = {
            R.drawable.boy_hair1, R.drawable.boy_hair2,};

    //女士鞋子图片
    private final static int[] womenShoesIconArray = {
            R.drawable.girl_shoes1, R.drawable.girl_shoes2, R.drawable.girl_shoes3, R.drawable.girl_shoes4,};
    //男士鞋子
    private final static int[] manShoesIconArray = {
            R.drawable.boy_shoes1, R.drawable.boy_shoes2,};

    //女士裤子图片
    private final static int[] womenTrousersIconArray = {
            R.drawable.girl_lower1, R.drawable.girl_lower2, R.drawable.girl_lower3, R.drawable.girl_lower4,};
    //男士裤子图片
    private final static int[] manTrousersIconArray = {
            R.drawable.boy_lower1, R.drawable.boy_lower2,};


    //女士衣服
    private final static List<SkinInfo> womenClothingList = new ArrayList<>();
    //男士衣服
    private final static List<SkinInfo> manClothingList = new ArrayList<>();

    //女士发卡
    private final static List<SkinInfo> womenHairpinList = new ArrayList<>();
    //男士发卡
    private final static List<SkinInfo> manHairpinList = new ArrayList<>();

    //女士鞋子
    private final static List<SkinInfo> womenShoesList = new ArrayList<>();
    //男士鞋子
    private final static List<SkinInfo> manShoesList = new ArrayList<>();

    //女士裤子
    private final static List<SkinInfo> womenTrousersList = new ArrayList<>();
    //男士裤子
    private final static List<SkinInfo> manTrousersList = new ArrayList<>();


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
