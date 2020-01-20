# SharedPreference

## getSharedPreferences

通过 Context 的 getSharedPreferences 方法获取 Sp 实例，其实现在 ContextImpl 中，方法代码如下：

```text
@Override
public SharedPreferences getSharedPreferences(String name, int mode) {
    // 处理 name 为 null 的情况
    // At least one application in the world actually passes in a null
    // name.  This happened to work because when we generated the file name
    // we would stringify it to "null.xml".  Nice.
    if (mPackageInfo.getApplicationInfo().targetSdkVersion <
            Build.VERSION_CODES.KITKAT) {
        if (name == null) {
            name = "null";
        }
    }

    File file;
    synchronized (ContextImpl.class) {
        if (mSharedPrefsPaths == null) {
            //创建缓存的 map
            mSharedPrefsPaths = new ArrayMap<>();
        }
        //先从缓存中获取 File 
        file = mSharedPrefsPaths.get(name);
        if (file == null) {
            //缓存中没有，先生成file
            file = getSharedPreferencesPath(name);
            //加入缓存
            mSharedPrefsPaths.put(name, file);
        }
    }
    //通过文件生成 SharedPreferences
    return getSharedPreferences(file, mode);
}
```

生成 file 时调用了 `getSharedPreferencesPath`

```text
@Override
public File getSharedPreferencesPath(String name) {
    return makeFilename(getPreferencesDir(), name + ".xml");
}
```

获取sp目录，也就是 data 下的 "shared\_prefs"目录下。



```text
private File getPreferencesDir() {
    synchronized (mSync) {
        if (mPreferencesDir == null) {
            //应用的data 目录下 shared_prefs 文件夹
            mPreferencesDir = new File(getDataDir(), "shared_prefs");
        }
        return ensurePrivateDirExists(mPreferencesDir);
    }
}
```

makeFilename 方法如下：

```text
private File makeFilename(File base, String name) {
    //文件名包含分隔符时抛出异常
    if (name.indexOf(File.separatorChar) < 0) {
        final File res = new File(base, name);
           return res;
    }
    throw new IllegalArgumentException(
            "File " + name + " contains a path separator");
}
```

以上是生成 file 对象的过程。

## 创建 SharedPreferences

创建 Sp 实例时调用了getSharedPreferences\(file, mode\):

```text
@Override
public SharedPreferences getSharedPreferences(File file, int mode) {
    SharedPreferencesImpl sp;
    synchronized (ContextImpl.class) {
        final ArrayMap<File, SharedPreferencesImpl> cache = getSharedPreferencesCacheLocked();
        // 先从缓存中获取
        sp = cache.get(file);
        if (sp == null) {
            //一些权限检查
            checkMode(mode);
            if (getApplicationInfo().targetSdkVersion >= android.os.Build.VERSION_CODES.O) {
                if (isCredentialProtectedStorage()
                        && !getSystemService(UserManager.class)
                                .isUserUnlockingOrUnlocked(UserHandle.myUserId())) {
                    throw new IllegalStateException("SharedPreferences in credential encrypted "
                            + "storage are not available until after user is unlocked");
                }
            }
            //创建 SharedPreferences 实例
            sp = new SharedPreferencesImpl(file, mode);
            // 放入缓存
            cache.put(file, sp);
            return sp;
        }
    }
    if ((mode & Context.MODE_MULTI_PROCESS) != 0 ||
        getApplicationInfo().targetSdkVersion < android.os.Build.VERSION_CODES.HONEYCOMB) {
        // If somebody else (some other process) changed the prefs
        // file behind our back, we reload it.  This has been the
        // historical (if undocumented) behavior.
        sp.startReloadIfChangedUnexpectedly();
    }
    return sp;
}
```

创建前先进行了一些权限检查，checkMode 用于检查 sp 的模式，MODE\_WORLD\_READABLE 和 MODE\_WORLD\_WRITEABLE 在7.0及以后会抛出异常。

```text
private void checkMode(int mode) {
    if (getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.N) {
        if ((mode & MODE_WORLD_READABLE) != 0) {
            throw new SecurityException("MODE_WORLD_READABLE no longer supported");
        }
        if ((mode & MODE_WORLD_WRITEABLE) != 0) {
            throw new SecurityException("MODE_WORLD_WRITEABLE no longer supported");
        }
    }
}
```

