package com.csw8929.minseo.nas.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.csw8929.minseo.nas.NasCredentialsValue;
import com.csw8929.minseo.nas.R;

/**
 * NAS 공용 7필드 (host / port / LAN host / LAN port / user / pass / path) CustomView.
 *
 * 사용:
 * <pre>
 * &lt;com.csw8929.minseo.nas.ui.NasFieldsView
 *     android:id="@+id/nas_fields"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content" /&gt;
 * </pre>
 *
 * 자바:
 * <pre>
 * NasFieldsView fields = findViewById(R.id.nas_fields);
 * fields.setFromValue(prefs.toValue());
 * ...
 * NasCredentialsValue v = fields.getFormValues();
 * </pre>
 *
 * 도메인 필드(nickname / base_path / pos_dir 등)는 앱이 이 View 위 또는 아래에 자유롭게 얹는다.
 */
public class NasFieldsView extends LinearLayout {

    private EditText etHost, etPort, etLanHost, etLanPort, etUser, etPass, etPath;

    public NasFieldsView(Context context) { this(context, null); }
    public NasFieldsView(Context context, @Nullable AttributeSet attrs) { this(context, attrs, 0); }
    public NasFieldsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.minseo_nas_fields_view, this, true);
        etHost    = findViewById(R.id.minseo_nas_et_host);
        etPort    = findViewById(R.id.minseo_nas_et_port);
        etLanHost = findViewById(R.id.minseo_nas_et_lan_host);
        etLanPort = findViewById(R.id.minseo_nas_et_lan_port);
        etUser    = findViewById(R.id.minseo_nas_et_user);
        etPass    = findViewById(R.id.minseo_nas_et_pass);
        etPath    = findViewById(R.id.minseo_nas_et_path);
    }

    /** 기존 자격증명을 폼에 채움. */
    public void setFromValue(NasCredentialsValue v) {
        etHost.setText(v.getHost());
        etPort.setText(String.valueOf(v.getPort()));
        etLanHost.setText(v.getLanHost());
        etLanPort.setText(String.valueOf(v.getLanPort()));
        etUser.setText(v.getUser());
        etPass.setText(v.getPass());
        etPath.setText(v.getPosDir());
    }

    /** 현재 폼 입력값을 POJO 로 반환. trim 처리. */
    public NasCredentialsValue getFormValues() {
        return NasCredentialsValue.builder()
                .host(etHost.getText().toString().trim())
                .port(parseIntOr(etPort.getText().toString(), 5000))
                .lanHost(etLanHost.getText().toString().trim())
                .lanPort(parseIntOr(etLanPort.getText().toString(), 5000))
                .user(etUser.getText().toString().trim())
                .pass(etPass.getText().toString())
                .basePath("/")
                .posDir(etPath.getText().toString().trim())
                .build();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        etHost.setEnabled(enabled);
        etPort.setEnabled(enabled);
        etLanHost.setEnabled(enabled);
        etLanPort.setEnabled(enabled);
        etUser.setEnabled(enabled);
        etPass.setEnabled(enabled);
        etPath.setEnabled(enabled);
    }

    private static int parseIntOr(String s, int fallback) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return fallback; }
    }
}
