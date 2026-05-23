package com.csw8929.minseo.nas;

/** DSM envelope 의 에러 코드를 실어 상위에서 만료 감지/분기에 쓰게 한다. */
public final class DsmException extends Exception {
    public final int code;

    public DsmException(int code, String msg) {
        super(msg + " (code=" + code + ")");
        this.code = code;
    }
}