创建 sp SharePreferences 的实现类是`SharedPreferencesImpl`构造方法的源码如下：

```text
SharedPreferencesImpl(File file, int mode) {
        mFile = file;
        mBackupFile = makeBackupFile(file);
        mMode = mode;
        mLoaded = false;
        mMap = null;
        mThrowable = null;
        //从文件中读取
        startLoadFromDisk();
}
```

在构造方法中，调用了 startLoadFromDisk 方法，在该方法中，先将读取状态标识置为false，然后开启了一个线程进行读取任务：

```text
private void startLoadFromDisk() {
    synchronized (mLock) {
        mLoaded = false;
    }
    //开启一个线程读取文件
    new Thread("SharedPreferencesImpl-load") {
        public void run() {
            loadFromDisk();
        }
    }.start();
}
```

读取任务在 loadFromDisk 方法中，代码为：

```text
private void loadFromDisk() {
    synchronized (mLock) {
        //已经读取成功，直接返回
        if (mLoaded) {
            return;
        }
        if (mBackupFile.exists()) {
            mFile.delete();
            mBackupFile.renameTo(mFile);
        }
    }

    Map<String, Object> map = null;
    StructStat stat = null;
    Throwable thrown = null;
    try {
        stat = Os.stat(mFile.getPath());
        if (mFile.canRead()) {
            BufferedInputStream str = null;
            try {
                //创建文件输入流
                str = new BufferedInputStream(
                        new FileInputStream(mFile), 16 * 1024);
                //将读取的文件解析成Map        
                map = (Map<String, Object>) XmlUtils.readMapXml(str);
            } catch (Exception e) {
                Log.w(TAG, "Cannot read " + mFile.getAbsolutePath(), e);
            } finally {
                IoUtils.closeQuietly(str);
            }
        }
    } catch (ErrnoException e) {
        // An errno exception means the stat failed. Treat as empty/non-existing by
        // ignoring.
    } catch (Throwable t) {
        thrown = t;
    }

    synchronized (mLock) {
        //读取完成后设置读取状态为true
        mLoaded = true;
        mThrowable = thrown;

        // It's important that we always signal waiters, even if we'll make
        // them fail with an exception. The try-finally is pretty wide, but
        // better safe than sorry.
        try {
            if (thrown == null) {
                if (map != null) {
                    mMap = map;
                    mStatTimestamp = stat.st_mtim;
                    mStatSize = stat.st_size;
                } else {
                    mMap = new HashMap<>();
                }
            }
            // In case of a thrown exception, we retain the old map. That allows
            // any open editors to commit and store updates.
        } catch (Throwable t) {
            mThrowable = t;
        } finally {
            //通知所有可能正在等待的线程
            mLock.notifyAll();
        }
    }
}
```

主要逻辑是读取XML文件并将其解析为 Map 对象，然后将读取标识置为 true。sp创建后，就可以进行读取和写入了。

## 读取值

getString 在 SharedPreferencesImpl 中方法代码为：

```text
public String getString(String key, @Nullable String defValue) {
    synchronized (mLock) {
        awaitLoadedLocked();
        String v = (String)mMap.get(key);
        return v != null ? v : defValue;
    }
}
```

在读取前，先调用了 awaitLoadedLocked 方法在文件还没有准备好时进行等待：

```text
private final Object mLock = new Object();

private void awaitLoadedLocked() {
    while (!mLoaded) {
        try {
            //没有读取成功，先等待
            mLock.wait();
        } catch (InterruptedException unused) {
        }
    }
    if (mThrowable != null) {
        throw new IllegalStateException(mThrowable);
    }
}
```

而上面 loadFromDisk 读取成功后会调用 mLock.notifyAll 方法，从而读取操作可以继续，也就是从 map 中根据key获取对应的值。

## 写入值

在写入时，需要先调用 edit 方法，获取一个 Editor 对象。

### 创建 Editor

edit 方法源码如下：

