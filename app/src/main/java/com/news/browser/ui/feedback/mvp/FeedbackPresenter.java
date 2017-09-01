package com.news.browser.ui.feedback.mvp;

import com.news.browser.base.BasePresenter;
import com.news.browser.bean.FeedbackTypeBean;
import com.news.browser.http.NetProtocol;
import com.news.browser.http.transformer.ScheduleTransformer;
import com.news.browser.ui.feedback.FeedbackActivity;
import com.news.browser.utils.CompressImage;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import rx.Subscriber;
import rx.Subscription;

/**
 * Created by zy1584 on 2017-8-3.
 */

public class FeedbackPresenter extends BasePresenter<FeedbackActivity> implements FeedbackContract.Presenter{

    private final FeedbackBiz mBiz;

    public FeedbackPresenter() {
        mBiz = new FeedbackBiz();
    }

    @Override
    public void getFeedbackType() {
        HashMap<String, String> feedbackMap = NetProtocol.getImpl().getBaseFeedbackMap();
        Subscription subscribe = mBiz.getFeedbackType(feedbackMap).compose(new ScheduleTransformer<FeedbackTypeBean>())
                .subscribe(new Subscriber<FeedbackTypeBean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        getIView().onGetFeedbackTypeError(e);
                    }

                    @Override
                    public void onNext(FeedbackTypeBean bean) {
                        getIView().receiveFeedbackType(bean);
                    }
                });
        addSubscription(subscribe);
    }

    @Override
    public void sendFeedback(int feedType, String content, String contact, List<String> imgList) {
//        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MediaType.parse("multipart/form-data;charset=utf-8"));

        HashMap<String, String> strParam = NetProtocol.getImpl().getFeedbackStrParam(contact, content, feedType);
        for (Map.Entry<String, String> entry : strParam.entrySet()) {
            RequestBody body = RequestBody.create(MediaType.parse("text/plain"), entry.getValue());
            builder.addFormDataPart(entry.getKey(), null, body);
        }

        if (imgList != null && imgList.size() > 0){
            for (String image : imgList){
                File srcFile = new File(image);
                if(!srcFile.exists()) {
                    continue;
                }
                //限制3张图片
                if(imgList.size() > 3) {
                    continue;
                }
                String fileName = srcFile.getName().substring(srcFile.getName().lastIndexOf("."), srcFile.getName().length());
                fileName = getIView().getFilesDir() + "/" + srcFile.lastModified() + fileName;
                boolean bCompress = CompressImage.commpressImage(image, fileName);
                if(!bCompress) {
                    continue;
                }
                RequestBody photo = RequestBody.create(MediaType.parse("image/png"), new File(fileName));
                builder.addFormDataPart("Image", srcFile.getName(), photo);
            }
        }

        mBiz.sendFeedback(builder.build()).compose(new ScheduleTransformer<ResponseBody>())
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        getIView().sendFeedbackError();
                    }

                    @Override
                    public void onNext(ResponseBody body) {
                        getIView().sendFeedbackSuccess();
                    }
                });
    }
}
