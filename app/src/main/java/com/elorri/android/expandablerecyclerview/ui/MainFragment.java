package com.elorri.android.expandablerecyclerview.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.elorri.android.expandablerecyclerview.R;
import com.elorri.android.expandablerecyclerview.data.ExpandableContract;
import com.elorri.android.expandablerecyclerview.service.AppService;

public class MainFragment extends Fragment implements View.OnClickListener, LoaderManager
        .LoaderCallbacks<Cursor>, MainAdapter.Callback {
    private MainAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private Context mContext;
    private EditText mNameView;
    private int RECYCLERVIEW_LOADER_ID = 1;
    private BroadcastReceiver mAppServiceReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MainAdapter(mContext, null, this);
        mRecyclerView.setAdapter(mAdapter);

        mNameView = (EditText) view.findViewById(R.id.name);
        view.findViewById(R.id.add_directory).setOnClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(RECYCLERVIEW_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAppServiceReceiver = new AppServiceReceiver();
        IntentFilter filter = new IntentFilter(AppService.APP_SERVICE_MESSAGE_EVENT);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mAppServiceReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mAppServiceReceiver);
    }

    @Override
    public void onClick(View view) {
        String name = String.valueOf(mNameView.getText());
        switch (view.getId()) {
            case R.id.add_directory: {
                Intent intent = new Intent(getActivity(), AppService.class);
                Bundle bundle=new Bundle();
                bundle.putString(ExpandableContract.FileEntry.DIRECTORY_NAME, name);
                intent.putExtras(bundle);
                intent.setAction(AppService.ADD_FILE_REQUEST);
                getActivity().startService(intent);
                return;
            }
            case R.id.add_file: {
                return;
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = ExpandableContract.buildRootUri();
        return new CursorLoader(getActivity(),
                uri,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void addFile(String directory) {
        Intent intent = new Intent(mContext, AppService.class);
        Bundle bundle=new Bundle();
        bundle.putString(ExpandableContract.FileEntry.DIRECTORY_NAME, directory);
        bundle.putString(ExpandableContract.FileEntry.FILE_NAME, String.valueOf(mNameView.getText()));
        intent.putExtras(bundle);
        Log.e("App", Thread.currentThread().getStackTrace()[2]+"directory "+directory+" file"+mNameView.getText());
        intent.setAction(AppService.ADD_FILE_REQUEST);
        mContext.startService(intent);
    }

    @Override
    public void deleteItem(String name) {
        Intent intent = new Intent(getActivity(), AppService.class);
        intent.putExtra(ExpandableContract.FileEntry.DIRECTORY_NAME, name);
        intent.setAction(AppService.DELETE_FILE_REQUEST);
        mContext.startService(intent);
    }



    private class AppServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("Nebo", Thread.currentThread().getStackTrace()[2] + "");
            String message = intent.getStringExtra(AppService.APP_SERVICE_MESSAGE_EVENT);
            switch (message) {
                case AppService.ADD_FILE_DONE:
                case AppService.DELETE_FILE_DONE: {
                    Log.e("Nebo", Thread.currentThread().getStackTrace()[2] + "");
                    getLoaderManager().restartLoader(RECYCLERVIEW_LOADER_ID, null, MainFragment.this);
                    break;
                }
            }
        }
    }
}
