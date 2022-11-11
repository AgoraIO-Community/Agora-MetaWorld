package io.agora.metachat.example.models;

import com.flyco.tablayout.listener.CustomTabEntity;

public class TabEntity implements CustomTabEntity {
    //选中时的图片
    public int selectedIcon;
    //未选中时的图片
    public int unSelectedIcon;

    public TabEntity(int selectedIcon, int unSelectedIcon) {
        this.selectedIcon = selectedIcon;
        this.unSelectedIcon = unSelectedIcon;
    }

    @Override
    public String getTabTitle() {
        return "";
    }

    @Override
    public int getTabSelectedIcon() {
        return selectedIcon;
    }

    @Override
    public int getTabUnselectedIcon() {
        return unSelectedIcon;
    }
}
