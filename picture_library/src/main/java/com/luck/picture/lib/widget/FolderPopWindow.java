package com.luck.picture.lib.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.luck.picture.lib.R;
import com.luck.picture.lib.adapter.PictureAlbumDirectoryAdapter;
import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.decoration.RecycleViewDivider;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.entity.LocalMediaFolder;
import com.luck.picture.lib.tools.ScreenUtils;

import java.util.List;

/**
 * @author：luck
 * @date：2017-5-25 17:02
 * @describe：文件目录PopupWindow
 */

public class FolderPopWindow extends PopupWindow implements View.OnClickListener {
    private Context context;
    private View window;
    private RecyclerView recyclerView;
    private PictureAlbumDirectoryAdapter adapter;
    private Animation animationIn, animationOut;
    private boolean isDismiss = false;
    private LinearLayout id_ll_root;
    private TextView tvPictureTitle;
    private Drawable drawableUp, drawableDown;
    private int chooseMode;
    private PictureSelectionConfig config;

    public FolderPopWindow(Context context, PictureSelectionConfig config) {
        this.context = context;
        this.config = config;
        this.chooseMode = config.chooseMode;
        window = LayoutInflater.from(context).inflate(R.layout.picture_window_folder, null);
        this.setContentView(window);
        this.setWidth(ScreenUtils.getScreenWidth(context));
        this.setHeight(ScreenUtils.getScreenHeight(context));
        this.setAnimationStyle(R.style.WindowStyle);
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.update();
        this.setBackgroundDrawable(new ColorDrawable(Color.argb(123, 0, 0, 0)));

        drawableUp = ContextCompat.getDrawable(context, config.upResId <= 0 ? R.drawable.arrow_up : config.upResId);
        drawableDown = ContextCompat.getDrawable(context, config.downResId <= 0 ? R.drawable.arrow_down : config.downResId);
        animationIn = AnimationUtils.loadAnimation(context, R.anim.photo_album_show);
        animationOut = AnimationUtils.loadAnimation(context, R.anim.photo_album_dismiss);
        initView();
    }

    public void initView() {
        id_ll_root = window.findViewById(R.id.id_ll_root);
        adapter = new PictureAlbumDirectoryAdapter(context, config);
        recyclerView = window.findViewById(R.id.folder_list);
        recyclerView.getLayoutParams().height = (int) (ScreenUtils.getScreenHeight(context) * 0.6);
        recyclerView.addItemDecoration(new RecycleViewDivider(
                context, LinearLayoutManager.HORIZONTAL, ScreenUtils.dip2px(context, 0), ContextCompat.getColor(context, R.color.transparent)));
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
        id_ll_root.setOnClickListener(this);
    }

    public void bindFolder(List<LocalMediaFolder> folders) {
        adapter.setChooseMode(chooseMode);
        adapter.bindFolderData(folders);
    }

    public void setPictureTitleView(TextView tvPictureTitle) {
        this.tvPictureTitle = tvPictureTitle;
    }

    @Override
    public void showAsDropDown(View anchor) {
        try {
            if (Build.VERSION.SDK_INT >= 24) {
                Rect rect = new Rect();
                anchor.getGlobalVisibleRect(rect);
                int h = anchor.getResources().getDisplayMetrics().heightPixels - rect.bottom;
                setHeight(h);
            }
            super.showAsDropDown(anchor);
            isDismiss = false;
            recyclerView.startAnimation(animationIn);
            tvPictureTitle.setCompoundDrawablesRelativeWithIntrinsicBounds
                    (null, null, drawableUp, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setOnItemClickListener(PictureAlbumDirectoryAdapter.OnItemClickListener onItemClickListener) {
        adapter.setOnItemClickListener(onItemClickListener);
    }

    @Override
    public void dismiss() {
        if (isDismiss) {
            return;
        }
        tvPictureTitle.setCompoundDrawablesRelativeWithIntrinsicBounds
                (null, null, drawableDown, null);
        isDismiss = true;
        recyclerView.startAnimation(animationOut);
        dismiss();
        animationOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isDismiss = false;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                    dismiss4Pop();
                } else {
                    FolderPopWindow.super.dismiss();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 在android4.1.1和4.1.2版本关闭PopWindow
     */
    private void dismiss4Pop() {
        new Handler().post(() -> FolderPopWindow.super.dismiss());
    }


    /**
     * 设置选中状态
     */
    public void notifyDataCheckedStatus(List<LocalMedia> medias) {
        try {
            // 获取选中图片
            List<LocalMediaFolder> folders = adapter.getFolderData();
            for (LocalMediaFolder folder : folders) {
                folder.setCheckedNum(0);
            }
            if (medias.size() > 0) {
                for (LocalMediaFolder folder : folders) {
                    int num = 0;// 记录当前相册下有多少张是选中的
                    List<LocalMedia> images = folder.getImages();
                    for (LocalMedia media : images) {
                        String path = media.getPath();
                        for (LocalMedia m : medias) {
                            if (path.equals(m.getPath())) {
                                num++;
                                folder.setCheckedNum(num);
                            }
                        }
                    }
                }
            }
            adapter.bindFolderData(folders);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.id_ll_root) {
            dismiss();
        }
    }

}