```text
@Override
public Editor edit() {
    synchronized (mLock) {
        awaitLoadedLocked();
    }
    return new EditorImpl();
}
```

当 sp 可用时，创建了一个 `EditorImpl` 对象并返回。`EditorImpl` 是`Editor`的实现类，同时是`SharedPreferencesImpl`的内部类。

EditorImpl 只有三个属性：

```text
public final class EditorImpl implements Editor {
    //写锁
    private final Object mEditorLock = new Object();

    @GuardedBy("mEditorLock")
    private final Map<String, Object> mModified = new HashMap<>();

    @GuardedBy("mEditorLock")
    private boolean mClear = false;
    
    //....
}
```

### putXxx

执行修改主要是一些重载的put方法，还有remove方法用于移除一个key，clear 方法用于清空修改。

EditorImpl 中的对应的修改方法代码如下，put 方法以 putString\(\) 为例：

```text
@Override
public Editor putString(String key, @Nullable String value) {
    synchronized (mEditorLock) {
        mModified.put(key, value);
        return this;
    }
}

@Override
public Editor remove(String key) {
    synchronized (mEditorLock) {
        mModified.put(key, this);
        return this;
    }
}

@Override
public Editor clear() {
    synchronized (mEditorLock) {
        mClear = true;
        return this;
    }
}
```

put remove 操作的是 EditorImpl 的 mModified，而 clear 方法只是将清除标记置为 true。另外这些方法都返回 Editor 对象，方便链式调用。

要使这些更改生效，就需要调用 apply 或者 commit 进行提交，先来看下 apply。

### EditorImpl\#apply\(\)

apply 的代码如下，略去了一些 log 信息：

```text
@Override
public void apply() {
    //先提交到内存
    final MemoryCommitResult mcr = commitToMemory();
    
    final Runnable awaitCommit = new Runnable() {
            @Override
            public void run() {
                try {
                    mcr.writtenToDiskLatch.await();
                } catch (InterruptedException ignored) {
                
                }
           }
    };

    QueuedWork.addFinisher(awaitCommit);

    Runnable postWriteRunnable = new Runnable() {
            @Override
            public void run() {
                awaitCommit.run();
                QueuedWork.removeFinisher(awaitCommit);
            }
        };

    SharedPreferencesImpl.this.enqueueDiskWrite(mcr, postWriteRunnable);

    // 写入内存后就可以通知监听者
    notifyListeners(mcr);
}
```

apply 逻辑是：

1. 通过 commitToMemory 方法把修改提交至内存中
2. 通过 enqueueDiskWrite 执行磁盘写入

#### EditorImpl\#commitToMemory\(\)

这个方法用于将在 Editor 上执行所有的 put 更改提交到内存的Sp中，并返回一个 MemoryCommitResult 对象提交信息，代码如下：

```text
private MemoryCommitResult commitToMemory() {
    // 当前内存中sp的版本号
    long memoryStateGeneration;
    
    //所有修改的key，用于通知监听者
    List<String> keysModified = null;
    
    Set<OnSharedPreferenceChangeListener> listeners = null;
    
    //要写入磁盘的map
    Map<String, Object> mapToWriteToDisk;

    synchronized (SharedPreferencesImpl.this.mLock) {
        //当前有正在写入的任务在执行
        if (mDiskWritesInFlight > 0) {
            // 此时有写入任务正在执行，所以不能直接修改 mMap，而是克隆它
            mMap = new HashMap<String, Object>(mMap);
        }
        //mapToWriteToDisk 为所有key value 对
        mapToWriteToDisk = mMap;
        mDiskWritesInFlight++;

        boolean hasListeners = mListeners.size() > 0;
        if (hasListeners) {
            //如果有监听者，则需要记录发生更改的 key
            keysModified = new ArrayList<String>();
            listeners = new HashSet<OnSharedPreferenceChangeListener>(mListeners.keySet());
        }

        synchronized (mEditorLock) {
            boolean changesMade = false;

            if (mClear) {
                //如果调用过 clear 先执行清空操作
                if (!mapToWriteToDisk.isEmpty()) {
                    changesMade = true;
                    mapToWriteToDisk.clear();
                }
                mClear = false;
            }

            for (Map.Entry<String, Object> e : mModified.entrySet()) {
                String k = e.getKey();
                Object v = e.getValue();
                //在 remove 方法中，移除一个key时，将它的值设为 EditorImpl.this
                // 所以如果 v==this,就代表移除一个key
                if (v == this || v == null) {
                    if (!mapToWriteToDisk.containsKey(k)) {
                        continue;
                    }
                    mapToWriteToDisk.remove(k);
                } else {
                    if (mapToWriteToDisk.containsKey(k)) {
                        Object existingValue = mapToWriteToDisk.get(k);
                        if (existingValue != null && existingValue.equals(v)) {
                            //新值与旧值相等，跳过
                            continue;
                        }
                    }
                    //将新增加的key和value放到即将写入的map中
                    mapToWriteToDisk.put(k, v);
                }

                changesMade = true;
                if (hasListeners) {
                    keysModified.add(k);
                }
            }

            mModified.clear();

            if (changesMade) {
                //版本自增
                mCurrentMemoryStateGeneration++;
            }
            memoryStateGeneration = mCurrentMemoryStateGeneration;
        }
    }
    return new MemoryCommitResult(memoryStateGeneration, keysModified, listeners,
                    mapToWriteToDisk);
}
```

