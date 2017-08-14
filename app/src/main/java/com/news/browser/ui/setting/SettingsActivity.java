package com.news.browser.ui.setting;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.news.browser.R;
import com.news.browser.base.BaseActivity;
import com.news.browser.base.BasePresenter;
import com.news.browser.preference.PreferenceManager;
import com.news.browser.ui.feedback.FeedbackActivity;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;
import net.rdrei.android.dirchooser.DirectoryChooserConfig;

import butterknife.BindView;
import butterknife.OnClick;


public class SettingsActivity extends BaseActivity {
    private static final int REQUEST_DIRECTORY = 1;
    @BindView(R.id.tv_rotate)
    TextView tv_rotate;

    @BindView(R.id.tv_engine)
    TextView tv_engine;

    @BindView(R.id.tv_path)
    TextView tv_path;

    @BindView(R.id.tv_version)
    TextView tv_version;
    private PreferenceManager mPreferenceManager;

    @OnClick(R.id.rl_rotate)
    void rotate(){

    }
    @OnClick(R.id.rl_engine)
    void setEngine(){

    }
    @OnClick(R.id.rl_path)
    void setPath(){
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
    void checkUpdate(){

    }
    @OnClick(R.id.rl_clear_cache)
    void clearCache(){

    }
    @OnClick(R.id.rl_feedback)
    void feedback(){
        gotoActivity(FeedbackActivity.class);
    }
    @OnClick(R.id.rl_about)
    void about(){

    }
    @OnClick(R.id.rl_reset)
    void reset(){

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
        mPreferenceManager = PreferenceManager.getInstance();
        tv_path.setText(mPreferenceManager.getDownloadDirectory());
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

    private void handleDirectoryChoice(String dir) {
        Log.e(TAG, "handleDirectoryChoice: " + dir );
        mPreferenceManager.setDownloadDirectory(dir);
        tv_path.setText(dir);
    }
}
