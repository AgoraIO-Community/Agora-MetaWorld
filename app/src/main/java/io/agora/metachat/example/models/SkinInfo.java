package io.agora.metachat.example.models;

import androidx.annotation.NonNull;

/**
 * 衣服、裤子、头发、鞋子实体类
 */
public class SkinInfo {

    //名称
    public String name;
    //衣服、裤子等id  0-1000
    public int id;
    //图片id
    public int iconId;
    //性别 0 男  1女
    public int gender;
    //价格
    public int price;
    //是否选择了
    public boolean isCheck;
    //类型
    public GarmentType type;

    public SkinInfo() {

    }

    /**
     * @param name    名字
     * @param id      衣服、裤子等id  0-1000
     * @param iconId  图片id
     * @param gender  性别 0 男  1女
     * @param price   价格
     * @param isCheck 是否选择了
     * @param type    服装类型
     */
    public SkinInfo(String name, int id, int iconId, int gender, int price, boolean isCheck, GarmentType type) {
        this.name = name;
        this.id = id;
        this.iconId = iconId;
        this.gender = gender;
        this.price = price;
        this.isCheck = isCheck;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public GarmentType getType() {
        return type;
    }

    public void setType(GarmentType type) {
        this.type = type;
    }

    @NonNull
    @Override
    public String toString() {
        return "SkinInfo{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", iconId=" + iconId +
                ", gender=" + gender +
                ", price=" + price +
                ", isCheck=" + isCheck +
                ", type=" + type +
                '}';
    }
}