该方法的逻辑比较容易理解，根据 Editor 的 mModified 中修改的key，来更新内存中 map 的对应的key，并记录发生修改的key，以便通知监听者。

方法返回的是一个 `MemoryCommitResult` 对象，`MemoryCommitResult` 是 SharedPreferencesImpl 的静态内部类：

```text
// Return value from EditorImpl#commitToMemory()
private static class MemoryCommitResult {
    //标识当前内存的版本
    final long memoryStateGeneration;
    //发生修改的key集合
    @Nullable final List<String> keysModified;
    
    @Nullable final Set<OnSharedPreferenceChangeListener> listeners;
    //要写入文件的map
    final Map<String, Object> mapToWriteToDisk;
    //lock
    final CountDownLatch writtenToDiskLatch = new CountDownLatch(1);

    @GuardedBy("mWritingToDiskLock")
    volatile boolean writeToDiskResult = false;
    
    boolean wasWritten = false;

    private MemoryCommitResult(long memoryStateGeneration, @Nullable List<String> keysModified,
            @Nullable Set<OnSharedPreferenceChangeListener> listeners,
            Map<String, Object> mapToWriteToDisk) {
        this.memoryStateGeneration = memoryStateGeneration;
        this.keysModified = keysModified;
        this.listeners = listeners;
        this.mapToWriteToDisk = mapToWriteToDisk;
    }

    void setDiskWriteResult(boolean wasWritten, boolean result) {
        this.wasWritten = wasWritten;
        writeToDiskResult = result;
        writtenToDiskLatch.countDown();
    }
}
```

将变更提交的内存后，调用了 enqueDiskWrite 进行文件写入。

#### SharedPreferencesImpl\#enqueDiskWrite

```text
private void enqueueDiskWrite(final MemoryCommitResult mcr,
                              final Runnable postWriteRunnable) {
    //是否是 commit                          
    final boolean isFromSyncCommit = (postWriteRunnable == null);

    final Runnable writeToDiskRunnable = new Runnable() {
            @Override
            public void run() {
                synchronized (mWritingToDiskLock) {
                    //写入文件
                    writeToFile(mcr, isFromSyncCommit);
                }
                synchronized (mLock) {
                    mDiskWritesInFlight--;
                }
                if (postWriteRunnable != null) {
                    //执行写入后的任务
                    postWriteRunnable.run();
                }
            }
        };

    // Typical #commit() path with fewer allocations, doing a write on
    // the current thread.
    if (isFromSyncCommit) {
        boolean wasEmpty = false;
        synchronized (mLock) {
            // mDiskWritesInFlight 在提交至内存时会自增
            // 为 1 说明此是没有其他的任务要写入
            wasEmpty = mDiskWritesInFlight == 1;
        }
        if (wasEmpty) {
            //在主线程中直接执行写入
            writeToDiskRunnable.run();
            return;
        }
    }

    QueuedWork.queue(writeToDiskRunnable, !isFromSyncCommit);
}
```

