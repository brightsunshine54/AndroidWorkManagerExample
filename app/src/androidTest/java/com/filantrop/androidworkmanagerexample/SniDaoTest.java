package com.filantrop.androidworkmanagerexample;

import static org.assertj.core.api.Assertions.assertThat;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.filantrop.androidworkmanagerexample.sni.SniDao;
import com.filantrop.androidworkmanagerexample.sni.SniDatabase;
import com.filantrop.androidworkmanagerexample.sni.SniDto;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class SniDaoTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private SniDatabase db;
    private SniDao sniDao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, SniDatabase.class).build();
        sniDao = db.sniDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void insertAndGetAllSni() throws InterruptedException {
        SniDto sni1 = new SniDto("sni1");
        SniDto sni2 = new SniDto("sni2");
        List<SniDto> snis = Arrays.asList(sni1, sni2);

        sniDao.insertAll(snis);

        LiveData<List<SniDto>> allSni = sniDao.getAll();
        List<SniDto> observedSnis = getObservedValue(allSni);

        assertThat(observedSnis).hasSize(2);
        assertThat(observedSnis.get(0).getSni()).isEqualTo("sni1");
        assertThat(observedSnis.get(1).getSni()).isEqualTo("sni2");
    }

    @Test
    public void insertDuplicateSni() throws InterruptedException {
        SniDto sni1 = new SniDto("sni1");
        SniDto sni2 = new SniDto("sni1"); // Duplicate
        List<SniDto> snis = Arrays.asList(sni1, sni2);

        sniDao.insertAll(snis);

        LiveData<List<SniDto>> allSni = sniDao.getAll();
        List<SniDto> observedSnis = getObservedValue(allSni);

        assertThat(observedSnis).hasSize(1);
        assertThat(observedSnis.get(0).getSni()).isEqualTo("sni1");
    }

    @Test
    public void deleteAll() throws InterruptedException {
        SniDto sni1 = new SniDto("sni1");
        List<SniDto> snis = Arrays.asList(sni1);

        sniDao.insertAll(snis);
        sniDao.deleteAll();

        int count = sniDao.getTotalCount();
        assertThat(count).isEqualTo(0);
    }

    @Test
    public void getSniCount() {
        SniDto sni1 = new SniDto("sni1");
        SniDto sni2 = new SniDto("sni2");
        List<SniDto> snis = Arrays.asList(sni1, sni2);

        sniDao.insertAll(snis);

        int count = sniDao.getTotalCount();

        assertThat(count).isEqualTo(2);

        sniDao.deleteAll();

        count = sniDao.getTotalCount();
        assertThat(count).isEqualTo(0);
    }

    private <T> T getObservedValue(final LiveData<T> liveData) throws InterruptedException {
        final Object[] data = new Object[1];
        final CountDownLatch latch = new CountDownLatch(1);
        Observer<T> observer = new Observer<T>() {
            @Override
            public void onChanged(T o) {
                data[0] = o;
                latch.countDown();
                liveData.removeObserver(this);
            }
        };
        liveData.observeForever(observer);
        latch.await(2, TimeUnit.SECONDS);
        //noinspection unchecked
        return (T) data[0];
    }
}
