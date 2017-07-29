package jrfeng.simplemusic.base;

public interface BaseView<T extends BasePresenter> {
    void setPresenter(T presenter);
}
