package com.filantrop.androidworkmanagerexample.sni;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;


import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public interface SniDao {

    @Query("SELECT * FROM sni_table")
    LiveData<List<SniDto>> getAll();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<SniDto> sniList);

    @Query("DELETE FROM sni_table")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM sni_table")
    int getTotalCount();

    @Query("SELECT COUNT(*) FROM sni_table where checked = true")
    int getCheckedCount();

    @Query("SELECT * FROM sni_table WHERE checked = false ORDER BY id LIMIT 1")
    SniDto getNextUnchecked();

    @Query("SELECT COUNT(*) FROM sni_table")
    LiveData<Integer> getSniCountLiveDate();

    @Query("UPDATE sni_table set checked = true where id = :id")
    void setChecked(int id);

    @Query("UPDATE sni_table set checked = false")
    void resetChecked();

    @Query("UPDATE sni_table set checked = false")
    ListenableFuture<Void> resetCheckedListenable();

}
