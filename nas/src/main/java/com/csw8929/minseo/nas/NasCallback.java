package com.csw8929.minseo.nas;

/** 비동기 NAS 작업 결과 콜백. 메인 스레드에서 호출된다. */
public interface NasCallback<T> {
    void onResult(T value);
    void onError(String message);
}
