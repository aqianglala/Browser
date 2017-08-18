package com.news.browser.ui.download;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadConnectListener;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.liulishuo.filedownloader.util.FileDownloadUtils;
import com.news.browser.R;
import com.news.browser.base.BaseActivity;
import com.news.browser.base.BaseApplication;
import com.news.browser.base.BasePresenter;
import com.news.browser.bus.RXEvent;
import com.news.browser.dialog.BrowserDialog;
import com.news.browser.dialog.FileDatabase;
import com.news.browser.preference.PreferenceManager;
import com.news.browser.ui.download.db.FileItem;
import com.news.browser.utils.FileOpenUtils;
import com.news.browser.utils.RxBus;
import com.news.browser.utils.Utils;
import com.news.browser.widget.DividerItemDecoration;
import com.news.browser.widget.RecyclerViewEmptySupport;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;


public class DownloadManagerActivity extends BaseActivity {

    private static Context mContext;
    private TaskItemAdapter adapter;

    @BindView(R.id.tv_title)
    TextView tv_title;

    @BindView(R.id.id_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.ll_empty)
    LinearLayout ll_empty;

    @OnClick(R.id.iv_back)
    void back(){
        finish();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_download_manager;
    }

    @Override
    protected BasePresenter loadPresenter() {
        return null;
    }

