package io.agora.metachat.example.models;

import com.flyco.tablayout.listener.CustomTabEntity;

public class TabEntity implements CustomTabEntity {

    private String dressType;
    //选中时的图片
    public int selectedIcon;
    //未选中时的图片
    public int unSelectedIcon;

    public TabEntity(String dressType, int selectedIcon, int unSelectedIcon) {
        this.dressType = dressType;
        this.selectedIcon = selectedIcon;
        this.unSelectedIcon = unSelectedIcon;
    }

    public String getDressType() {
        return dressType;
    }

    public void setDressType(String dressType) {
        this.dressType = dressType;
    }

    public int getSelectedIcon() {
        return selectedIcon;
    }

    public void setSelectedIcon(int selectedIcon) {
        this.selectedIcon = selectedIcon;
    }

    public int getUnSelectedIcon() {
        return unSelectedIcon;
    }

    public void setUnSelectedIcon(int unSelectedIcon) {
        this.unSelectedIcon = unSelectedIcon;
    }

    @Override
    public String getTabTitle() {
        return null;
    }

    @Override
    public int getTabSelectedIcon() {
        return this.selectedIcon;
    }

    @Override
    public int getTabUnselectedIcon() {
        return this.unSelectedIcon;
    }
}
