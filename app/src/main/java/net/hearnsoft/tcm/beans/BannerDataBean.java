package net.hearnsoft.tcm.beans;

public class BannerDataBean {

    int imgRes;
    String imgResUrl;
    String imgContentUrl;
    String imgDesc;

    /**
     * 空方法
     */
    public BannerDataBean() {}

    /**
     * 本地resource加载构造方法
     * @param imgRes 图片资源的resource ID
     */
    public BannerDataBean(int imgRes) {
        this.imgRes = imgRes;
    }

    /**
     * 本地resource加载构造方法
     * @param imgRes 图片资源的resource ID
     * @param imgContentUrl 图片资源指向的内容URL
     */
    public BannerDataBean(int imgRes, String imgContentUrl) {
        this.imgRes = imgRes;
        this.imgContentUrl = imgContentUrl;
    }

    /**
     * 本地resource加载构造方法
     * @param imgRes 图片资源的resource ID
     * @param imgContentUrl 图片资源指向的内容URL
     * @param imgDesc 图片资源的描述，用于无障碍
     */
    public BannerDataBean(int imgRes, String imgContentUrl, String imgDesc){
        this.imgRes = imgRes;
        this.imgContentUrl = imgContentUrl;
        this.imgDesc = imgDesc;
    }

    /**
     * 网络resource加载构造方法
     * @param imgResUrl 图片资源的URL
     */
    public BannerDataBean(String imgResUrl) {
        this.imgResUrl = imgResUrl;
    }

    /**
     * 网络resource加载构造方法
     * @param imgResUrl 图片资源的URL
     * @param imgContentUrl 图片资源指向的内容URL
     */
    public BannerDataBean(String imgResUrl, String imgContentUrl) {
        this.imgResUrl = imgResUrl;
        this.imgContentUrl = imgContentUrl;
    }

    /**
     * 网络resource加载构造方法
     * @param imgResUrl 图片资源的URL
     * @param imgContentUrl 图片资源指向的内容URL
     * @param imgDesc 图片资源的描述，用于无障碍
     */
    public BannerDataBean(String imgResUrl, String imgContentUrl, String imgDesc){
        this.imgResUrl = imgResUrl;
        this.imgContentUrl = imgContentUrl;
        this.imgDesc = imgDesc;
    }

    public int getImgRes() {
        return imgRes;
    }

    public void setImgRes(int imgRes) {
        this.imgRes = imgRes;
    }

    public String getImgResUrl() {
        return imgResUrl;
    }

    public void setImgResUrl(String imgResUrl) {
        this.imgResUrl = imgResUrl;
    }

    public String getImgContentUrl() {
        return imgContentUrl;
    }

    public void setImgContentUrl(String imgContentUrl) {
        this.imgContentUrl = imgContentUrl;
    }

    public String getImgDesc() {
        return imgDesc;
    }

    public void setImgDesc(String imgDesc) {
        this.imgDesc = imgDesc;
    }
}