    @Override
    protected void initView() {
        super.initView();
        mContext = this;

        final RecyclerViewEmptySupport recyclerView = (RecyclerViewEmptySupport) findViewById(R.id.recycler_view);

        recyclerView.setEmptyView(ll_empty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setAdapter(adapter = new TaskItemAdapter());

        // 链接服务
        TasksManager.getImpl().onCreate(new WeakReference<>(this));
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {
        tv_title.setText(R.string.download_title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        TasksManager.getImpl().onDestroy();
        adapter = null;
//        FileDownloader.getImpl().pauseAll();
        super.onDestroy();
    }

    public void postNotifyDataChanged() {
        if (adapter != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    private static class TaskItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_icon;
        private TextView tv_name;
        private ProgressBar pb_task;
        private TextView tv_process;
        private TextView tv_status;
        private Button btn_action;
        private ImageView iv_delete;

        public TaskItemViewHolder(View itemView) {
            super(itemView);
            assignViews();
        }

        private void assignViews() {
            iv_icon = (ImageView) findViewById(R.id.iv_icon);
            tv_name = (TextView) findViewById(R.id.tv_name);
            pb_task = (ProgressBar) findViewById(R.id.pb_task);
            tv_process = (TextView) findViewById(R.id.tv_process);
            tv_status = (TextView) findViewById(R.id.tv_status);
            btn_action = (Button) findViewById(R.id.btn_action);
            iv_delete = (ImageView) findViewById(R.id.iv_delete);
        }

        private View findViewById(final int id) {
            return itemView.findViewById(id);
        }

        /**
         * download id
         */
        private int id;

        public void update(final int id) {
            this.id = id;
        }

        /**
         * 下载完成
         */
        public void updateDownloaded(long total) {
//            pb_task.setMax(1);
//            pb_task.setProgress(1);
            pb_task.setVisibility(View.GONE);
            tv_process.setText(Utils.formatFileSize(total) + "M");
            tv_status.setVisibility(View.GONE);
            btn_action.setText(R.string.open);
        }

        /**
         * 下载暂停或失败
         *
         * @param status
         * @param sofar
         * @param total
         */
        public void updateNotDownloaded(final int status, final long sofar, final long total) {
            pb_task.setVisibility(View.VISIBLE);
            tv_status.setVisibility(View.VISIBLE);
            if (sofar > 0 && total > 0) {
                final float percent = sofar
                        / (float) total;
                pb_task.setMax(100);
                pb_task.setProgress((int) (percent * 100));
            } else {
                pb_task.setMax(1);
                pb_task.setProgress(0);
            }

            switch (status) {
                case FileDownloadStatus.error:
                    tv_status.setText(R.string.tasks_manager_demo_status_error);
                    break;
                case FileDownloadStatus.paused:
                    tv_status.setText(R.string.tasks_manager_demo_status_paused);
                    break;
                default:
                    tv_status.setText(R.string.tasks_manager_demo_status_not_downloaded);
                    break;
            }
            tv_process.setText(Utils.formatFileSize(sofar) + "/" + Utils.formatFileSize(total) + "M");
            btn_action.setText(R.string.start);
        }

        /**
         * 下载中
         *
         * @param status
         * @param sofar
         * @param total
         */
        public void updateDownloading(final int status, final long sofar, final long total) {
            final float percent = sofar
                    / (float) total;
            pb_task.setVisibility(View.VISIBLE);
            pb_task.setMax(100);
            pb_task.setProgress((int) (percent * 100));

            tv_status.setVisibility(View.VISIBLE);
            switch (status) {
                case FileDownloadStatus.pending:// 等待中
                    tv_status.setText(R.string.tasks_manager_demo_status_pending);
                    break;
                case FileDownloadStatus.started:// 等待结束
                    tv_status.setText(R.string.tasks_manager_demo_status_started);
                    break;
                case FileDownloadStatus.connected:// 连接成功
                    tv_status.setText(R.string.tasks_manager_demo_status_connected);
                    break;
                case FileDownloadStatus.progress:// 进度回调
                    break;
                default:
                    tv_status.setText(BaseApplication.getContext().getString(
                            R.string.tasks_manager_demo_status_downloading, status));
                    break;
            }
            tv_process.setText(Utils.formatFileSize(sofar) + "/" + Utils.formatFileSize(total) + "M");
            btn_action.setText(R.string.pause);
        }
    }

    private static class TaskItemAdapter extends RecyclerView.Adapter<TaskItemViewHolder> {

        private FileDownloadListener taskDownloadListener = new FileDownloadSampleListener() {

            // 获取holder，局部更新
            private TaskItemViewHolder checkCurrentHolder(final BaseDownloadTask task) {
                final TaskItemViewHolder tag = (TaskItemViewHolder) task.getTag();
                if (tag.id != task.getId()) {
                    throw new RuntimeException("tag.id != task.getId");
                }

                return tag;
            }

            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                super.pending(task, soFarBytes, totalBytes);
                final TaskItemViewHolder tag = checkCurrentHolder(task);
                if (tag == null) {
                    return;
                }

                tag.updateDownloading(FileDownloadStatus.pending, soFarBytes
                        , totalBytes);
            }

            @Override
            protected void started(BaseDownloadTask task) {
                super.started(task);
                final TaskItemViewHolder tag = checkCurrentHolder(task);
                if (tag == null) {
                    return;
                }
                tag.tv_status.setVisibility(View.VISIBLE);
                tag.tv_status.setText(R.string.tasks_manager_demo_status_started);
            }

            @Override
            protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                super.connected(task, etag, isContinue, soFarBytes, totalBytes);
                final TaskItemViewHolder tag = checkCurrentHolder(task);
                if (tag == null) {
                    return;
                }
                tag.updateDownloading(FileDownloadStatus.connected, soFarBytes
                        , totalBytes);
            }

            @Override
            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                super.progress(task, soFarBytes, totalBytes);
                final TaskItemViewHolder tag = checkCurrentHolder(task);
                if (tag == null) {
                    return;
                }
                tag.updateDownloading(FileDownloadStatus.progress, soFarBytes
                        , totalBytes);
                tag.tv_status.setText(Utils.formatSpeed(task.getSpeed()));
            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e) {
                super.error(task, e);
                final TaskItemViewHolder tag = checkCurrentHolder(task);
                if ("Connection reset by peer".equals(e.getMessage())) {
                    System.setProperty("http.keepAlive", "false");
                }
                if (tag == null) {
                    return;
                }

                tag.updateNotDownloaded(FileDownloadStatus.error, task.getLargeFileSoFarBytes()
                        , task.getLargeFileTotalBytes());
                TasksManager.getImpl().removeTaskForViewHolder(task.getId());
            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                super.paused(task, soFarBytes, totalBytes);
                final TaskItemViewHolder tag = checkCurrentHolder(task);
                if (tag == null) {
                    return;
                }
                tag.updateNotDownloaded(FileDownloadStatus.paused, soFarBytes, totalBytes);
                TasksManager.getImpl().removeTaskForViewHolder(task.getId());
            }

            @Override
            protected void completed(BaseDownloadTask task) {
                super.completed(task);
                final TaskItemViewHolder tag = checkCurrentHolder(task);
                if (tag == null) {
                    return;
                }
                setApkIcon(tag, task.getPath(), task.getStatus());
                long size = TasksManager.getImpl().getById(task.getId()).getSize();
                tag.updateDownloaded(size);
                TasksManager.getImpl().removeTaskForViewHolder(task.getId());

            }
        };
        private View.OnClickListener taskActionOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag() == null) {
                    return;
                }

                TaskItemViewHolder holder = (TaskItemViewHolder) v.getTag();

                CharSequence action = ((TextView) v).getText();
                if (action.equals(v.getResources().getString(R.string.pause))) {
                    // to pause
                    FileDownloader.getImpl().pause(holder.id);
                } else if (action.equals(v.getResources().getString(R.string.start))) {
                    // to start
                    // to start
                    final FileItem model = TasksManager.getImpl().get(holder.getLayoutPosition());
                    final BaseDownloadTask task = FileDownloader.getImpl().create(model.getUrl())
                            .setPath(model.getPath())
                            .setCallbackProgressTimes(100)
                            .setAutoRetryTimes(5)
                            .setListener(taskDownloadListener);

                    TasksManager.getImpl()
                            .addTaskForViewHolder(task);

                    TasksManager.getImpl()
                            .updateViewHolder(holder.id, holder);

                    task.start();
                } else if (action.equals(v.getResources().getString(R.string.delete))) {
                    // to delete
                    new File(TasksManager.getImpl().get(holder.getLayoutPosition()).getPath()).delete();
                    holder.btn_action.setEnabled(true);
                    holder.updateNotDownloaded(FileDownloadStatus.INVALID_STATUS, 0, 0);
                } else if (action.equals(v.getResources().getString(R.string.open))) {
                    // to open
                    final FileItem model = TasksManager.getImpl().get(holder.getLayoutPosition());
//                    BaseDownloadTask task = TasksManager.getImpl().getTaskById(model.getId());
//                    if (task != null && task.getStatus() != FileDownloadStatus.completed) return;
//                    Intent intent = FileOpenUtils.openFile(model.getPath());
//                    if (intent != null) {
//                        BaseApplication.getContext().startActivity(intent);
//                    }
                    openTask(v, model, holder);
                }
            }
        };

        @Override
        public TaskItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TaskItemViewHolder holder = new TaskItemViewHolder(
                    LayoutInflater.from(
                            parent.getContext())
                            .inflate(R.layout.item_list_download, parent, false));

            holder.btn_action.setOnClickListener(taskActionOnClickListener);
            return holder;
        }

        @Override
        public void onBindViewHolder(final TaskItemViewHolder holder, final int position) {
            final FileItem model = TasksManager.getImpl().get(position);

            holder.update(model.getId());
            holder.btn_action.setTag(holder);

            holder.tv_name.setText(model.getName());

            TasksManager.getImpl()
                    .updateViewHolder(holder.id, holder);
            holder.btn_action.setEnabled(true);
            holder.iv_delete.setEnabled(true);

            if (TasksManager.getImpl().isReady()) {
                final int status = TasksManager.getImpl().getStatus(model.getId(), model.getPath());
                // 监听
                BaseDownloadTask task = TasksManager.getImpl().getTaskById(model.getId());
                setApkIcon(holder, model.getPath(), status);

                if (task != null && task.getListener() == null) {
                    task.setListener(taskDownloadListener);
                }
                if (status == FileDownloadStatus.pending || status == FileDownloadStatus.started ||
                        status == FileDownloadStatus.connected) {
                    // start task, but file not created yet
                    holder.updateDownloading(status, TasksManager.getImpl().getSoFar(model.getId())
                            , TasksManager.getImpl().getTotal(model.getId()));
                } else if (!new File(model.getPath()).exists() &&
                        !new File(FileDownloadUtils.getTempPath(model.getPath())).exists()) {
                    // not exist file
                    holder.updateNotDownloaded(status, 0, 0);
                } else if (TasksManager.getImpl().isDownloaded(status)) {
                    // already downloaded and exist
                    holder.updateDownloaded(model.getSize());
                } else if (status == FileDownloadStatus.progress) {
                    // downloading
                    holder.updateDownloading(status, TasksManager.getImpl().getSoFar(model.getId())
                            , TasksManager.getImpl().getTotal(model.getId()));
                } else {
                    // not start
                    holder.updateNotDownloaded(status, TasksManager.getImpl().getSoFar(model.getId())
                            , TasksManager.getImpl().getTotal(model.getId()));
                }
            } else {
                holder.tv_status.setVisibility(View.VISIBLE);
                holder.tv_status.setText(R.string.tasks_manager_demo_status_loading);
                holder.btn_action.setEnabled(false);
                holder.iv_delete.setEnabled(false);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openTask(v, model, holder);

                }
            });
            holder.iv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BrowserDialog.showConfirm(mContext, R.string.prompt_confirm, R.string.prompt_deleted, null,
                            new BrowserDialog.Item(R.string.sure) {
                                @Override
                                public void onClick() {
                                    int pos = holder.getLayoutPosition();
                                    FileDownloader.getImpl().clear(model.getId(), model.getPath());

                                    TasksManager.getImpl().removeTaskForViewHolder(model.getId());
                                    TasksManager.getImpl().removeTaskFromDB(model.getId());
                                    TasksManager.getImpl().removeItem(pos);

//                    notifyItemRemoved(pos);
                                    notifyDataSetChanged();
                                }
                            });
                }
            });

        }

        private void openTask(View v, FileItem model, final TaskItemViewHolder holder) {
            BaseDownloadTask task = TasksManager.getImpl().getTaskById(model.getId());
            if (task != null && task.getStatus() != FileDownloadStatus.completed) return;
            String path = model.getPath();
            if (!TextUtils.isEmpty(path)) {
                if (new File(path).exists()) {
                    Intent intent = FileOpenUtils.openFile(model.getPath());
                    if (intent != null) {
                        BaseApplication.getContext().startActivity(intent);
                    }
                } else {
                    String action = holder.btn_action.getText().toString();
                    if (action.equals(v.getResources().getString(R.string.open))) {
                        BrowserDialog.showConfirm(mContext, R.string.prompt_confirm, R.string.prompt_file_deleted, null,
                                new BrowserDialog.Item(R.string.action_ok) {
                                    @Override
                                    public void onClick() {
                                        final FileItem model = TasksManager.getImpl().get(holder.getLayoutPosition());
                                        final BaseDownloadTask task = FileDownloader.getImpl().create(model.getUrl())
                                                .setPath(model.getPath())
                                                .setCallbackProgressTimes(100)
                                                .setAutoRetryTimes(5)
                                                .setListener(taskDownloadListener);

                                        TasksManager.getImpl()
                                                .addTaskForViewHolder(task);

                                        TasksManager.getImpl()
                                                .updateViewHolder(holder.id, holder);

                                        task.start();
                                    }
                                });
                    }
                }
            }
        }

        private void setApkIcon(TaskItemViewHolder holder, String path, int status) {
            if (path.endsWith(".apk") && status == FileDownloadStatus.completed) {
                Drawable apkIcon = Utils.getApkIcon(mContext, path);
                holder.iv_icon.setImageDrawable(apkIcon);
            } else {
                holder.iv_icon.setImageResource(FileOpenUtils.getIconResId(path));
            }

        }

        @Override
        public int getItemCount() {
            return TasksManager.getImpl().getTaskCounts();
        }
    }

    public static class TasksManager {
        private FileDatabase dbController;
        private PreferenceManager mPreferenceManager;
        private List<FileItem> itemList;

        private final static class HolderClass {
            private final static TasksManager INSTANCE
                    = new TasksManager();
        }

        public static TasksManager getImpl() {
            return HolderClass.INSTANCE;
        }

        private TasksManager() {
            dbController = FileDatabase.getInstance();
            itemList = dbController.getAllFileItems();
            mPreferenceManager = PreferenceManager.getInstance();
        }

        private SparseArray<BaseDownloadTask> taskSparseArray = new SparseArray<>();

        public void addTaskForViewHolder(final BaseDownloadTask task) {
            taskSparseArray.put(task.getId(), task);
        }

        public void removeTaskForViewHolder(final int id) {
            taskSparseArray.remove(id);
        }

        public void removeItem(int position) {
            itemList.remove(position);
        }

        public void updateViewHolder(final int id, final TaskItemViewHolder holder) {
            final BaseDownloadTask task = taskSparseArray.get(id);
            if (task == null) {
                return;
            }

            task.setTag(holder);
        }

        public BaseDownloadTask getTaskById(int id) {
            return taskSparseArray.get(id);
        }

        public void releaseTask() {
            taskSparseArray.clear();
        }

        private FileDownloadConnectListener listener;

        private void registerServiceConnectionListener(final WeakReference<DownloadManagerActivity>
                                                               activityWeakReference) {
            if (listener != null) {
                FileDownloader.getImpl().removeServiceConnectListener(listener);
            }

            listener = new FileDownloadConnectListener() {

                @Override
                public void connected() {
                    if (activityWeakReference == null
                            || activityWeakReference.get() == null) {
                        return;
                    }

                    activityWeakReference.get().postNotifyDataChanged();
                }

                @Override
                public void disconnected() {
                    if (activityWeakReference == null
                            || activityWeakReference.get() == null) {
                        return;
                    }

                    activityWeakReference.get().postNotifyDataChanged();
                }
            };

            FileDownloader.getImpl().addServiceConnectListener(listener);
        }

        private void unregisterServiceConnectionListener() {
            FileDownloader.getImpl().removeServiceConnectListener(listener);
            listener = null;
        }

        public void onCreate(final WeakReference<DownloadManagerActivity> activityWeakReference) {
            if (!FileDownloader.getImpl().isServiceConnected()) {
                FileDownloader.getImpl().bindService();
            }
            registerServiceConnectionListener(activityWeakReference);
        }

        public void onDestroy() {
            unregisterServiceConnectionListener();
//            releaseTask();
        }

        public boolean isReady() {
            return FileDownloader.getImpl().isServiceConnected();
        }

        public FileItem get(final int position) {
            return itemList.get(position);
        }

        public FileItem getById(final int id) {
            for (FileItem model : itemList) {
                if (model.getId() == id) {
                    return model;
                }
            }

            return null;
        }

        /**
         * 判断是否存在相同的下载任务
         *
         * @param url
         * @return
         */
        public FileItem getByUrl(final String url) {
            if (TextUtils.isEmpty(url)) return null;
            for (FileItem model : itemList) {
                if (url.equals(model.getUrl())) {
                    return model;
                }
            }

            return null;
        }

        /**
         * @param status Download Status
         * @return has already downloaded
         * @see FileDownloadStatus
         */
        public boolean isDownloaded(final int status) {
            return status == FileDownloadStatus.completed;
        }

        public int getStatus(final int id, String path) {
            return FileDownloader.getImpl().getStatus(id, path);
        }

        public long getTotal(final int id) {
            return FileDownloader.getImpl().getTotal(id);
        }

        public long getSoFar(final int id) {
            return FileDownloader.getImpl().getSoFar(id);
        }

        public int getTaskCounts() {
            return itemList.size();
        }

        public void removeTaskFromDB(int id) {
            dbController.deleteFileItem(id);
        }

        public FileItem addTask(final String url, String name) {
            return addTask(url, name, "", "");
        }

        public FileItem addTask(final String url, String name, String click_id, String conversion_link) {
            if (TextUtils.isEmpty(url)) {
                return null;
            }
            FileItem model = getByUrl(url);
            if (model != null) {
                model.setClickId(click_id);// 广告联盟
                model.setConversionLink(conversion_link);// 广告联盟
                RxBus.getInstance().post(new RXEvent(RXEvent.TAG_BROWSER_MSG, R.string.prompt_task_exist));
                return model;
            }
            String path = createPath(url, name);
            if (new File(path).exists()) {// 存在同名文件
                RxBus.getInstance().post(new RXEvent(RXEvent.TAG_BROWSER_MSG, R.string.prompt_same_file_exist));
                return null;
            }

            final int id = FileDownloadUtils.generateId(url, path);
            FileItem newModel = new FileItem(id, url, name, 0, 0, path);
            newModel.setClickId(click_id);// 广告联盟
            newModel.setConversionLink(conversion_link);// 广告联盟

            dbController.updateFileItem(newModel);
            itemList.add(newModel);

            BaseDownloadTask task = getTaskById(id);
            if (task == null) {
                task = FileDownloader.getImpl().create(newModel.getUrl())
                        .setPath(newModel.getPath())
                        .setAutoRetryTimes(5)
                        .setCallbackProgressTimes(100);
                task.start();
                addTaskForViewHolder(task);
            }

            return newModel;
        }

        public String createPath(final String url, String name) {
            if (TextUtils.isEmpty(url)) {
                return null;
            }
            String path = mPreferenceManager.getDownloadDirectory() + "/" + name;
            return path;
        }

        public List<FileItem> getApkTasks() {
            ArrayList<FileItem> apkItems = new ArrayList<>();
            for (FileItem item : itemList) {
                String path = item.getPath();
                if (path.endsWith(".apk")) {
                    apkItems.add(item);
                }
            }
            return apkItems;
        }

        public boolean isApk(BaseDownloadTask task) {
            if (task == null) return false;
            String path = task.getPath();
            if (!TextUtils.isEmpty(path) && path.endsWith(".apk")) {
                if (new File(path).exists()) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 正常安装
         *
         * @param localPath
         * @param context
         * @return
         */
        public synchronized boolean normalInstall(String localPath, Context context) {
            File file = new File(localPath);
            if (!file.exists())
                return false;
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.addCategory("android.intent.category.DEFAULT");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            context.startActivity(intent);
            return true;
        }

        /**
         * 更新文件大小
         *
         * @param id
         * @param total
         */
        public void updateModelSize(int id, long total) {
            if (total == 0) return;
            FileItem item = getById(id);
            if (item != null && item.getSize() == 0) {
                item.setSize(total);
                dbController.updateFileItem(item);
            }
        }

        public void removeTaskListener(int id) {
            BaseDownloadTask task = getTaskById(id);
            if (task != null) {
                task.setListener(null);
            }
        }

        public void addTaskListener(int id, FileDownloadListener listener) {
            BaseDownloadTask task = getTaskById(id);
            if (task != null) {
                task.setListener(listener);
            }
        }
    }
}
