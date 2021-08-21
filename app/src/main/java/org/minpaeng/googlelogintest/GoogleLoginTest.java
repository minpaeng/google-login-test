package org.minpaeng.googlelogintest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class GoogleLoginTest extends AppCompatActivity implements View.OnClickListener{

    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "requestIdToken";
    private TextView mStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_login);

        // Views
        mStatusTextView = findViewById(R.id.status);

        // Button listeners
        findViewById(R.id.button).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);

        //서버 클라이언트 아이디
        String serverClientId = getString(R.string.server_client_id);

        //이메일, 토큰ID 요청
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                //.requestScopes(new Scope(Scopes.DRIVE_APPFOLDER)) //인증코드를 얻기 위해 필요한 코드
                //.requestServerAuthCode(serverClientId) //인증코드를 얻기 위해 필요한 코드 : 토큰과 교환할 수 있음
                .requestEmail()
                .requestIdToken(serverClientId) // 토큰
                .build();

        //클라이언트 생성
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


    }

    @Override
    public void onStart() {
        super.onStart();

        //앱에 로그인 되어 있지 않은 상태라면 null 반환
        //앱에 이미 로그인 되어 있는 상태라면 null을 반환하지 않음
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);
    }

    // 구글 계정 선택 후 결과 처리
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // GoogleSignInClient.getSignInIntent 인텐트 실행 후 결과코드 반환
        if (requestCode == RC_SIGN_IN) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    // 로그인 결과 처리
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            //로그인 성공 시 계정에 맞는 UI로 업데이트
            updateUI(account);
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    // 로그인
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // 로그아웃
    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }

    // 앱에 계정 접근 끊어내기(버튼 추가하여 온클릭 이벤트 등록): 탈퇴 개념인거같은데 정확하지 않음
    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }

    //계정 상태에 맞는 UI로 업데이트
    private void updateUI(@Nullable GoogleSignInAccount account) {
        if (account != null) {
            mStatusTextView.setText(getString(R.string.signed_in_fmt, account.getDisplayName()));

            //계정 정보 가져오기
            String personName = account.getDisplayName();
            String personGivenName = account.getGivenName();
            String personFamilyName = account.getFamilyName();
            String personEmail = account.getEmail();
            String personId = account.getId();
            Uri personPhoto = account.getPhotoUrl();
            String serverAuthCode = account.getServerAuthCode(); //onCreate함수 gso부분 주석 해제하면 값이 반환됨
            String idToken = account.getIdToken();

            Log.d(TAG, "handleSignInResult:personName "+personName);
            Log.d(TAG, "handleSignInResult:personGivenName "+personGivenName);
            Log.d(TAG, "handleSignInResult:personEmail "+personEmail);
            Log.d(TAG, "handleSignInResult:personId "+personId);
            Log.d(TAG, "handleSignInResult:personFamilyName "+personFamilyName);
            Log.d(TAG, "handleSignInResult:personPhoto "+personPhoto);
            Log.d(TAG, "handleSignInResult:serverAuthCode "+serverAuthCode);
            Log.d(TAG, "handleSignInResult:idToken "+idToken);

            findViewById(R.id.button).setVisibility(View.GONE);
            findViewById(R.id.button2).setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setText(R.string.signed_out);

            findViewById(R.id.button).setVisibility(View.VISIBLE);
            findViewById(R.id.button2).setVisibility(View.GONE);
        }
    }

    private void refreshIdToken() {
        // 토큰 만료 시 refresh
        // 어케쓰는지 아직 잘 모르겠음
        mGoogleSignInClient.silentSignIn()
                .addOnCompleteListener(this, new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        handleSignInResult(task);
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                signIn();
                break;
            case R.id.button2:
                signOut();
                break;
        }
    }
}
