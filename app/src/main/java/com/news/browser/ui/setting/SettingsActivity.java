package com.news.browser.ui.setting;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.news.browser.R;
import com.news.browser.base.BaseActivity;
import com.news.browser.base.BasePresenter;
import com.news.browser.bean.EngineBean.EngineItem;
import com.news.browser.bean.UpgradeBean;
import com.news.browser.bus.RXEvent;
import com.news.browser.db.EngineDatabase;
import com.news.browser.preference.PreferenceManager;
import com.news.browser.service.UpdateService;
import com.news.browser.service.UpdateService.IAppStoreUpdateReturn;
import com.news.browser.service.UpdateService.IFileDownloadInfoReturn;
import com.news.browser.ui.about.AboutActivity;
import com.news.browser.ui.feedback.FeedbackActivity;
import com.news.browser.utils.ActivityCollector;
import com.news.browser.utils.FileCacheUtils;
import com.news.browser.utils.GlideCatchUtil;
import com.news.browser.utils.NetUtils;
import com.news.browser.utils.RxBus;
import com.news.browser.utils.Utils;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;
import net.rdrei.android.dirchooser.DirectoryChooserConfig;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class SettingsActivity extends BaseActivity implements IAppStoreUpdateReturn, IFileDownloadInfoReturn {
    private static final int REQUEST_DIRECTORY = 1;

    private PreferenceManager mPreferenceManager;
    private PackageManager mPm;
    private int mLocalVerCode;
    private String mLocalVerName;
    private UpgradeBean mUpgradeBean;
    private int mNewVerCode;
    private String mNewVerName;

    private ProgressBar downloading_pb_task;
    private TextView downloading_tv_status;
    private TextView downloading_tv_speed;
    private Dialog downloading_dialog;

    private EngineDatabase mEngineDatabase;
    private List<EngineItem> mEngineItems = new ArrayList<>();
    private EngineItem mDefaultEngine;

    private Subscription rxSubscription;

    @BindView(R.id.tv_title)
    TextView tv_title;

    @BindView(R.id.tv_rotate)
    TextView tv_rotate;

    @BindView(R.id.tv_engine)
    TextView tv_engine;

    @BindView(R.id.tv_path)
    TextView tv_path;

    @BindView(R.id.tv_version)
    TextView tv_version;

    @BindView(R.id.rl_engine)
    RelativeLayout rl_engine;

    @OnClick(R.id.iv_back)
    void back(){
        finish();
    }

    @OnClick(R.id.rl_rotate)
    void rotate() {
        // TODO: 2017-8-16
    }

    @OnClick(R.id.rl_engine)
    void setEngine() {
        gotoActivity(SetEngineActivity.class);
    }

    @OnClick(R.id.rl_path)
    void setPath() {
        final Intent chooserIntent = new Intent(this, DirectoryChooserActivity.class);

        final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                .newDirectoryName("DirChooserSample")
                .allowReadOnlyDirectory(true)
                .allowNewDirectoryNameModification(true)
                .build();

        chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_CONFIG, config);
        startActivityForResult(chooserIntent, REQUEST_DIRECTORY);
    }

    @OnClick(R.id.rl_check_update)
    void checkUpdate() {
        if (NetUtils.isConnected(this)) {
            if(mNewVerCode>mLocalVerCode){
                checkAppStoreVersion();
            }else{
                toast("已是最新版本");
            }
        } else {
            toast("请连接网络");
        }

    }

    @OnClick(R.id.rl_clear_cache)
    void clearCache() {
        Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                boolean b = GlideCatchUtil.getInstance().cleanCacheDisk();
                subscriber.onNext(b);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        toast(aBoolean ? "清除成功！" : "清除失败！");
                    }
                });
    }

    @OnClick(R.id.rl_feedback)
    void feedback() {
        gotoActivity(FeedbackActivity.class);
    }

    @OnClick(R.id.rl_about)
    void about() {
        gotoActivity(AboutActivity.class);
    }

    @OnClick(R.id.rl_reset)
    void reset() {
        String searchUrl = mPreferenceManager.getSearchUrl();
        mEngineDatabase.setDefaultEngine(searchUrl);
        setDefaultEngine();
        RxBus.getInstance().post(new RXEvent(RXEvent.TAG_UPDATE_DEFAULT_ENGINE, ""));
        mPreferenceManager.setDownloadDirectory(FileCacheUtils.getInstance().getDownloadDirectory());
        tv_path.setText(mPreferenceManager.getDownloadDirectory());
        toast("恢复默认设置成功！");
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_settings;
    }

    @Override
    protected BasePresenter loadPresenter() {
        return null;
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {
        tv_title.setText(R.string.setting);
        mPreferenceManager = PreferenceManager.getInstance();
        tv_path.setText(mPreferenceManager.getDownloadDirectory());

        getAppNewVersion();

        mEngineDatabase = EngineDatabase.getInstance();

        setEngineItemVisibility();
        setDefaultEngine();

        rxSubscription = RxBus.getInstance().toObservable(RXEvent.class)
                .subscribe(new Action1<RXEvent>() {
                    @Override
                    public void call(RXEvent rxEvent) {
                        String tag = rxEvent.getTag();
                        if (tag == RXEvent.TAG_UPDATE_DEFAULT_ENGINE) {
                            setDefaultEngine();
                        }
                    }
                });
    }

    private void getAppNewVersion() {
        mPm = mActivity.getPackageManager();
        try {
            PackageInfo packageInfo = mPm.getPackageInfo(mActivity.getPackageName(), 0);
            mLocalVerCode = packageInfo.versionCode;
            mLocalVerName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        mUpgradeBean = UpdateService.getUpgradeBean();
        if (mUpgradeBean != null) {
            UpgradeBean.DataBean dataBean = mUpgradeBean.getData().get(0);
            mNewVerCode = dataBean.getVersionCode();
            mNewVerName = dataBean.getVersionName();
            if (mNewVerCode > mLocalVerCode) {
                tv_version.setText(mNewVerName);
            } else {
                tv_version.setText(mLocalVerName);
            }
        } else {
            tv_version.setText(mLocalVerName);
        }
    }

    private void setEngineItemVisibility() {
        getEngineList().subscribe(new Action1<List<EngineItem>>() {
            @Override
            public void call(List<EngineItem> engineItems) {
                if (engineItems != null && engineItems.size() > 0) {
                    mEngineItems.clear();
                    mEngineItems.addAll(engineItems);
                    rl_engine.setVisibility(engineItems.size() == 1 ? View.GONE : View.VISIBLE);
                } else {
                    rl_engine.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setDefaultEngine() {
        getDefaultEngine().subscribe(new Action1<EngineItem>() {
            @Override
            public void call(EngineItem engineItem) {
                mDefaultEngine = engineItem;
                if (mDefaultEngine == null) {
                    tv_engine.setText("百度");
                } else {
                    tv_engine.setText(mDefaultEngine.getName());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_DIRECTORY) {
            if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
                handleDirectoryChoice(data
                        .getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR));
            } else {
                // Nothing selected
            }
        }
    }

    private Observable<List<EngineItem>> getEngineList() {
        return Observable.create(new Observable.OnSubscribe<List<EngineItem>>() {
            @Override
            public void call(Subscriber<? super List<EngineItem>> subscriber) {
                List<EngineItem> engineList = mEngineDatabase.getAllEngineItems();
                subscriber.onNext(engineList);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<EngineItem> getDefaultEngine() {
        return Observable.create(new Observable.OnSubscribe<EngineItem>() {
            @Override
            public void call(Subscriber<? super EngineItem> subscriber) {
                EngineItem item = mEngineDatabase.getDefaultEngine();
                subscriber.onNext(item);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private void handleDirectoryChoice(String dir) {
        Log.e(TAG, "handleDirectoryChoice: " + dir);
        mPreferenceManager.setDownloadDirectory(dir);
        tv_path.setText(dir);
    }

    private void checkAppStoreVersion() {

        if (UpdateService.isDownloading()) {
            toast("浏览器正在更新中");
        } else if (UpdateService.isDownloaded(mNewVerCode)) {
            if (!UpdateService.normalInstall(mActivity)) {
                toast("文件已被删除，请重新下载");
            }
        } else {
            showUpdateDialog(mUpgradeBean);
        }
    }

    /**
     * 显示检查更新框
     *
     * @param bean
     */
    private void showUpdateDialog(final UpgradeBean bean) {
        if (bean == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View layout = LayoutInflater.from(this).inflate(R.layout.layout_dialog_update, null);

        TextView tv_version = (TextView) layout.findViewById(R.id.tv_version);
        TextView tv_size = (TextView) layout.findViewById(R.id.tv_size);
        TextView tv_title = (TextView) layout.findViewById(R.id.tv_title_update);
        TextView tv_description = (TextView) layout.findViewById(R.id.tv_description);

        TextView tv_left = (TextView) layout.findViewById(R.id.tv_left);
        TextView tv_right = (TextView) layout.findViewById(R.id.tv_right);

        UpgradeBean.DataBean info = bean.getData().get(0);

        tv_version.setText("版本号：" + info.getVersionName());
        tv_size.setText("大小：" + Utils.formatFileSize(info.getFileSize()) + "M");
        tv_title.setText(info.getUpdateTitle());
        tv_description.setText(info.getUpdateInfo());

        tv_left.setText(info.getIsForce() == 0 ? "下次更新" : "退出");
        tv_right.setText("立即更新");

        builder.setView(layout);
        final Dialog dialog = builder.show();

        tv_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                TextView view = (TextView) v;
                String str = view.getText().toString();
                if ("退出".equals(str)) {
                    ActivityCollector.finishAll();
                }
            }
        });
        tv_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                UpdateService.setFileDownloadInfoListener(SettingsActivity.this);
                UpdateService.startDownloadAppStore(bean);
            }
        });
    }

    @Override
    public void receiveFileDownloadInfo(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        if (downloading_dialog == null) {
            showDownloadingDialog(task, soFarBytes, totalBytes);
        } else {
            if (downloading_dialog.isShowing()) {
                if (task.getStatus() == FileDownloadStatus.completed) {
                    downloading_dialog.dismiss();
                    UpdateService.installApp();
                } else {
                    updateDownloadingDialog(task, soFarBytes, totalBytes);
                }
            }
        }
    }

    private void updateDownloadingDialog(BaseDownloadTask task, int soFarBytes, float totalBytes) {
        final float percent = soFarBytes / totalBytes;
        downloading_pb_task.setMax(100);
        downloading_pb_task.setProgress((int) (percent * 100));

        downloading_tv_status.setText("已下载：" + (int) (percent * 100) + "%");
        downloading_tv_speed.setText(Utils.formatSpeed(task.getSpeed()));
    }

    public void showDownloadingDialog(BaseDownloadTask task, int sofar, int total) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View layout = LayoutInflater.from(this).inflate(R.layout.layout_dialog_progress, null);

        downloading_pb_task = (ProgressBar) layout.findViewById(R.id.pb_task);
        downloading_tv_status = (TextView) layout.findViewById(R.id.tv_status);
        downloading_tv_speed = (TextView) layout.findViewById(R.id.tv_speed);

        updateDownloadingDialog(task, sofar, total);

        builder.setCancelable(false);
        builder.setView(layout);
        downloading_dialog = builder.show();

        layout.findViewById(R.id.tv_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloading_dialog.dismiss();
                UpdateService.cancelDownloadAppStore();
            }
        });

        layout.findViewById(R.id.tv_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloading_dialog.dismiss();
                UpdateService.resetFileDownloadInterface();
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        UpdateService.setCheckVersionInsterface(null, null, null);
    }

    @Override
    public void onStart() {
        super.onStart();
        UpdateService.setCheckVersionInsterface(null, null, this);
    }

    @Override
    public void receiveAppStoreUpdateInfo(int status) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!rxSubscription.isUnsubscribed()) {
            rxSubscription.unsubscribe();
        }
    }
}
