package com.example.ppjoke.ui.detail;

import android.view.LayoutInflater;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import com.example.ppjoke.R;
import com.example.ppjoke.databinding.LayoutFeedDetailTypeVideoBinding;
import com.example.ppjoke.databinding.LayoutFeedDetailTypeVideoHeaderBinding;
import com.example.ppjoke.model.Feed;
import com.example.ppjoke.view.FullScreenPlayerView;

public class VideoViewHandler extends ViewHandler {
    private final CoordinatorLayout coordinator;
    private FullScreenPlayerView playerView;
    private LayoutFeedDetailTypeVideoBinding mVideoBinding;
    private String category;
    private boolean backPressed;

    public VideoViewHandler(FragmentActivity activity) {
        super(activity);

        mVideoBinding = DataBindingUtil.setContentView(activity, com.example.ppjoke.R.layout.layout_feed_detail_type_video);

        mInateractionBinding = mVideoBinding.bottomInteraction;
        mRecyclerView = mVideoBinding.recyclerView;
        playerView = mVideoBinding.playerView;
        coordinator = mVideoBinding.coordinator;

        View authorInfoView = mVideoBinding.authorInfo.getRoot();
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) authorInfoView.getLayoutParams();
        params.setBehavior(new ViewAnchorBehavior(R.id.player_view));

        mVideoBinding.actionClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.finish();
            }
        });

        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) playerView.getLayoutParams();
        ViewZoomBehavior behavior = (ViewZoomBehavior) layoutParams.getBehavior();
        behavior.setViewZoomCallback(new ViewZoomBehavior.ViewZoomCallback() {
            @Override
            public void onDragZoom(int height) {
                int bottom = playerView.getBottom();
                boolean moveUp = height < bottom;
                boolean fullscreen = moveUp ? height >= coordinator.getBottom() - mInateractionBinding.getRoot().getHeight()
                        : height >= coordinator.getBottom();
                setViewAppearance(fullscreen);
            }
        });

    }

    @Override
    public void bindInitData(Feed feed) {
        super.bindInitData(feed);
        mVideoBinding.setFeed(feed);

        category = mActivity.getIntent().getStringExtra(FeedDetailActivity.KEY_CATEGORY);
        playerView.bindData(category, mFeed.width, mFeed.height, mFeed.cover, mFeed.url);

        //???????????????????????? ??????????????????????????????playerView???bottom??? ??? coordinator???bottom???
        //????????????????????????????????????????????????????????????????????????
        playerView.post(() -> {
            boolean fullscreen = playerView.getBottom() >= coordinator.getBottom();
            setViewAppearance(fullscreen);
        });

        //???headerView??? ????????????????????????????????????
        LayoutFeedDetailTypeVideoHeaderBinding headerBinding = LayoutFeedDetailTypeVideoHeaderBinding.inflate(
                LayoutInflater.from(mActivity),
                mRecyclerView,
                false);

        headerBinding.setFeed(mFeed);
        listAdapter.addHeaderView(headerBinding.getRoot());

    }

    private void setViewAppearance(boolean fullscreen) {
        mVideoBinding.setFullscreen(fullscreen);
        mInateractionBinding.setFullscreen(fullscreen);
        mVideoBinding.fullscreenAuthorInfo.getRoot().setVisibility(fullscreen ? View.VISIBLE : View.GONE);

        //???????????????????????????
        int inputHeight = mInateractionBinding.getRoot().getMeasuredHeight();
        //????????????????????????
        int ctrlViewHeight = playerView.getPlayController().getMeasuredHeight();
        //??????????????????bottom???
        int bottom = playerView.getPlayController().getBottom();
        //????????????????????????????????????????????????????????????????????????
        playerView.getPlayController().setY(fullscreen ? bottom - inputHeight - ctrlViewHeight
                : bottom - ctrlViewHeight);
        mInateractionBinding.inputView.setBackgroundResource(fullscreen ? R.drawable.bg_edit_view2 : R.drawable.bg_edit_view);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backPressed = true;
        //???????????????????????? ?????? ??????????????????????????????????????????????????? ???????????????????????????
        playerView.getPlayController().setTranslationY(0);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!backPressed) {
            playerView.inActive();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        backPressed = false;
        playerView.onActive();
    }
}
