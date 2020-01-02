package com.example.smarthouse.viewmodels;

import android.graphics.Bitmap;

import androidx.databinding.ObservableField;

import com.example.smarthouse.repositorys.Repository;
import com.example.smarthouse.repositorys.SharedPreferencesRepository;
import com.example.smarthouse.data.UsersHouseInfo;
import com.example.smarthouse.utils.MakeObservable;
import com.example.smarthouse.utils.PictureResize;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;


public class HousesListViewModel extends AuthViewModel {

    Repository mRepository;

    ObservableField<String> mUserQuery;
    Observable<String> mUserQueryObservable;


    @Inject
    HousesListViewModel(Repository repository, SharedPreferencesRepository shearedPrefrencesRepository) {
        super(repository, shearedPrefrencesRepository);
        this.mRepository = repository;
        mUserQuery = new ObservableField<>("");
        mUserQueryObservable = MakeObservable.makeObservebleForString(mUserQuery);
    }



    public Observable<List<UsersHouseInfo>> getUsersHousesObservable()
    {

        Observable<List<UsersHouseInfo>> newUsersHausesObservable = getmUsernameObservable()
                .switchMap((username) -> {
                    return mRepository.getUserHouses(username).toObservable();
                });

        return Observable
                .combineLatest(newUsersHausesObservable, mUserQueryObservable, (userHouseList, queryString) -> {
                    List<UsersHouseInfo> newList = new ArrayList<>();
                    if(queryString != null && !queryString.equals("")) {
                        for (UsersHouseInfo usersHauses : userHouseList) {
                            if (usersHauses.getName().toLowerCase().contains(queryString.toLowerCase())) {
                                newList.add(usersHauses);
                            }
                        }
                        return newList;
                    }
                    else
                    {
                        return userHouseList;
                    }
                });
    }


    public String getUserQuery() {
        return mUserQuery.get();
    }

    public void setUserQuery(String mUserQuery) {
        this.mUserQuery.set(mUserQuery);
    }


    public Single<String> saveBitmapToDatabase(Bitmap bitmap, String houseId)
    {
        Bitmap resizedBitmap = PictureResize.getResizedBitmap(bitmap, 800, 533);
        return mRepository.changeHousePicture(resizedBitmap, mCurrentUsername,houseId);
    }


    public Completable changeHouseName(String newName, String houseId)
    {
       return mRepository.changeHouseName(newName, mCurrentUsername,houseId);
    }



}
