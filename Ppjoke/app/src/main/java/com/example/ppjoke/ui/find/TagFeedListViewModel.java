package com.example.ppjoke.ui.find;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.example.libnetwork.ApiResponse;
import com.example.libnetwork.ApiService;
import com.example.ppjoke.model.Feed;
import com.example.ppjoke.ui.AbsViewModel;
import com.example.ppjoke.ui.login.UserManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TagFeedListViewModel extends AbsViewModel<Feed> {
    private String feedType;

    @Override
    public DataSource createDataSource() {
        return new DataSource();
    }

    public void setFeedType(String feedType) {
        this.feedType = feedType;
    }

    private class DataSource extends ItemKeyedDataSource<Integer, Feed> {

        @Override
        public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Feed> callback) {
            loadData(0, callback);
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            loadData(params.key, callback);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            callback.onResult(Collections.emptyList());
        }

        @NonNull
        @Override
        public Integer getKey(@NonNull Feed item) {
            return item.id;
        }
    }

    private void loadData(Integer feedId, ItemKeyedDataSource.LoadCallback<Feed> callback) {
        ApiResponse<List<Feed>> response = ApiService.get("/feeds/queryHotFeedsList")
                .addParam("userId", UserManager.get().getUserId())
                .addParam("pageCount", 10)
                .addParam("feedType", feedType)
                .addParam("feedId", feedId)
                .responseType(new TypeReference<ArrayList<Feed>>() {
                }.getType())
                .execute();

        List<Feed> result = response.body == null ? Collections.emptyList() : response.body;
        callback.onResult(result);

        if (feedId > 0) {
            //分页的情况 通知一下 UI 本次加载是否有数据,方便UI 关闭上拉加载动画什么的
            ((MutableLiveData) getBoundaryPageData()).postValue(result.size() > 0);
        }
    }
}
