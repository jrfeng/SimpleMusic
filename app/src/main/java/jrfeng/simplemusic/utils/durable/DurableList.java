package jrfeng.simplemusic.utils.durable;


import android.support.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;


public class DurableList<E> implements List<E>, Durable {
    private File mFile;
    private List<E> mList;
    private boolean mIsChanged;
    private boolean mIsSaved;

    //**********************Constructor*********************

    public DurableList(String file) {
        this(new File(file));
    }

    public DurableList(File file) {
        mFile = file;
    }

    //*************************List*************************

    @Override
    public int size() {
        return mList.size();
    }

    @Override
    public boolean isEmpty() {
        return mList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return mList.contains(o);
    }

    @NonNull
    @Override
    public Iterator<E> iterator() {
        return mList.iterator();
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return mList.toArray();
    }

    @NonNull
    @Override
    public <T> T[] toArray(T[] a) {
        return mList.toArray(a);
    }

    @Override
    public boolean add(E e) {
        mIsSaved = false;
        return mIsChanged = mList.add(e);
    }

    @Override
    public boolean remove(Object o) {
        mIsSaved = false;
        return mIsChanged = mList.remove(o);
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        mIsSaved = false;
        return mList.containsAll(c);
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends E> c) {
        mIsSaved = false;
        return mIsChanged = mList.addAll(c);
    }

    @Override
    public boolean addAll(int index, @NonNull Collection<? extends E> c) {
        mIsSaved = false;
        return mIsChanged = mList.addAll(index, c);
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        mIsSaved = false;
        return mIsChanged = mList.removeAll(c);
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        mIsSaved = false;
        return mIsChanged = mList.removeAll(c);
    }

    @Override
    public void clear() {
        mIsSaved = false;
        mIsChanged = true;
        mList.clear();
    }

    @Override
    public E get(int index) {
        return mList.get(index);
    }

    @Override
    public E set(int index, E element) {
        mIsSaved = false;
        mIsChanged = true;
        return mList.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        mIsSaved = false;
        mIsChanged = true;
        mList.add(index, element);
    }

    @Override
    public E remove(int index) {
        mIsSaved = false;
        mIsChanged = true;
        return mList.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return mList.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return mList.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return mList.listIterator();
    }

    @NonNull
    @Override
    public ListIterator<E> listIterator(int index) {
        return mList.listIterator(index);
    }

    @NonNull
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return mList.subList(fromIndex, toIndex);
    }

    //*************************Durable**********************

    @Override
    public void restore() {  //提示：有阻塞UI线程的风险
        try {
            //调试
            log("尝试从本地恢复 : " + mFile.getName());

            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(mFile));
            ObjectInputStream input = new ObjectInputStream(inputStream);
            mList = (LinkedList<E>) input.readObject();
            input.close();

            //调试
            log("恢复成功");
        } catch (IOException | ClassNotFoundException e) {
            //调试
            log("恢复失败 : 新建");
            mList = new LinkedList<>();
        }
    }

    @Override
    public void restoreAsync(OnRestoredListener listener) {
        restore();
        listener.onRestored();
    }

    @Override
    public synchronized void save() {
        if (!mIsChanged) {
            //调试
            log("不保存, 数据集未发生改变 : " + mFile.getName());
            return;
        }

        if (mIsSaved) {
            //调试
            log("已保存过 : " + mFile.getName());
            return;
        }

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
            mIsSaved = true;
        }
    }

    @Override
    public void saveAsync() {
        new Thread() {
            @Override
            public void run() {
                save();
            }
        }.start();
    }

    public boolean isChanged() {
        return mIsChanged;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        saveAsync();
    }

    //***************private****************

    private void log(String msg) {
        System.out.println("DurableList : " + msg);
    }
}