方法中第二个参数是一个 Runnable ，用于在文件写入后执行。如果这个参数为null，那么此次调用来自 commit\(\) 方法，否则此次调用就来自 apply 方法，在上面的 apply 方法中，我们也看到它传了一个 Runnable。

上面方法中的 writeToDiskRunnable 定义了文件写入的过程：先调用 writeToFile 将内存的SP写入文件，然后将 mDiskWritesInFlight 减一，最后在执行 postWriteRunnable。

不过 writeToDiskRunnable  的执行时机却暗藏玄机，如果此调用来自 commit\(\) 方法，并且当前只有这次写入需要执行，那么就会在主线程执行这次文件写入；其他情况都会通过 QueuedWork 把写入任务加入队列中，稍后再写入。关于 QueuedWork 稍后再介绍，先来看看 writeToFile 方法。

### SharedPreferencesImpl\#writeToFile\(\)

writeToFile 是执行文件写入的方法，代码如下，我省略的一些日志输出代码：

```text
@GuardedBy("mWritingToDiskLock")
private void writeToFile(MemoryCommitResult mcr, boolean isFromSyncCommit) {
    // 一些时间点，主要用于日志输出
    long startTime = 0;
    long existsTime = 0;
    long backupExistsTime = 0;
    long outputStreamCreateTime = 0;
    long writeTime = 0;
    long fsyncTime = 0;
    long setPermTime = 0;
    long fstatTime = 0;
    long deleteTime = 0;

    boolean fileExists = mFile.exists();

    // Rename the current file so it may be used as a backup during the next read
    if (fileExists) {
        boolean needsWrite = false;

        // 只有文件版本小于内存版本时才写入
        if (mDiskStateGeneration < mcr.memoryStateGeneration) {
            if (isFromSyncCommit) {
                needsWrite = true;
            } else {
                synchronized (mLock) {
                    //对于 apply，没有必要每次都写入，而是只执行最后一次的提交对应的写入
                    if (mCurrentMemoryStateGeneration == mcr.memoryStateGeneration) {
                        needsWrite = true;
                    }
                }
            }
        }

        if (!needsWrite) {
            //没必要写入文件，直接返回
            mcr.setDiskWriteResult(false, true);
            return;
        }

        boolean backupFileExists = mBackupFile.exists();

        if (!backupFileExists) {
            //备份文件不存在，将 正式文件命名为备份文件
            if (!mFile.renameTo(mBackupFile)) {
                mcr.setDiskWriteResult(false, false);
                return;
            }
        } else {
            //备份文件存在，删除正式文件
            mFile.delete();
        }
    }

    // Attempt to write the file, delete the backup and return true as atomically as
    // possible.  If any exception occurs, delete the new file; next time we will restore
    // from the backup.
    try {
        FileOutputStream str = createFileOutputStream(mFile);

        if (str == null) {
            mcr.setDiskWriteResult(false, false);
            return;
        }
        //写入数据
        XmlUtils.writeMapXml(mcr.mapToWriteToDisk, str);

        writeTime = System.currentTimeMillis();

        FileUtils.sync(str);

        fsyncTime = System.currentTimeMillis();

        str.close();
        ContextImpl.setFilePermissionsFromMode(mFile.getPath(), mMode, 0);

        // Writing was successful, delete the backup file if there is one.
        mBackupFile.delete();

        //更新文件版本号
        mDiskStateGeneration = mcr.memoryStateGeneration;

        mcr.setDiskWriteResult(true, true);

        long fsyncDuration = fsyncTime - writeTime;
        mSyncTimes.add((int) fsyncDuration);
        mNumSync++;

        return;
    } catch (XmlPullParserException e) {
        Log.w(TAG, "writeToFile: Got exception:", e);
    } catch (IOException e) {
        Log.w(TAG, "writeToFile: Got exception:", e);
    }

    // Clean up an unsuccessfully written file
    if (mFile.exists()) {
        if (!mFile.delete()) {
            Log.e(TAG, "Couldn't clean up partially-written file " + mFile);
        }
    }
    mcr.setDiskWriteResult(false, false);
}
```





