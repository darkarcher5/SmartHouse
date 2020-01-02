package com.example.smarthouse.UI.roomFragment;


import android.content.Context;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.smarthouse.BaseAplication;
import com.example.smarthouse.R;
import com.example.smarthouse.UI.BaseFragment;
import com.example.smarthouse.UI.LogInFragmentDirections;
import com.example.smarthouse.databinding.FragmentRoomBinding;
import com.example.smarthouse.viewmodels.RoomViewModel;
import com.example.smarthouse.viewmodels.ViewModelProviderFactory;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * A simple {@link Fragment} subclass.
 */
public class RoomFragment extends BaseFragment {

    @Inject
    ViewModelProviderFactory mViewModelFactory;


    FragmentRoomBinding mBinding;

    RoomViewModel mViewModel;


    public RoomFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        ((BaseAplication) getActivity().getApplication())
                .getmAuthCompoent()
                .getRoomSubcomponentFacotry()
                .create()
                .inject(this);
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        mBinding = FragmentRoomBinding.inflate(inflater,container,false);

        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(RoomViewModel.class);

        //region AuthViewModel

        Disposable authStateDisposable = mViewModel.getmAuthState()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((authState) -> {
                            switch (authState) {

                                case UNAUTHENTICATED:
                                    ((BaseAplication) getActivity().getApplication()).releseAuthComponent();
                                    Navigation.findNavController(mBinding.getRoot()).navigate(LogInFragmentDirections.actionGlobalLogInFragment());
                                    break;
                            }
                        },
                        (e) -> Log.e("AuthObserverError:" ,e.getMessage()));

        //endregion


        mViewModel.setmHouseId(RoomFragmentArgs.fromBundle(getArguments()).getHouseId());

        setUpToolbar();

        mBinding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
        mBinding.drawerLayout.openDrawer(GravityCompat.START);


        Disposable hasUserChosenRoomOnceDisposable = mViewModel.getHasUserChosenRoomOnceObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (hasChosen) -> mBinding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED),
                        (e) ->{});

        addDisposables(authStateDisposable,hasUserChosenRoomOnceDisposable);

        return mBinding.getRoot();
    }

    public void setUpToolbar() {
        ((AppCompatActivity) getActivity()).setSupportActionBar(mBinding.roomsToolbar);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(actionBar != null)
        {
            actionBar.setTitle("");
            actionBar.setDisplayHomeAsUpEnabled(true);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(getActivity(), mBinding.drawerLayout, mBinding.roomsToolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
            toggle.setDrawerIndicatorEnabled(true);
            mBinding.drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        Disposable chosenRoomidDisposable= mViewModel
                .getmChosenRoomIdObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        ((roomInfo) -> {
                            if(roomInfo != null) {
                                mBinding.drawerLayout.closeDrawers();
                            }
                        }),
                        (e) -> Log.e("ToolbarTitleError: ",e.getMessage())
                );

        Disposable choosenRoomNameDisposable = mViewModel
                .getChosenRoomNameObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (roomName) -> actionBar.setTitle(roomName),
                        (e) ->Log.e("TitleError: ", e.getMessage())
                );


        setBackNavigation();
        addDisposables(chosenRoomidDisposable,choosenRoomNameDisposable);



    }


    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null)
        {
            if(savedInstanceState.getBoolean("isDrawerOpned",true))
            {
                mBinding.drawerLayout.openDrawer(GravityCompat.START);
            }
            else
            {
                mBinding.drawerLayout.closeDrawer(GravityCompat.START);
            }
            mViewModel.setmHouseId(savedInstanceState.getString("houseId",null));
            mViewModel.setmChosenRoomId(savedInstanceState.getString("roomId",null));
        }


    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isDrawerOpned", mBinding.drawerLayout.isDrawerOpen(GravityCompat.START));
        outState.putString("houseId", mViewModel.getmHouseId());
        outState.putString("roomId", mViewModel.getmChosenRoomId());

    }

    void setBackNavigation() {

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                mViewModel.getRoomIdPositionForBackNavigation().subscribe(
                        (index) ->
                        {
                            if(index == -1)
                            {
                                Navigation.findNavController(mBinding.getRoot()).navigate(RoomFragmentDirections.actionRoomFragmentToHousesListFragmnet());
                            }
                            else
                            {
                                mViewModel.setUpRoomIdFromBackNavigation(index);
                            }
                        }

                );
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(this,callback);

    }


}


