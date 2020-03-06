---
description: 源码解析
---

# SharedPreferences

{% hint style="info" %}
公众号已发表文章：

[SharedPreferences 源码解析\(上） ](https://mp.weixin.qq.com/s/RNWcE9TZl2cfEcGjS-GQAw)

[SharedPreferences 源码解析\(下）](https://mp.weixin.qq.com/s/AE7QcqYiwmrYCMiOJMSsBA)
{% endhint %}

## getSharedPreferences

通过 Context 的 getSharedPreferences 方法获取 Sp 实例，其具体的实现在 ContextImpl 类中，方法代码如下：

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

逻辑就是根据名称找到或者生成对应的文件，生成文件时调用了 `getSharedPreferencesPath`，该方法代码如下：

```text
@Override
public File getSharedPreferencesPath(String name) {
    return makeFilename(getPreferencesDir(), name + ".xml");
}
```

调用了makeFileName方法，第一个参数是文件目录，通过getPreferencesDir方法获取；第二个是文件名，通过这里可以看出存储sp使用的xml文件。

getPreferencesDir 代码如下，其实就是获取 data 下的 "shared\_prefs"目录：

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

ensurePrivateDirExists 方法主要目的是在目录不存在时进行创建，源码如下，细节就不再分析了。

```text
private static File ensurePrivateDirExists(File file) {
    return ensurePrivateDirExists(file, 0771, -1, null);
}


private static File ensurePrivateDirExists(File file, int mode, int gid, String xattr) {
    if (!file.exists()) {
        final String path = file.getAbsolutePath();
        try {
            Os.mkdir(path, mode);
            Os.chmod(path, mode);
            if (gid != -1) {
                Os.chown(path, -1, gid);
            }
        } catch (ErrnoException e) {
            if (e.errno == OsConstants.EEXIST) {
                // We must have raced with someone; that's okay
            } else {
                Log.w(TAG, "Failed to ensure " + file + ": " + e.getMessage());
            }
        }

        if (xattr != null) {
            try {
                final StructStat stat = Os.stat(file.getAbsolutePath());
                final byte[] value = new byte[8];
                Memory.pokeLong(value, 0, stat.st_ino, ByteOrder.nativeOrder());
                Os.setxattr(file.getParentFile().getAbsolutePath(), xattr, value, 0);
            } catch (ErrnoException e) {
                Log.w(TAG, "Failed to update " + xattr + ": " + e.getMessage());
            }
        }
    }
    return file;
}
```

makeFilename 方法实现如下：

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

可以看出主要逻辑就是生成 File 对象。

以上是生成 file 对象的过程。

## 创建 SharedPreferences

有了文件后，就可以创建 Sp 实例了，这个过程通过 getSharedPreferences\(file, mode\)方法完成，该方法源码如下:

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

方法会先存缓存中获取，如果缓存没有，那么会执行创建，并把新创建的sp实例放入缓存。

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

创建 sp 时直接实例化了`SharedPreferencesImpl，`它是 SharePreferences 的实现类，构造方法的源码如下：

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

在构造方法中，调用了 startLoadFromDisk 方法，从名字可以看出来，这个方法的任务主要是从文件中读取内容，其源码如下：

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

在该方法中，先将读取状态标识置为false，然后开启了一个线程执行文件读取任务，文件读取任务由 loadFromDisk 方法定义，代码为：

```text
private void loadFromDisk() {
    synchronized (mLock) {
        //已经读取成功，直接返回
        if (mLoaded) {
            return;
        }
        //如果有备份文件，优先从备份文件恢复
        if (mBackupFile.exists()) {
            mFile.delete();
            mBackupFile.renameTo(mFile);
        }
    }

    Map<String, Object> map = null;
    Throwable thrown = null;
    try {
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
                    //将 mMap 赋值
                    mMap = map;
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

以 `getString` 方法为例，在 `SharedPreferencesImpl`中代码：

```text
public String getString(String key, @Nullable String defValue) {
    synchronized (mLock) {
        awaitLoadedLocked();
        String v = (String)mMap.get(key);
        return v != null ? v : defValue;
    }
}
```

在读取前，先调用了 awaitLoadedLocked 方法，在还没有完成文件读取时进行等待：

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

而前面 loadFromDisk 方法在读取成功后会调用 mLock.notifyAll 方法，从而通知读取操作可以继续。

值的读取就是从生成的 HashMap 中根据 key 获取对应的值。

## 写入值

相比读取，写入过程就复杂很多了。在写入时，需要先调用 `edit` 方法，获取一个 `Editor` 对象。

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

当 Sp 可用时，创建了一个 `EditorImpl` 对象并返回。`EditorImpl` 是`Editor`的实现类，同时是`SharedPreferencesImpl`的内部类。

`EditorImpl` 只有三个属性：

```text
public final class EditorImpl implements Editor {
    //写锁
    private final Object mEditorLock = new Object();
    //新增或者发生变更的键值对
    @GuardedBy("mEditorLock")
    private final Map<String, Object> mModified = new HashMap<>();
    //是否需要清空之前的值
    @GuardedBy("mEditorLock")
    private boolean mClear = false;
    
    //....
}
```

执行修改主要是一些重载的 put 方法，remove 方法用于移除一个键值对，clear 方法用于清空修改。

### putXxx

EditorImpl 中的对应的修改方法代码如下\(put 方法以 putString 为例\)：

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

put 和 remove 操作的是 EditorImpl 的 mModified，而 clear 方法只是将清除标记置为 true。

另外这些方法都返回 Editor 对象，方便链式调用。

通过对 EditorImpl 操作后，所有要新增或发生修改的键值对都被记录在了 mModified 这个 map 中，而要使这些更改生效，就需要调用 `apply` 或者 `commit` 进行提交。

先来看下 apply 方法。

### EditorImpl\#apply\(\)

apply 方法源码如下，略去了一些 log 信息：

```text
@Override
public void apply() {
    //先提交到内存
    final MemoryCommitResult mcr = commitToMemory();
    
    final Runnable awaitCommit = new Runnable() {
            @Override
            public void run() {
                try {
                    //等待,确保写入被执行
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

    // 写入内存后就可以通知监听者了
    notifyListeners(mcr);
}
```

apply 逻辑是：

1. 通过 `commitToMemory` 方法把修改提交至内存中
2. 通过 `enqueueDiskWrite` 将磁盘写入任务提交至任务队列
3. 通知监听者

> awaitCommit 可能在文件写入时被执行，也可能会在 QueuedWork 执行完所有任务后再执行。如果通过文件写入过程执行，会从QueuedWork的finisher中把它移除。

### EditorImpl\#commitToMemory\(\)

这个方法用于将在 Editor 上执行所有更改提交到内存的Sp中，并返回一个 MemoryCommitResult 对象提交信息，代码如下：

```text
private MemoryCommitResult commitToMemory() {
    // 当前内存中sp的版本号
    long memoryStateGeneration;
    
    //所有发生修改的key，用于通知监听者
    List<String> keysModified = null;
    
    Set<OnSharedPreferenceChangeListener> listeners = null;
    
    //要写入磁盘的map
    Map<String, Object> mapToWriteToDisk;

    synchronized (SharedPreferencesImpl.this.mLock) {
        if (mDiskWritesInFlight > 0) {
            // 此时有写入任务正在执行，所以不能直接修改 mMap，而是克隆它
            mMap = new HashMap<String, Object>(mMap);
        }
        //mapToWriteToDisk 为所有键值对
        mapToWriteToDisk = mMap;
        
        //写入任务+1
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
            //遍历通过 Editor 做出修改的键值对
            for (Map.Entry<String, Object> e : mModified.entrySet()) {
                String k = e.getKey();
                Object v = e.getValue();
                //在 remove 方法中，移除一个key时，将它的值设为 EditorImpl.this
                //所以如果 v==this,就代表移除一个key
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
                    //将新增加或者放生变更的key和value放到即将写入的map中
                    mapToWriteToDisk.put(k, v);
                }

                changesMade = true;
                if (hasListeners) {
                    //记录真正发生变更的key
                    keysModified.add(k);
                }
            }

            mModified.clear();

            if (changesMade) {
                //将内存中的 Sp 版本号自增
                mCurrentMemoryStateGeneration++;
            }
            //记录当前内存中的版本
            memoryStateGeneration = mCurrentMemoryStateGeneration;
        }
    }
    //创建 MemoryCommitResult 对象
    return new MemoryCommitResult(memoryStateGeneration, keysModified, listeners,
                    mapToWriteToDisk);
}
```

该方法的主要逻辑是，根据 Editor 的 mModified 中记录的发生修改的key，来更新内存中的 map 对应的 key。如果Sp有监听者，还需要记录发生修改的key，以便通知监听者。最后返回一个`MemoryCommitResult` 对象，它记录了当前内存中Sp版本号、发生变更的key、监听者和最要写入文件的map（即内存中Sp的所有键值对）。

需要注意的点：

1. 对于 Editor\#clear\(\) 方法，如果执行过，会先将所有键值对清空，然后写入 mModified 中记录的键值对
2. 内存中的Sp 有一个版本号标识，每次提交新的改动到内存后，该版本号会加1

该方法返回的是一个 `MemoryCommitResult` 对象，`MemoryCommitResult` 是 SharedPreferencesImpl 的静态内部类，它的定义如下：

```text
// Return value from EditorImpl#commitToMemory()
private static class MemoryCommitResult {
    //标识当前内存的版本
    final long memoryStateGeneration;
    //发生修改的key集合
    @Nullable final List<String> keysModified;
    //sp 的监听者
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
        //countDown
        writtenToDiskLatch.countDown();
    }
}
```

其中 setDiskWriteResult 方法用来设置文件写入结果，后面会讲到。

### SharedPreferencesImpl\#enqueDiskWrite

将变更提交的内存后得到 MemoryCommitResult 对象后，调用了 enqueDiskWrite 进行文件写入，这个方法源码如下：

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
                    //文件写入任务数减一
                    mDiskWritesInFlight--;
                }
                if (postWriteRunnable != null) {
                    //执行写入后的任务
                    postWriteRunnable.run();
                }
            }
        };

    if (isFromSyncCommit) {
        boolean wasEmpty = false;
        synchronized (mLock) {
            // mDiskWritesInFlight 在提交至内存时会自增，
            // 如果是 1 说明此时没有其他的任务要写入
            wasEmpty = mDiskWritesInFlight == 1;
        }
        if (wasEmpty) {
            //在当前线程中直接执行写入
            writeToDiskRunnable.run();
            return;
        }
    }

    QueuedWork.queue(writeToDiskRunnable, !isFromSyncCommit);
}
```

该方法中第二个参数是一个 Runnable ，用于在文件写入后执行。如果这个参数为null，那么就说明此次调用来自 commit\(\) 方法，否则此次调用就来自 apply \(\)方法，在上面的 apply 方法中，我们也看到它确实传了一个 Runnable。

上面方法中的 writeToDiskRunnable 定义了文件写入的过程：**先调用 writeToFile 将内存的Sp写入文件，然后将 mDiskWritesInFlight 减一，最后在执行 postWriteRunnable。**

不过 writeToDiskRunnable  的执行时机却暗藏玄机。如果此调用来自 commit\(\) 方法，并且目前只有这次写入需要执行，那么就会在当前线程执行这次文件写入\(如果我们的调用来自主线程，就会直接在主线程中执行文件写入，这就可能导致性能问题\)；其他情况都会通过 QueuedWork 把写入任务加入队列中，关于 QueuedWork 稍后再介绍，先来看看 writeToFile 方法如何执行文件写入的。

### SharedPreferencesImpl\#writeToFile\(\)

writeToFile 是执行文件写入的方法，代码如下，我省略的一些日志输出代码：

```text
@GuardedBy("mWritingToDiskLock")
private void writeToFile(MemoryCommitResult mcr, boolean isFromSyncCommit) {
    // 省略了一些代码

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
                    //对于 apply，没有必要每次都写入，而是只执行最新一次的提交对应的写入
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
            //备份文件不存在，将正式文件重命名为备份文件
            if (!mFile.renameTo(mBackupFile)) {
                //如果失败，停止写入
                mcr.setDiskWriteResult(false, false);
                return;
            }
        } else {
            //备份文件存在，删除正式文件
            mFile.delete();
        }
    }//end if(fileExists)..

    try {
        //创建文件输出流（会自动创建文件）
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

        //写入成功，删除备份文件
        mBackupFile.delete();

        //更新文件版本号
        mDiskStateGeneration = mcr.memoryStateGeneration;

        //设置写入结果
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

    // 发生异常，清理未成功写入的文件
    if (mFile.exists()) {
        if (!mFile.delete()) {
            Log.e(TAG, "Couldn't clean up partially-written file " + mFile);
        }
    }
    mcr.setDiskWriteResult(false, false);
}
```

在写入前，会先比较当前磁盘版本号与内存版本号，只有磁盘版本号小于内存版本号时，才会执行文件写入。

另外，对于 apply ，会将每次 mcr 的版本号与当前内存的最终版本号进行对比，只有相等时才会执行文件写入。这样一来，如果短时间内有多次 apply 文件写入请求，只有最后一次写入会被真正执行。

在执行写入前，会先创建一个备份文件，当写入过程中发生意外，下次读取时可以从备份文件中恢复。在从文件加载Sp 的 loadFromDisk 方法中就有如下代码：

```text
synchronized (mLock) {
    if (mLoaded) {
        return;
    }
    //如果备份文件存在，则优先从备份文件中恢复
    if (mBackupFile.exists()) {
        mFile.delete();
        mBackupFile.renameTo(mFile);
    }
}
```

正式的文件写入过程主要分以下几步:

1. 创建文件输出流
2. 将 map 写入到 xml 文件
3. 删除备份文件 
4. 更新磁盘Sp对应版本号 mDiskStateGeneration
5. 设置写入结果

关于第五步，针对文件写入情况，会调用 MemoryCommitResult\#setDiskWriteResult 方法设置结果，这个方法源码：

```text
//lock
final CountDownLatch writtenToDiskLatch = new CountDownLatch(1);

void setDiskWriteResult(boolean wasWritten, boolean result) {
    this.wasWritten = wasWritten;
    writeToDiskResult = result;
    //countDown
    writtenToDiskLatch.countDown();
}
```

设置完结果后，调用了 writtenToDiskLatch.countDown\(\) 以通知正在等待的线程。 

以上就是 apply 的过程，主要分为两步：

1. 提交修改至内存，生成一个 MemoryCommitResult 对象mcr,这个 mcr 记录着当前内存中sp的版本号、所有键值对、发生修改的键等信息
2. 通过 QueuedWork 将文件写入任务入队，等待执行

在讲解 QueuedWork 之前，我们先对照着看一下 commit 方法。

### EditorImpl\#commit\(\)

```text
public boolean commit() {
    //1.提交至内存
    MemoryCommitResult mcr = commitToMemory();
    //2.将文件写入任务入队列
    SharedPreferencesImpl.this.enqueueDiskWrite(
                mcr, null);
    try {
        //3.等待文件写入结果
        mcr.writtenToDiskLatch.await();
    } catch (InterruptedException e) {
        return false;
    } finally {
                
    }
    //4. 通知监听者
    notifyListeners(mcr);
    //5. 返回文件写入结果
    return mcr.writeToDiskResult;
}
```

commit 方法也包括提交内存和将文件写入任务入队，但后面还增加了等待文件写入完成的过程，因为commit 方法的返回值就是文件写入的结果。

另外，调用 [SharedPreferencesImpl\#enqueDiskWrite](sharedpreference.md#sharedpreferencesimpl-enquediskwrite)方法时，第二个参数传的是 null,在分析该方法时我们也看到，该方法正是以这个参数是否为 null 来区分是commit 还是 apply 的。

为了方便阅读，再把该方法源码贴一遍：

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
                    //文件写入任务数减一
                    mDiskWritesInFlight--;
                }
                if (postWriteRunnable != null) {
                    //执行写入后的任务
                    postWriteRunnable.run();
                }
            }
        };

    if (isFromSyncCommit) {
        boolean wasEmpty = false;
        synchronized (mLock) {
            // mDiskWritesInFlight 在提交至内存时会自增，
            // 如果是 1 说明此时没有其他的任务要写入
            wasEmpty = mDiskWritesInFlight == 1;
        }
        if (wasEmpty) {
            //在当前线程中直接执行写入
            writeToDiskRunnable.run();
            return;
        }
    }

    QueuedWork.queue(writeToDiskRunnable, !isFromSyncCommit);
}
```

对于 commit 方法提交，如果当前并没有其他文件写入任务要执行，那么会在当前线程执行文件写入。

接下来终于要揭晓 QueuedWork 的神秘面纱了。

## [QueuedWork](https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/android/app/QueuedWork.java) 

在看代码细节之前，先来看看官方注释对 QueuedWork 有个初步了解：

> Internal utility class to keep track of process-global work that's outstanding and hasn't been finished yet. 
>
> New work will be {@link [\#queue](https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/android/app/QueuedWork.java;l=221) queued}. It is possible to add 'finisher'-runnables that are {@link [\#waitToFinish](https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/android/app/QueuedWork.java;l=154) guaranteed to be run}. This is used to make sure the work has been finished.

> This was created for writing SharedPreference edits out asynchronously so we'd have a mechanism to wait for the writes in Activity.onPause and similar places, but we may use this mechanism for  other things in the future. 
>
>  The queued asynchronous work is performed on a separate, dedicated thread.

通过以上注释，可以获取一下信息：

1. 用于维护进程级别的任务
2. 新任务通过queue方法入队
3. 可以增加 finisher 任务，它们肯定会被执行，可以用来确保任务已经被执行完成
4. 目前主要用于执行 Sp 的文件同步并提供等待完成机制
5. 队列的任务会在单独线程中执行。

下面就来看看 QueuedWork 的代码实现，先来看看它的几个重要的静态属性：

### 静态属性

```text
private static final long DELAY = 100;
//类的锁
private static final Object sLock = new Object();
//处理工作的锁，确保只有一个线程在处理任务
private static Object sProcessingWork = new Object();

private static Handler sHandler = null;
//存储所有finisher
private static final LinkedList<Runnable> sFinishers = new LinkedList<>();
//存储所有任务
private static final LinkedList<Runnable> sWork = new LinkedList<>();
//是否允许延迟
private static boolean sCanDelay = true;

  
public static void addFinisher(Runnable finisher) {
    synchronized (sLock) {
        sFinishers.add(finisher);
    }
}    
```

### QueuedWork\#queue\(\)

前面在 SharedPreferencesImpl\#enqueDiskWrite\(\)，调用了queue 方法将文件写入工作入队，该方法源码如下：

```text
public static void queue(Runnable work, boolean shouldDelay) {
    //1.获取 handler
    Handler handler = getHandler();

    synchronized (sLock) {
        //2. 将work添加到任务列表
        sWork.add(work);
        //3. 发送消息触发执行
        if (shouldDelay && sCanDelay) {
            handler.sendEmptyMessageDelayed(QueuedWorkHandler.MSG_RUN, DELAY);
        } else {
            handler.sendEmptyMessage(QueuedWorkHandler.MSG_RUN);
        }
    }
}
```

主要逻辑就是讲任务加入队列中，然后通过handler发送消息来出发任务的执行。这个方法的第二个参数标识是否需要延后一段时间（DELAY 的值是100），在SharedPreferencesImpl\#enqueDiskWrite\(\)中是这样调用的：

```text
QueuedWork.queue(writeToDiskRunnable, !isFromSyncCommit);
```

也就是说，**对于 commit ，shouldDelay 为 false；对于 apply ，shouldDelay 为true**。shouldDelay 决定了在通过 handler 发送消息时是否启用延时。

那为什么要 apply 进行延时呢？ 

在 SharedPreferencesImpl 的 writeToFile 方法中有如下判断：

```text
if (mDiskStateGeneration < mcr.memoryStateGeneration) {
    if (isFromSyncCommit) {
        needsWrite = true;
    } else {
        synchronized (mLock) {
            //对于 apply，没有必要每次都写入，而是只执行最新一次的提交对应的写入
            if (mCurrentMemoryStateGeneration == mcr.memoryStateGeneration) {
                needsWrite = true;
            }
        }
    }
}
```

DELAY 的值是常量100，如果100ms 内有多次 apply 提交，这个延时可以确保当100ms后，任务队列中的文件写入任务被统一处理时，只有最新的apply提交对应的文件写入任务会真正被执行。因为只有它对应的mcr的版本号是和内存 一致的，其他的mcr版本都低于内存版本。这样可以有效去除冗余的文件写入，提升性能。

### getHandler\(\)

上面方法中 handler 的获取调用了 getHandler\(\) 方法，其源码如下：

```text
private static Handler getHandler() {
    synchronized (sLock) {
        if (sHandler == null) {
            //创建新的线程
            HandlerThread handlerThread = new HandlerThread("queued-work-looper",
                    Process.THREAD_PRIORITY_FOREGROUND);
            handlerThread.start();
            sHandler = new QueuedWorkHandler(handlerThread.getLooper());
        }
        return sHandler;
    }
}
```

可以看到，初始化时，新建了一个 `HanlderThread` ，并基于它创建了一个 QueuedWorkHandler 赋值给 sHandler。

### QueuedWorkHandler

QueuedWorkHandler 是 QueuedWork 的静态内部类，它的定义很简单，在收到MSG\_RUN消息后，调用 processPendingWork\(\) 处理所有待执行的任务。

```text
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
```

### processPendingWork

```text
private static void processPendingWork() {
    synchronized (sProcessingWork) {
        LinkedList<Runnable> work;

        synchronized (sLock) {
            work = (LinkedList<Runnable>) sWork.clone();
            sWork.clear();

            // 移除所有MSG_RUN消息
            getHandler().removeMessages(QueuedWorkHandler.MSG_RUN);
        }

        if (work.size() > 0) {
            //按顺序执行任务
            for (Runnable w : work) {
                w.run();
            }
        }
    }
}
```

processPendingWork 的逻辑也很简单，就是将任务列表中的任务按序执行。正常情况下，这个过程就是在 getHandler 中创建的 HandlerThread 中进行。

那不正常情况呢？

### waitToFinish

```text
/**
 * Trigger queued work to be processed immediately. The queued work is processed on a separate
 * thread asynchronous. While doing that run and process all finishers on this thread. The
 * finishers can be implemented in a way to check weather the queued work is finished.
 *
 * Is called from the Activity base class's onPause(), after BroadcastReceiver's onReceive,
 * after Service command handling, etc. (so async work is never lost)
 */
public static void waitToFinish() {

    Handler handler = getHandler();

    synchronized (sLock) {
        if (handler.hasMessages(QueuedWorkHandler.MSG_RUN)) {
            // Delayed work will be processed at processPendingWork() below
            handler.removeMessages(QueuedWorkHandler.MSG_RUN);
        }

        // We should not delay any work as this might delay the finishers
        sCanDelay = false;
    }

    StrictMode.ThreadPolicy oldPolicy = StrictMode.allowThreadDiskWrites();
    try {
        processPendingWork();
    } finally {
        StrictMode.setThreadPolicy(oldPolicy);
    }

    try {
        while (true) {
            Runnable finisher;

            synchronized (sLock) {
                finisher = sFinishers.poll();
            }

            if (finisher == null) {
                break;
            }

            finisher.run();
        }
    } finally {
        sCanDelay = true;
    }
    //...

}
```

waitToFinish 会在当前线程立刻执行所有待执行的任务，任务执行完后会一并执行所有 finisher 来通知任务执行完成。

而通过方法的注释可以看出，这个方法主要在 Activity的onPause 方法中、BroadcastReceiver 的 onReceive 方法后、以及Service 的 onCommand 后调用（具体见下面代码），以确保所有任务都被执行没有丢失，但这回导致在主线程执行文件写入，是有可能造成性能问题的。

```text
//Service 启动
private void handleServiceArgs(ServiceArgsData data) {
        Service s = mServices.get(data.token);
        if (s != null) {
            try {
                if (data.args != null) {
                    data.args.setExtrasClassLoader(s.getClassLoader());
                    data.args.prepareToEnterProcess();
                }
                int res;
                if (!data.taskRemoved) {
                    //调用Service的 onStartCommand
                    res = s.onStartCommand(data.args, data.flags, data.startId);
                } else {
                    s.onTaskRemoved(data.args);
                    res = Service.START_TASK_REMOVED_COMPLETE;
                }

                QueuedWork.waitToFinish();
                
                //.....
            }
        }
    }
```

```text
//service stop
private void handleStopService(IBinder token) {
        Service s = mServices.remove(token);
        if (s != null) {
            try {
                //调用Service的onDestroy方法
                s.onDestroy();
                s.detachAndCleanUp();
                //.....
                
                QueuedWork.waitToFinish();

                //....
            } catch (Exception e) {
                if (!mInstrumentation.onException(s, e)) {
                    throw new RuntimeException(
                            "Unable to stop service " + s
                            + ": " + e.toString(), e);
                }
                Slog.i(TAG, "handleStopService: exception for " + token, e);
            }
        } 
    }
```

```text
//在 Android 11 之前，在 onPause调用后执行 QueuedWork.waitToFinish();
@Override
    public void handlePauseActivity(IBinder token, boolean finished, boolean userLeaving,
            int configChanges, PendingTransactionActions pendingActions, String reason) {
        ActivityClientRecord r = mActivities.get(token);
        if (r != null) {
            if (userLeaving) {
                performUserLeavingActivity(r);
            }

            r.activity.mConfigChangeFlags |= configChanges;
            performPauseActivity(r, finished, reason, pendingActions);

            // Make sure any pending writes are now committed.
            if (r.isPreHoneycomb()) {
                QueuedWork.waitToFinish();
            }
            mSomeActivitiesChanged = true;
        }
    }
```

```text
//在Android 11之后，在onStop 后调用
public void handleStopActivity(IBinder token, boolean show, int configChanges,
            PendingTransactionActions pendingActions, boolean finalStateRequest, String reason) {
        final ActivityClientRecord r = mActivities.get(token);
        r.activity.mConfigChangeFlags |= configChanges;

        final StopInfo stopInfo = new StopInfo();
        performStopActivityInner(r, stopInfo, show, true /* saveState */, finalStateRequest,
                reason);

        //...

        // Make sure any pending writes are now committed.
        if (!r.isPreHoneycomb()) {
            QueuedWork.waitToFinish();
        }
        
        //...
}
```

## 建议

* 尽量使用apply
* 多次 put ，一次apply
* 拆分 sp 文件的大小，避免一个 app 只使用一个 Sp，这样文件将会变得很大，写入时间会变长。如果恰好卡在 waitToFinish 这样的时间点，有可能造成 ANR。
* 不要连续多次edit\(\), 应该获取一次获取edit\(\),然后多次执行putxxx\(\), 减少内存波动
* 不要使用MODE\_MULTI\_PROCESS

## 相关问题

### commit 和 apply 区别？

commit 会等待文件写入完成，并且有可能会在主线程写入文件，并且没有针对短时间内频繁更新做优化，有可能导致每次操作都在主线程写入。

apply 如果短时间内\(100ms\)有多次提交，只有最后一次会执行文件写入（因为会对比每次提交的版本号是否与当前内存版本号一致），并且是在单独的线程里执行写入，不会影响性能。

## 相关链接

[gityuan:全面剖析SharedPreferences](http://gityuan.com/2017/06/18/SharedPreferences/)

[SharedPreferences灵魂拷问之原理](https://juejin.im/post/5df7af66e51d4557f17fb4f7)

