package io.agora.metachat.example.data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;

import io.agora.metachat.example.R;
import io.agora.metachat.example.models.RoleInfo;
import io.agora.metachat.example.models.SkinInfo;
import io.agora.metachat.example.utils.MMKVUtils;
import io.agora.metachat.example.utils.MetaChatConstants;

/**
 * 换装数据
 */
public class SkinsData {


    //顶部tab选中的状态图片
    public static int[] mIconSelectIds = {
            R.mipmap.clothes_icon1, R.mipmap.shoose_icon1,
            R.mipmap.trousers_icon1, R.mipmap.hairpin_icon1};

    //顶部tab未选中的图片
    public static int[] mIconUnselectIds = {
            R.mipmap.clothes_icon, R.mipmap.shoose_icon,
            R.mipmap.trousers_icon, R.mipmap.hairpin_icon};

    //顶部tab选中的状态图片
    public static int[] mIconSelectIdsNan = {
            R.mipmap.clothes_icon1, R.mipmap.shoose_icon1,
            R.mipmap.trousers_icon1, R.mipmap.hairpin_icon1};

    //顶部tab未选中的图片
    public static int[] mIconUnselectIdsNan = {
            R.mipmap.clothes_icon, R.mipmap.shoose_icon,
            R.mipmap.trousers_icon, R.mipmap.hairpin_icon};

    //女士衣服图片
    public final static int[] womenClothesIconArray = {
            R.mipmap.clothes1, R.mipmap.clothes2,
            R.mipmap.clothes3,};
    //男士衣服图片
    public final static int[] manClothesIconArray = {
            R.mipmap.man_clothes1, R.mipmap.man_clothes2,
            R.mipmap.man_clothes3,};
    //眼镜图片
    public final static int[] glassesIconArray = {
            R.mipmap.glasses1, R.mipmap.glasses2,
            R.mipmap.glasses3, R.mipmap.glasses4, R.mipmap.glasses5, R.mipmap.glasses6, R.mipmap.glasses1, R.mipmap.glasses2,
            R.mipmap.glasses3, R.mipmap.glasses4, R.mipmap.glasses5, R.mipmap.glasses6};
    //发卡图片
    public final static int[] womenHairpinIconArray = {
            R.mipmap.hairpin1, R.mipmap.hairpin2,};

    //男士发卡图片
    public final static int[] manHairpinIconArray = {
            R.mipmap.hairpin3, R.mipmap.hairpin4,};
    //女士鞋子图片
    public final static int[] womenShoesIconArray = {
            R.mipmap.shooes3, R.mipmap.shooes4,};
    //男士鞋子
    public final static int[] manShoesIconArray = {
            R.mipmap.shooes2, R.mipmap.shooes5,};
    //女士裤子图片
    public final static int[] womenTrousersIconArray = {
            R.mipmap.trousers1, R.mipmap.trousers2,
    };
    //男士裤子图片
    public final static int[] manTrousersIconArray = {
            R.mipmap.trousers3, R.mipmap.trousers4,};


    //女士衣服
    public final static ArrayList<SkinInfo> womenClothingList = new ArrayList<>();
    //男士衣服
    public final static ArrayList<SkinInfo> manClothingList = new ArrayList<>();

    //男士鞋子
    public final static ArrayList<SkinInfo> manShoesList = new ArrayList<>();
    //女士鞋子
    public final static ArrayList<SkinInfo> womenShoesList = new ArrayList<>();
    //男士裤子
    public final static ArrayList<SkinInfo> manTrousersList = new ArrayList<>();
    //女士裤子
    public final static ArrayList<SkinInfo> womenTrousersList = new ArrayList<>();
    //女士发卡
    public final static ArrayList<SkinInfo> womenHairpinList = new ArrayList<>();
    //男士发卡
    public final static ArrayList<SkinInfo> manHairpinList = new ArrayList<>();

