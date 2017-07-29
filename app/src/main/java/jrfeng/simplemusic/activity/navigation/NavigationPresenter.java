package jrfeng.simplemusic.activity.navigation;

public class NavigationPresenter implements NavigationContract.Presenter {
    private NavigationContract.View mView;

    public NavigationPresenter(NavigationContract.View view){
        mView = view;
    }

    @Override
    public void start() {

    }
}
