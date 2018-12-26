package com.djdenpa.quickcalendar.utils;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class QuickCalendarExecutors {

  private static final Object instantiationLock = new Object();
  private static QuickCalendarExecutors sInstance;
  private final Executor diskIO;
  private final Executor networkIO;
  private final Executor mainThread;

  private QuickCalendarExecutors(Executor pDiskIO, Executor pNetworkIO, Executor pMainThread){
    diskIO = pDiskIO;
    networkIO = pNetworkIO;
    mainThread = pMainThread;
  }

  public static QuickCalendarExecutors getInstance() {
    if (sInstance == null) {
      synchronized(instantiationLock) {
        if (sInstance == null) {
          sInstance = new QuickCalendarExecutors(
                  Executors.newSingleThreadExecutor(),
                  Executors.newFixedThreadPool(3),
                  new MainThreadExecutor()
          );

        }
      }
    }
    return sInstance;
  }

  public Executor diskIO() {
    return diskIO;
  }
  public Executor networkIO() {
    return networkIO;
  }
  public Executor mainThread() {
    return mainThread;
  }

  private static class MainThreadExecutor implements Executor {
    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    @Override
    public void execute(@NonNull Runnable command) {
      mainThreadHandler.post(command);
    }
  }

}