    //换装数据
    public final static ArrayList<ArrayList<SkinInfo>> changeClothesList = new ArrayList<>();

/*    static boolean firstOpenBool;

    public static void clearSKins() {
        womenClothingList.clear();
        manClothingList.clear();
        manShoesList.clear();
        womenShoesList.clear();
        manTrousersList.clear();
        womenTrousersList.clear();
        womenHairpinList.clear();
        manHairpinList.clear();
        changeClothesList.clear();
    }

    public static void initSkins(Context context, int gender) {
        //第一次初始化换装数据，之后从getPreferences(context,Utils.changeData);读取数据
        firstOpenBool = MMKVUtils.getInstance().getValue(MetaChatConstants.MMKV_FIRST_OPEN, true);

        if (gender == 1) {
            initWomenClothes(gender);
            initWomenShoes(gender);
            initWomenTrousers(gender);
            initWomenHairpin(gender);

            initManClothes(gender);
            initManShoes(gender);
            initManTrousers(gender);
            initManHairpin(gender);
        } else {
            initManClothes(gender);
            initManShoes(gender);
            initManTrousers(gender);
            initManHairpin(gender);

            initWomenClothes(gender);
            initWomenShoes(gender);
            initWomenTrousers(gender);
            initWomenHairpin(gender);
        }
    }*/

