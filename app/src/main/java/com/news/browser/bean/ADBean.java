package com.news.browser.bean;

import android.graphics.Rect;
import android.view.View;

import java.util.List;

/**
 * Created by zy1584 on 2017-9-4.
 */

public class ADBean implements BaseNewsItem {
    /**
     * click_link : http://c.gdt.qq.com/gdt_mclick.fcg
     * conversion_link : http://t.gdt.qq.com/conv/allianc
     * crt_type : 3
     * description : 咚漫漫画，精彩漫画必备神器
     * img_url : http://pgdt.gtimg.cn/gdt/0/DAAO6K7ABIABIA
     * impression_link : http://v.gdt.qq.com/gdt_stats.fc
     * interact_type : 1
     * is_full_screen_interstitial : true
     * snapshot_url : ["http://pgdt.gtimg.cn/gdt/0/DAAO6K7AFKAImAAqBZGoxcCNlowQpR.jpg/0?ck=7d880bb20b31a5977f41fa7e8ae3cf96","http://pgdt.gtimg.cn/gdt/0/DAAO6K7AFKAImAAtBZGoxcC3z-ggUT.jpg/0?ck=8e2d9fce1681866f84fe170dded08d79"]
     * title : 咚漫
     */

    private String click_link;
    private String conversion_link;
    private int crt_type;
    private String description;
    private String img_url;
    private String impression_link;
    private int interact_type;
    private boolean is_full_screen_interstitial;
    private String title;
    private List<String> snapshot_url;

    public String getClick_link() {
        return click_link;
    }

    public void setClick_link(String click_link) {
        this.click_link = click_link;
    }

    public String getConversion_link() {
        return conversion_link;
    }

    public void setConversion_link(String conversion_link) {
        this.conversion_link = conversion_link;
    }

    public int getCrt_type() {
        return crt_type;
    }

    public void setCrt_type(int crt_type) {
        this.crt_type = crt_type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getImpression_link() {
        return impression_link;
    }

    public void setImpression_link(String impression_link) {
        this.impression_link = impression_link;
    }

    public int getInteract_type() {
        return interact_type;
    }

    public void setInteract_type(int interact_type) {
        this.interact_type = interact_type;
    }

    public boolean isIs_full_screen_interstitial() {
        return is_full_screen_interstitial;
    }

    public void setIs_full_screen_interstitial(boolean is_full_screen_interstitial) {
        this.is_full_screen_interstitial = is_full_screen_interstitial;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getSnapshot_url() {
        return snapshot_url;
    }

    public void setSnapshot_url(List<String> snapshot_url) {
        this.snapshot_url = snapshot_url;
    }

    /**
     * 广告曝光上报
     */
    private boolean isThirdTiming;// 广告是否已经在上报的流程中，第三方统计
    private boolean hasExpose2Third;// 广告是否已经曝光上报，第三方统计

    private boolean isSelfTiming;// 广告是否已经在上报的流程中，自营统计
    private boolean hasExpose2Self;// 广告是否已经曝光上报，自营统计

    private final Rect mCurrentViewRect = new Rect();

    public boolean isThirdTiming() {
        return isThirdTiming;
    }

    public void setThirdTiming(boolean thirdTiming) {
        isThirdTiming = thirdTiming;
    }

    public boolean isHasExpose2Third() {
        return hasExpose2Third;
    }

    public void setHasExpose2Third(boolean hasExpose2Third) {
        this.hasExpose2Third = hasExpose2Third;
    }

    public boolean isSelfTiming() {
        return isSelfTiming;
    }

    public void setSelfTiming(boolean selfTiming) {
        isSelfTiming = selfTiming;
    }

    public boolean isHasExpose2Self() {
        return hasExpose2Self;
    }

    public void setHasExpose2Self(boolean hasExpose2Self) {
        this.hasExpose2Self = hasExpose2Self;
    }

    public int getVisibilityPercents(View view) {

        int percents = 100;

        view.getLocalVisibleRect(mCurrentViewRect);

        int height = view.getHeight();

        if (viewIsPartiallyHiddenTop()) {
            // mContentView is partially hidden behind the top edge
            percents = (height - mCurrentViewRect.top) * 100 / height;
        } else if (viewIsPartiallyHiddenBottom(height)) {
            percents = mCurrentViewRect.bottom * 100 / height;
        }
        return percents;
    }

    private boolean viewIsPartiallyHiddenBottom(int height) {
        return mCurrentViewRect.bottom > 0 && mCurrentViewRect.bottom < height;
    }

    private boolean viewIsPartiallyHiddenTop() {
        return mCurrentViewRect.top > 0;
    }
}