对于 apply 的优化，多次提交只有最后一次会执行写入。

[QueuedWork](https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/android/app/QueuedWork.java)

```text

private static final LinkedList<Runnable> sFinishers = new LinkedList<>();
private static final LinkedList<Runnable> sWork = new LinkedList<>();
private static boolean sCanDelay = true;

/**
     * Queue a work-runnable for processing asynchronously.
     *
     * @param work The new runnable to process
     * @param shouldDelay If the message should be delayed
     */
    @UnsupportedAppUsage
    public static void queue(Runnable work, boolean shouldDelay) {
        Handler handler = getHandler();

        synchronized (sLock) {
            sWork.add(work);

            if (shouldDelay && sCanDelay) {
                handler.sendEmptyMessageDelayed(QueuedWorkHandler.MSG_RUN, DELAY);
            } else {
                handler.sendEmptyMessage(QueuedWorkHandler.MSG_RUN);
            }
        }
    }
    
    
//创建新的 HanlderThread
private static Handler getHandler() {
        synchronized (sLock) {
            if (sHandler == null) {
                HandlerThread handlerThread = new HandlerThread("queued-work-looper",
                        Process.THREAD_PRIORITY_FOREGROUND);
                handlerThread.start();

                sHandler = new QueuedWorkHandler(handlerThread.getLooper());
            }
            return sHandler;
        }
    }
    
public static void queue(Runnable work, boolean shouldDelay) {
        Handler handler = getHandler();

        synchronized (sLock) {
            sWork.add(work);

            if (shouldDelay && sCanDelay) {
                handler.sendEmptyMessageDelayed(QueuedWorkHandler.MSG_RUN, DELAY);
            } else {
                handler.sendEmptyMessage(QueuedWorkHandler.MSG_RUN);
            }
        }
    }
    
    
    
@UnsupportedAppUsage
    public static void addFinisher(Runnable finisher) {
        synchronized (sLock) {
            sFinishers.add(finisher);
        }
    }
    
    
    
    private static class QueuedWorkHandler extends Handler {
        static final int MSG_RUN = 1;

        QueuedWorkHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what == MSG_RUN) {
                processPendingWork();
            }
        }
    }
    
    
    private static void processPendingWork() {
        long startTime = 0;

        if (DEBUG) {
            startTime = System.currentTimeMillis();
        }

        synchronized (sProcessingWork) {
            LinkedList<Runnable> work;

            synchronized (sLock) {
                work = (LinkedList<Runnable>) sWork.clone();
                sWork.clear();

                // Remove all msg-s as all work will be processed now
                getHandler().removeMessages(QueuedWorkHandler.MSG_RUN);
            }

            if (work.size() > 0) {
                for (Runnable w : work) {
                    w.run();
                }

                if (DEBUG) {
                    Log.d(LOG_TAG, "processing " + work.size() + " items took " +
                            +(System.currentTimeMillis() - startTime) + " ms");
                }
            }
        }
    }
```

[XMLUtils](https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/com/android/internal/util/XmlUtils.java;l=50)

FileUtils

## commit



```text
public boolean commit() {
            long startTime = 0;

            if (DEBUG) {
                startTime = System.currentTimeMillis();
            }

            MemoryCommitResult mcr = commitToMemory();

            SharedPreferencesImpl.this.enqueueDiskWrite(
                mcr, null /* sync write on this thread okay */);
            try {
                mcr.writtenToDiskLatch.await();
            } catch (InterruptedException e) {
                return false;
            } finally {
                if (DEBUG) {
                    Log.d(TAG, mFile.getName() + ":" + mcr.memoryStateGeneration
                            + " committed after " + (System.currentTimeMillis() - startTime)
                            + " ms");
                }
            }
            notifyListeners(mcr);
            return mcr.writeToDiskResult;
        }
```



QueuedWork 的 waitToFinish 会在 Activity onPause onStop stopService 中执行。见 ActivityThread

[SharedPreferences灵魂拷问之原理](https://juejin.im/post/5df7af66e51d4557f17fb4f7)



commit 和 apply 区别