    /**
     * 初始化换装数据
     */
    /*public static void initSkinsData(Context context, String name, String gender) {
        //第一次初始化换装数据，之后从getPreferences(context,Utils.changeData);读取数据
        firstOpenBool = MMKVUtils.getInstance().getValue(MetaChatConstants.MMKV_FIRST_OPEN, true);


        if (firstOpenBool) {
            ArrayList<SkinInfo> skinInfoList = new ArrayList<>();
            skinInfoList.add(womenClothingList.get(0));
            skinInfoList.add(womenShoesList.get(0));
            skinInfoList.add(womenTrousersList.get(0));
            skinInfoList.add(womenHairpinList.get(0));
            skinInfoList.add(manClothingList.get(0));
            skinInfoList.add(manShoesList.get(0));
            skinInfoList.add(manTrousersList.get(0));
            skinInfoList.add(manHairpinList.get(0));

            RoleInfo roleInfo = new RoleInfo(skinInfoList, name, gender);
            MMKVUtils.getInstance().putValue(MetaChatConstants.MMKV_ROLE_INFO, JSONObject.toJSON(roleInfo));

            MMKVUtils.getInstance().putValue(MetaChatConstants.MMKV_FIRST_OPEN, false);
            //MetaChatContext.getInstance().sendSceneMessage(jsonObject);

        } else {
            RoleInfo roleInfo = JSONObject.parseObject(MMKVUtils.getInstance().getValue(MetaChatConstants.MMKV_ROLE_INFO, ""), RoleInfo.class);
            roleInfo.setGender(gender);

            for (int i = 0; i < roleInfo.getChangeClothingList().size(); i++) {
                switch (roleInfo.getChangeClothingList().get(i).getType()) {
                    case WOMEN_CLOTHING:
                        for (int j = 0; j < womenClothingList.size(); j++) {
                            if (womenClothingList.get(j).getId() == roleInfo.changeClothingList.get(i).getId()) {
                                womenClothingList.set(j, roleInfo.changeClothingList.get(i));
                            }
                        }
                        break;
                    case MAN_CLOTHING:
                        for (int j = 0; j < manClothingList.size(); j++) {
                            if (manClothingList.get(j).getId() == changeClothes.changeClothingList.get(i).getId()) {
                                manClothingList.set(j, changeClothes.changeClothingList.get(i));
                            }
                        }
                        break;
                    case WOMEN_SHOES:
                        for (int j = 0; j < womenShoesList.size(); j++) {
                            if (womenShoesList.get(j).getId() == changeClothes.changeClothingList.get(i).getId()) {
                                womenShoesList.set(j, changeClothes.changeClothingList.get(i));
                            }
                        }
                        break;
                    case MAN_SHOES:
                        for (int j = 0; j < manShoesList.size(); j++) {
                            if (manShoesList.get(j).getId() == changeClothes.changeClothingList.get(i).getId()) {
                                manShoesList.set(j, changeClothes.changeClothingList.get(i));
                            }
                        }
                        break;
                    case WOMEN_TROUSERS:
                        for (int j = 0; j < womenTrousersList.size(); j++) {
                            if (womenTrousersList.get(j).getId() == changeClothes.changeClothingList.get(i).getId()) {
                                womenTrousersList.set(j, changeClothes.changeClothingList.get(i));
                            }
                        }
                        break;
                    case MAN_TROUSERS:
                        for (int j = 0; j < manTrousersList.size(); j++) {
                            if (manTrousersList.get(j).getId() == changeClothes.changeClothingList.get(i).getId()) {
                                manTrousersList.set(j, changeClothes.changeClothingList.get(i));
                            }
                        }
                        break;

                    case WOMEN_HAIRPIN:
                        for (int j = 0; j < womenHairpinList.size(); j++) {
                            if (womenHairpinList.get(j).getId() == changeClothes.changeClothingList.get(i).getId()) {
                                womenHairpinList.set(j, changeClothes.changeClothingList.get(i));
                            }
                        }
                        break;
                    case MAN_HAIRPIN:
                        for (int j = 0; j < manHairpinList.size(); j++) {
                            if (manHairpinList.get(j).getId() == changeClothes.changeClothingList.get(i).getId()) {
                                manHairpinList.set(j, changeClothes.changeClothingList.get(i));
                            }
                        }
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + changeClothes.getChangeClothingList().get(i).getType());
                }
            }

            String jsonObject = gson.toJson(changeClothes);
            MetaChatContext.getInstance().sendSceneMessage(jsonObject);
        }
    }

    *//**
     * 初始化女装数据
     *//*
    public static ArrayList<ChangeReloading> initWomenClothes(int gender) {
        for (int i = 0; i < womenClothesIconArray.length; i++) {
            ChangeReloading changeReloading;
            if (firstOpenBool) {
                if (i == 0) {
                    changeReloading = new ChangeReloading("womenClothes" + i, i, womenClothesIconArray[i], 1, 0, true, GarmentType.WOMEN_CLOTHING);
                } else {
                    changeReloading = new ChangeReloading("womenClothes" + i, i, womenClothesIconArray[i], 1, 0, false, GarmentType.WOMEN_CLOTHING);
                }
            } else {
                changeReloading = new ChangeReloading("womenClothes" + i, i, womenClothesIconArray[i], 1, 0, false, GarmentType.WOMEN_CLOTHING);
            }
            womenClothingList.add(changeReloading);
        }
        changeClothesList.add(womenClothingList);
        return womenClothingList;
    }

    *//**
     * 初始化男装数据
     *//*
    public static ArrayList<ChangeReloading> initManClothes(int gender) {
        for (int i = 0; i < manClothesIconArray.length; i++) {
            ChangeReloading changeReloading;
            if (firstOpenBool) {
                if (i == 0) {
                    changeReloading = new ChangeReloading("manClothes" + i, i, manClothesIconArray[i], 0, 0, true, GarmentType.MAN_CLOTHING);
                } else {
                    changeReloading = new ChangeReloading("manClothes" + i, i, manClothesIconArray[i], 0, 0, false, GarmentType.MAN_CLOTHING);
                }
            } else {
                changeReloading = new ChangeReloading("manClothes" + i, i, manClothesIconArray[i], 0, 0, false, GarmentType.MAN_CLOTHING);
            }

            manClothingList.add(changeReloading);
        }
        changeClothesList.add(manClothingList);
        return manClothingList;
    }

    *//**
     * 初始化女鞋数据
     *//*
    public static ArrayList<ChangeReloading> initWomenShoes(int gender) {
        for (int i = 0; i < womenShoesIconArray.length; i++) {
            ChangeReloading changeReloading;
            if (firstOpenBool) {
                if (i == 0) {
                    changeReloading = new ChangeReloading("womenShoes" + i, i, womenShoesIconArray[i], 1, 0, true, GarmentType.WOMEN_SHOES);
                } else {
                    changeReloading = new ChangeReloading("womenShoes" + i, i, womenShoesIconArray[i], 1, 0, false, GarmentType.WOMEN_SHOES);
                }
            } else {
                changeReloading = new ChangeReloading("womenShoes" + i, i, womenShoesIconArray[i], 1, 0, false, GarmentType.WOMEN_SHOES);
            }

            womenShoesList.add(changeReloading);
        }
        changeClothesList.add(womenShoesList);
        return womenShoesList;
    }

    *//**
     * 初始化男鞋数据
     *//*
    public static ArrayList<ChangeReloading> initManShoes(int gender) {
        for (int i = 0; i < manShoesIconArray.length; i++) {
            ChangeReloading changeReloading;
            if (firstOpenBool) {
                if (i == 0) {
                    changeReloading = new ChangeReloading("manShoes" + i, i, manShoesIconArray[i], 0, 0, true, GarmentType.MAN_SHOES);
                } else {
                    changeReloading = new ChangeReloading("manShoes" + i, i, manShoesIconArray[i], 0, 0, false, GarmentType.MAN_SHOES);
                }
            } else {
                changeReloading = new ChangeReloading("manShoes" + i, i, manShoesIconArray[i], 0, 0, false, GarmentType.MAN_SHOES);
            }

            manShoesList.add(changeReloading);
        }
        changeClothesList.add(manShoesList);
        return manShoesList;
    }

    *//**
     * 初始化女裤数据
     *//*
    public static ArrayList<ChangeReloading> initWomenTrousers(int gender) {
        for (int i = 0; i < womenTrousersIconArray.length; i++) {
            ChangeReloading changeReloading;
            if (firstOpenBool) {
                if (i == 0) {
                    changeReloading = new ChangeReloading("womenTrousers" + i, i, womenTrousersIconArray[i], 1, 0, true, GarmentType.WOMEN_TROUSERS);
                } else {
                    changeReloading = new ChangeReloading("womenTrousers" + i, i, womenTrousersIconArray[i], 1, 0, false, GarmentType.WOMEN_TROUSERS);
                }
            } else {
                changeReloading = new ChangeReloading("womenTrousers" + i, i, womenTrousersIconArray[i], 1, 0, false, GarmentType.WOMEN_TROUSERS);
            }

            womenTrousersList.add(changeReloading);
        }
        changeClothesList.add(womenTrousersList);
        return womenTrousersList;
    }

    *//**
     * 初始化女士头发
     *
     * @return
     *//*
    public static ArrayList<ChangeReloading> initWomenHairpin(int gender) {
        for (int i = 0; i < womenHairpinIconArray.length; i++) {
            ChangeReloading changeReloading;
            if (firstOpenBool) {
                if (i == 0) {
                    changeReloading = new ChangeReloading("womenHairpin" + i, i, womenHairpinIconArray[i], 1, 0, true, GarmentType.WOMEN_HAIRPIN);
                } else {
                    changeReloading = new ChangeReloading("womenHairpin" + i, i, womenHairpinIconArray[i], 1, 0, false, GarmentType.WOMEN_HAIRPIN);
                }
            } else {
                changeReloading = new ChangeReloading("womenHairpin" + i, i, womenHairpinIconArray[i], 1, 0, false, GarmentType.WOMEN_HAIRPIN);
            }

            womenHairpinList.add(changeReloading);
        }
        changeClothesList.add(womenHairpinList);
        return womenHairpinList;
    }

    *//**
     * 初始化男裤数据
     *//*
    public static ArrayList<ChangeReloading> initManTrousers(int gender) {
        for (int i = 0; i < manTrousersIconArray.length; i++) {
            ChangeReloading changeReloading;
            if (firstOpenBool) {
                if (i == 0) {
                    changeReloading = new ChangeReloading("manTrousers" + i, i, manTrousersIconArray[i], 0, 0, true, GarmentType.MAN_TROUSERS);
                } else {
                    changeReloading = new ChangeReloading("manTrousers" + i, i, manTrousersIconArray[i], 0, 0, false, GarmentType.MAN_TROUSERS);
                }
            } else {
                changeReloading = new ChangeReloading("manTrousers" + i, i, manTrousersIconArray[i], 0, 0, false, GarmentType.MAN_TROUSERS);
            }
            manTrousersList.add(changeReloading);
        }
        changeClothesList.add(manTrousersList);
        return manTrousersList;
    }

    *//**
     * 初始化男裤数据
     *//*
    public static ArrayList<ChangeReloading> initManHairpin(int gender) {
        for (int i = 0; i < manHairpinIconArray.length; i++) {
            ChangeReloading changeReloading;
            if (firstOpenBool) {
                if (i == 0) {
                    changeReloading = new ChangeReloading("manHairpin" + i, i, manHairpinIconArray[i], 0, 0, true, GarmentType.MAN_HAIRPIN);
                } else {
                    changeReloading = new ChangeReloading("manHairpin" + i, i, manHairpinIconArray[i], 0, 0, false, GarmentType.MAN_HAIRPIN);
                }
            } else {
                changeReloading = new ChangeReloading("manHairpin" + i, i, manHairpinIconArray[i], 0, 0, false, GarmentType.MAN_HAIRPIN);
            }

            manHairpinList.add(changeReloading);
        }
        changeClothesList.add(manHairpinList);
        return manHairpinList;
    }
*/
}
