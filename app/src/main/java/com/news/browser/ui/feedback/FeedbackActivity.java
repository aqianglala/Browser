package com.news.browser.ui.feedback;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.news.browser.R;
import com.news.browser.base.BaseActivity;
import com.news.browser.bean.FeedbackTypeBean;
import com.news.browser.ui.feedback.mvp.FeedbackContract;
import com.news.browser.ui.feedback.mvp.FeedbackPresenter;
import com.news.browser.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class FeedbackActivity extends BaseActivity<FeedbackPresenter> implements FeedbackContract.View {
    private List<FeedbackTypeBean.TypelistBean> mTypeList = new ArrayList<>();
    private FeedbackTypeAdapter mFeedbackTypeAdapter;
    private FeedbackTypeBean.TypelistBean mCurrentType;
    private ProgressDialog mProgressDialog;
    private Dialog successBuilder;

    @OnClick(R.id.iv_back)
    void back(){
        finish();
    }

    @OnClick(R.id.btn_commit)
    void commit(){
        if (mCurrentType != null){
            if ("请在此快速选择您遇到的问题类型".equals(mCurrentType.getTypeName())){
                toast("反馈类型不能为空！");
                return;
            }
        }
        String content = et_content.getText().toString().trim();
        if (TextUtils.isEmpty(content)){
            tv_warn.setVisibility(View.VISIBLE);
            return;
        }else{
            tv_warn.setVisibility(View.GONE);
        }

        String contact = et_contact.getText().toString().trim();
        if (!TextUtils.isEmpty(contact) && !Utils.isEmail(contact) && !Utils.isTelephoneNum(contact)){
            toast("请输入正确的手机号或邮箱！");
            return;
        }
        ArrayList<String> imageLst = new ArrayList<String>();
        for(int i = 0 ;i<mLinearLayout.getChildCount();i++){
            Object tag = mLinearLayout.getChildAt(i).getTag();
            if(tag!=null){
                imageLst.add(i, (String)tag);
            }
        }
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(R.string.send_message_title);
        mProgressDialog.setMessage(getResources().getString(
                R.string.send_message));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        mPresenter.sendFeedback(mCurrentType.getId(), content, contact, imageLst);
    }
    @BindView(R.id.tv_title)
    TextView tv_title;

    @BindView(R.id.sp_feedback_type)
    Spinner sp_feedback_type;

    @BindView(R.id.et_content)
    EditText et_content;

    @BindView(R.id.tv_length)
    TextView tv_length;

    @BindView(R.id.tv_warn)
    TextView tv_warn;

    @BindView(R.id.et_contact)
    EditText et_contact;

    @BindView(R.id.ll_upload_images)
    LinearLayout mLinearLayout;

    @BindView(R.id.btn_commit)
    Button btn_commit;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_feedback;
    }

    @Override
    protected FeedbackPresenter loadPresenter() {
        return new FeedbackPresenter();
    }

    @Override
    protected void initView() {
        super.initView();
        et_content.addTextChangedListener(mWatcher);
        addUploadImage();

        mFeedbackTypeAdapter = new FeedbackTypeAdapter(mTypeList, this);
        sp_feedback_type .setAdapter(mFeedbackTypeAdapter);
        sp_feedback_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FeedbackTypeBean.TypelistBean bean = mTypeList.get(position);
                if (bean != null){
                    toast(bean.getTypeName());
                    mCurrentType = bean;
                }
                view.setBackgroundResource(R.drawable.shape_feedback_bg_gray);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {
        tv_title.setText(R.string.feedback);
        mPresenter.getFeedbackType();
    }

    @Override
    public void receiveFeedbackType(FeedbackTypeBean bean) {
        if (bean != null){
            List<FeedbackTypeBean.TypelistBean> typeList = bean.getTypelist();
            if (typeList != null && typeList.size() > 0){
                mTypeList.clear();
                FeedbackTypeBean.TypelistBean typelistBean = new FeedbackTypeBean.TypelistBean();
                typelistBean.setTypeName("请在此快速选择您遇到的问题类型");
                mTypeList.add(typelistBean);
                mTypeList.addAll(typeList);
                mFeedbackTypeAdapter.notifyDataSetChanged();
                mCurrentType = typelistBean;
            }
        }
    }

    @Override
    public void onGetFeedbackTypeError(Throwable e) {

    }

    @Override
    public void sendFeedbackSuccess() {
        try {
            mProgressDialog.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.send_succeed_title);
            builder.setMessage(R.string.send_succeed_message);
            builder.setCancelable(false);
            successBuilder = builder.create();
            successBuilder.show();
            mHandler.sendEmptyMessageDelayed(COMMIT_SUCCESS, 2000);//2s关闭activity,返回到设置界面
        } catch (Exception e) {
        }
    }

    @Override
    public void sendFeedbackError() {
        try {
            AlertDialog.Builder failBuilder = new AlertDialog.Builder(this);
            failBuilder.setTitle(R.string.send_failure_title);
            failBuilder.setMessage(R.string.send_failure_message);
            failBuilder.setPositiveButton(R.string.sure,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mProgressDialog.dismiss();
                        }
                    });
            failBuilder.create().show();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private TextWatcher mWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
