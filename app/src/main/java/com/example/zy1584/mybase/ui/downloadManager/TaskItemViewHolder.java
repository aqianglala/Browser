package com.example.zy1584.mybase.ui.downloadManager;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.zy1584.mybase.R;
import com.example.zy1584.mybase.base.BaseApplication;
import com.example.zy1584.mybase.utils.Utils;
import com.liulishuo.filedownloader.model.FileDownloadStatus;

/**
 * Created by zy1584 on 2017-7-21.
 */

public class TaskItemViewHolder extends RecyclerView.ViewHolder{
    private ImageView iv_icon;
    private TextView tv_name;
    private ProgressBar pb_task;
    private TextView tv_process;
    private TextView tv_status;
    private Button btn_action;

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
    }

    private View findViewById(final int id) {
        return itemView.findViewById(id);
    }

    /**
     * viewHolder position
     */
    private int position;
    /**
     * download id
     */
    private int id;

    public void update(final int id, final int position) {
        this.id = id;
        this.position = position;
    }

    /**
     * 下载完成
     */
    public void updateDownloaded() {
        pb_task.setMax(1);
        pb_task.setProgress(1);

        tv_status.setText(R.string.tasks_manager_demo_status_completed);
        btn_action.setText(R.string.delete);
    }

    /**
     * 下载暂停或失败
     * @param status
     * @param sofar
     * @param total
     */
    public void updateNotDownloaded(final int status, final long sofar, final long total) {
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
        btn_action.setText(R.string.start);
    }

    /**
     * 下载中
     * @param status
     * @param sofar
     * @param total
     */
    public void updateDownloading(final int status, final long sofar, final long total) {
        final float percent = sofar
                / (float) total;
        pb_task.setMax(100);
        pb_task.setProgress((int) (percent * 100));

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
        tv_process.setText(Utils.formatFileSize(sofar) + "/"+ Utils.formatFileSize(total) + "M");
        btn_action.setText(R.string.pause);
    }
}
