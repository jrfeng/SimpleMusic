package jrfeng.player.utils.durable;

import android.support.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 一个可持久化的 List。所有方法都是线程安全的。
 * @param <E>
 */
public class DurableList<E> implements List<E>, Durable {
    private File mFile;
    private List<E> mList;
    private boolean mRestored;

    private Lock mReadLock;
    private Lock mWriteLock;

    //**********************Constructor*********************

    public DurableList(String file) {
        this(new File(file));
    }

    public DurableList(File file) {
        mFile = file;
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        mReadLock = lock.readLock();
        mWriteLock = lock.writeLock();
    }

    //*************************List*************************

    @Override
    public int size() {
        mReadLock.lock();
        try {
            return mList.size();
        } finally {
            mReadLock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        mReadLock.lock();
        try {
            return mList.isEmpty();
        } finally {
            mReadLock.unlock();
        }
    }

    @Override
    public boolean contains(Object o) {
        mReadLock.lock();
        try {
            return mList.contains(o);
        } finally {
            mReadLock.unlock();
        }
    }

    @NonNull
    @Override
    public Iterator<E> iterator() {
        mReadLock.lock();
        try {
            return mList.iterator();
        } finally {
            mReadLock.unlock();
        }
    }

    @NonNull
    @Override
    public Object[] toArray() {
        mReadLock.lock();
        try {
            return mList.toArray();
        } finally {
            mReadLock.unlock();
        }
    }

    @NonNull
    @Override
    public <T> T[] toArray(@NonNull T[] a) {
        mReadLock.lock();
        try {
            return mList.toArray(a);
        } finally {
            mReadLock.unlock();
        }
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        mReadLock.lock();
        try {
            return mList.containsAll(c);
        } finally {
            mReadLock.unlock();
        }
    }

    @Override
    public boolean add(E e) {
        mWriteLock.lock();
        try {
            return mList.add(e);
        } finally {
            mWriteLock.unlock();
        }
    }

    @Override
    public boolean remove(Object o) {
        mWriteLock.lock();
        try {
            return mList.remove(o);
        } finally {
            mWriteLock.unlock();
        }
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends E> c) {
        mWriteLock.lock();
        try {
            return mList.addAll(c);
        } finally {
            mWriteLock.unlock();
        }
    }

    @Override
    public boolean addAll(int index, @NonNull Collection<? extends E> c) {
        mWriteLock.lock();
        try {
            return mList.addAll(index, c);
        } finally {
            mWriteLock.unlock();
        }
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        mWriteLock.lock();
        try {
            return mList.removeAll(c);
        } finally {
            mWriteLock.unlock();
        }
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        mWriteLock.lock();
        try {
            return mList.retainAll(c);
        } finally {
            mWriteLock.unlock();
        }
    }

    @Override
    public void clear() {
        mWriteLock.lock();
        try {
            mList.clear();
        } finally {
            mWriteLock.unlock();
        }
    }

    @Override
    public E get(int index) {
        mReadLock.lock();
        try {
            return mList.get(index);
        } finally {
            mReadLock.unlock();
        }
    }

    @Override
    public E set(int index, E element) {
        mWriteLock.lock();
        try {
            return mList.set(index, element);
        } finally {
            mWriteLock.unlock();
        }
    }

    @Override
    public void add(int index, E element) {
        mWriteLock.lock();
        try {
            mList.add(index, element);
        } finally {
            mWriteLock.unlock();
        }
    }

    @Override
    public E remove(int index) {
        mWriteLock.lock();
        try {
            return mList.remove(index);
        } finally {
            mWriteLock.unlock();
        }
    }

    @Override
    public int indexOf(Object o) {
        mReadLock.lock();
        try {
            return mList.indexOf(o);
        } finally {
            mReadLock.unlock();
        }
    }

    @Override
    public int lastIndexOf(Object o) {
        mReadLock.lock();
        try {
            return mList.lastIndexOf(o);
        } finally {
            mReadLock.unlock();
        }
    }

    @Override
    public ListIterator<E> listIterator() {
        mReadLock.lock();
        try {
            return mList.listIterator();
        } finally {
            mReadLock.unlock();
        }
    }

    @NonNull
    @Override
    public ListIterator<E> listIterator(int index) {
        mReadLock.lock();
        try {
            return mList.listIterator(index);
        } finally {
            mReadLock.unlock();
        }
    }

    @NonNull
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        mReadLock.lock();
        try {
            return mList.subList(fromIndex, toIndex);
        } finally {
            mReadLock.unlock();
        }
    }

    //*************************Durable**********************

    @Override
    public void restore() {  //提示：有阻塞UI线程的风险
        mWriteLock.lock();
        try {
            //调试
            log("从本地恢复 : " + mFile.getName());

            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(mFile));
            ObjectInputStream input = new ObjectInputStream(inputStream);
            mList = (LinkedList<E>) input.readObject();
            input.close();

            //调试
            log("恢复成功 : " + mFile.getName());
        } catch (IOException | ClassNotFoundException e) {
            //调试
            log("恢复失败 : 新建 " + mFile.getName());
            mList = new LinkedList<>();
        } finally {
            mRestored = true;
            mWriteLock.unlock();
        }
    }

    @Override
    public void restoreAsync(final OnRestoredListener listener) {
        new Thread() {
            @Override
            public void run() {
                restore();
                if (listener != null) {
                    listener.onRestored();
                }
            }
        }.start();
    }

    @Override
    public boolean isRestored() {
        return mRestored;
    }

    @Override
    public synchronized void save() {
        mReadLock.lock();
        try {
            //调试
            log("开始保存 : " + mFile.getName());

            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(mFile));
            ObjectOutputStream output = new ObjectOutputStream(outputStream);

            output.writeObject(mList);
            output.close();

            //调试
            log("保存成功 : " + mFile.getAbsolutePath());
        } catch (IOException e) {
            //调试
            log("保存失败 : " + mFile.getAbsolutePath() + " : " + e);
        } finally {
            mReadLock.unlock();
        }
    }

    @Override
    public void saveAsync() {
        saveAsync(null);
    }

    @Override
    public void saveAsync(final OnSavedListener listener) {
        new Thread() {
            @Override
            public void run() {
                save();
                if (listener != null) {
                    listener.onSaved();
                }
            }
        }.start();
    }

    //***************private****************

    //调试用
    private void log(String msg) {
        System.out.println("DurableList : " + msg);
    }
}