//            if(et_content.getText().toString()!= null && et_content.getText().length() >0){
//                btn_commit.setEnabled(true);
//            }else{
//                btn_commit.setEnabled(false);
//            }
            tv_length.setText(et_content.getText().length()+"/"+"400");
        }
    };

    private View.OnClickListener mImageViewClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if(v.getTag() != null){
                //点击图片,删除图片
                picRemove(v);
            }else{
                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            }
        };
    };

    protected void picRemove(final View v) {
        AlertDialog.Builder picBuilder = new AlertDialog.Builder(this);
        picBuilder.setMessage(R.string.pic_remove);
        picBuilder.setPositiveButton(R.string.sure,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mLinearLayout.removeView(v);
                        addUploadImage();
                    }
                });
        picBuilder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        picBuilder.create().show();
    }

    /**
     * @Description:添加用户反馈的附件图片
     * void
     */
    private void addUploadImage() {
        int childCount = mLinearLayout.getChildCount();
        for(int i =0; i<childCount;i++){
            if(mLinearLayout.getChildAt(i).getTag() == null){
                if(childCount > 3){
                    //控制用户只能上传3张附图
                    mLinearLayout.getChildAt(i).setVisibility(View.INVISIBLE);
                }else{
                    mLinearLayout.getChildAt(i).setVisibility(View.VISIBLE);
                }
                return;
            }
        }
        ImageView imageView = new ImageView(this);//添加反馈图片图标
        imageView.setImageResource(R.drawable.add_image_button);
        mLinearLayout.addView(imageView, new ViewGroup.LayoutParams(140, 140));
        imageView.setPadding(10, 10, 10, 10);
        imageView.setOnClickListener(mImageViewClickListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1 && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            int c = mLinearLayout.getChildCount();
            for (int i = 0; i < c; i++) {
                Object tag = mLinearLayout.getChildAt(i).getTag();
                if (tag != null && tag.equals(picturePath)) {
                    Toast.makeText(this, R.string.same_pic_message,
                            Toast.LENGTH_LONG).show();
                    return;
                }
            }
            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
            Bitmap createBitmap = centerSquareScaleBitmap(bitmap, 100);
            Resources res = getResources();
            Bitmap proBmp = BitmapFactory.decodeResource(res,
                    R.drawable.feedback_detele_prompt);

            Bitmap newBitmap = doodle(createBitmap, proBmp);
            // bitmap = BitmapFactory.decodeFile(picturePath, options);

            ImageView imageView = new ImageView(this);
            imageView.setOnClickListener(mImageViewClickListener);
            imageView.setPadding(10, 10, 10, 10);
            mLinearLayout.addView(imageView, mLinearLayout.getChildCount() - 1,
                    new ViewGroup.LayoutParams(140, 140));

            imageView.setImageBitmap(newBitmap);
            imageView.setTag(picturePath);//给imageView设置Tag
            addUploadImage();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static Bitmap centerSquareScaleBitmap(Bitmap bitmap, int edgeLength) {
        if (null == bitmap || edgeLength <= 0) {
            return null;
        }
        Bitmap result = bitmap;
        int widthOrg = bitmap.getWidth();
        int heightOrg = bitmap.getHeight();

        if (widthOrg > edgeLength && heightOrg > edgeLength) {
            // 压缩到一个最小长度是edgeLength的bitmap
            int longerEdge = (int) (edgeLength * Math.max(widthOrg, heightOrg) / Math
                    .min(widthOrg, heightOrg));
            int scaledWidth = widthOrg > heightOrg ? longerEdge : edgeLength;
            int scaledHeight = widthOrg > heightOrg ? edgeLength : longerEdge;
            Bitmap scaledBitmap;

            try {
                scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth,
                        scaledHeight, true);
            } catch (Exception e) {
                return null;
            }

            // 从图中截取正中间的正方形部分。
            int xTopLeft = (scaledWidth - edgeLength) / 2;
            int yTopLeft = (scaledHeight - edgeLength) / 2;

            try {
                result = Bitmap.createBitmap(scaledBitmap, xTopLeft, yTopLeft,
                        edgeLength, edgeLength);
                scaledBitmap.recycle();
            } catch (Exception e) {
                return null;
            }
        }
        return result;
    }

    /**
     * 组合涂鸦图片和源图片
     *
     * @param src
     *            源图片
     * @param watermark
     *            涂鸦图片
     * @return
     */
    public Bitmap doodle(Bitmap src, Bitmap watermark) {
        // 另外创建一张图片
        Bitmap newb = Bitmap.createBitmap(src.getWidth(), src.getHeight(),
                Bitmap.Config.ARGB_8888);// 创建一个新的和SRC长度宽度一样的位图
        Canvas canvas = new Canvas(newb);
        canvas.drawBitmap(src, 0, 0, null);// 在 0，0坐标开始画入原图片src
        canvas.drawBitmap(watermark, (src.getWidth() - watermark.getWidth()),
                0, null); // 涂鸦图片画到原图片中间位置
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();

        watermark.recycle();
        watermark = null;

        return newb;
    }
    private static final int COMMIT_SUCCESS = 1;
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case COMMIT_SUCCESS:
                    try {
                        if(successBuilder!=null && successBuilder.isShowing()){
                            successBuilder.dismiss();
                            finish();
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                    break;
                default:
                    break;
            }
        };
    };
}
