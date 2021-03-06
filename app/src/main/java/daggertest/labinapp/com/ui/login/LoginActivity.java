package daggertest.labinapp.com.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import daggertest.labinapp.com.base.BaseActivity;
import daggertest.labinapp.com.base.BaseApplication;

import daggertest.labinapp.com.daggertest.R;
import daggertest.labinapp.com.ui.login.di.LoginActivityModule;
import daggertest.labinapp.com.ui.main.MainActivity;
import retrofit2.Retrofit;

import static daggertest.labinapp.com.util.NullChecker.isNotNull;


public class LoginActivity extends BaseActivity implements LoginContract.View {
    @Inject
    Retrofit retrofit;
    @Inject
    LoginPresenter mPresenter;
    public final int REQUEST_SIGN_GOOGLE = 9001;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        ButterKnife.bind(this);


    }


    @OnClick(R.id.googleLogin)
    public void googleLogin() {
        Intent intent = mPresenter.loginWithGoogle();
        startActivityForResult(intent, REQUEST_SIGN_GOOGLE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SIGN_GOOGLE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            checkResultThenProceedToLogin(result);
        }
    }

    private void checkResultThenProceedToLogin(GoogleSignInResult result) {
        if (isNotNull(result)) {
            showProgressDialog();
            mPresenter.logInWithFirebase(result.getSignInAccount());
        }
    }


    @Override
    public void startNextActivity() {
        hideProgressDialog();
        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public void showFirebaseAuthenticationFailedMessage(String error) {
        hideProgressDialog();
        Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.takeView(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.dropView();  //prevent leaking activity in
    }


    @Override
    protected void setupActivityComponent() {
        BaseApplication.get(this).getAppComponent()
                .plus(new LoginActivityModule(this))
                .inject(this);
    }
}
