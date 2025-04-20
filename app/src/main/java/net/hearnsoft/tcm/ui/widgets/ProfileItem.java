package net.hearnsoft.tcm.ui.widgets;

public class ProfileItem {
    public static final int TYPE_AVATAR = 0;
    public static final int TYPE_INFO = 1;
    public static final int TYPE_BUTTON = 2;  // 按钮类型，专用于登出按钮
    public static final int TYPE_PREFERENCE_ITEM = 3;
    public static final int TYPE_BANNER = 4;

    private int type;
    private String title;
    private String content;
    private boolean clickable;
    private OnItemClickListener clickListener;

    public ProfileItem(int type, String title, String content) {
        this(type, title, content, false, null);
    }

    public ProfileItem(int type, String title, String content, boolean clickable, OnItemClickListener clickListener) {
        this.type = type;
        this.title = title;
        this.content = content;
        this.clickable = clickable;
        this.clickListener = clickListener;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isClickable() {
        return clickable;
    }

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    public OnItemClickListener getClickListener() {
        return clickListener;
    }

    public void setClickListener(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(ProfileItem item);
    }
}
