package com.csw8929.minseo.nas;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Synology FileStation 동기 호출 헬퍼. SID 만료 시 자동 재시도, envelope 디코딩 포함.
 *
 * 호출 스레드에서 동기 실행 — 호출자가 자체 background executor 로 감싼다.
 * 메서드 모두 실패 시 {@link DsmException} 또는 일반 {@link Exception} 던짐.
 *
 * 두 앱(Minseo3 진도/즐겨찾기 + Minseo6 위치)이 공유하던 "JSON 파일을 FileStation에 push/fetch" 패턴 추출.
 */
public final class FileStation {

    private static final String TAG = "NasFileStation";

    private FileStation() {}

    /**
     * 폴더 안의 모든 파일 이름 반환. 폴더가 없으면 빈 리스트 (DSM 408 처리).
     * 하위 폴더는 isdir=true 항목으로 응답에 섞여 오지만 본 메서드는 파일/폴더 모두 반환.
     * 호출자가 필요 시 파일명 패턴으로 필터.
     */
    public static List<String> listFiles(String dir) throws Exception {
        String normalized = SynologyDsmHelper.normalizeDir(dir);
        return SynologyDsmHelper.withSidAndRetry(sid -> {
            String url = DsAuth.apiBase() + "/webapi/entry.cgi"
                    + "?api=SYNO.FileStation.List&version=2&method=list"
                    + "&folder_path=" + URLEncoder.encode(normalized, "UTF-8")
                    + "&_sid=" + sid;
            String body = DsHttp.httpGet(url);
            JSONObject envelope = new JSONObject(body);
            if (!envelope.optBoolean("success", false)) {
                int code = SynologyDsmHelper.errorCode(envelope);
                if (code == SynologyDsmHelper.ERROR_NO_SUCH_FILE) {
                    Log.i(TAG, "listFiles: " + normalized + " 없음 → 빈 리스트");
                    return Collections.<String>emptyList();
                }
                throw new DsmException(code, "list failed");
            }
            JSONArray files = envelope.getJSONObject("data").optJSONArray("files");
            List<String> names = new ArrayList<>();
            if (files == null) return names;
            for (int i = 0; i < files.length(); i++) {
                JSONObject f = files.getJSONObject(i);
                String name = f.optString("name", "");
                if (!name.isEmpty()) names.add(name);
            }
            return names;
        });
    }

    /**
     * 풀 경로의 파일을 받아 JSONObject 로 파싱.
     * 파일 없음(HTML 404 또는 envelope success=false) 이면 null 반환.
     */
    public static JSONObject downloadJson(String fullPath) throws Exception {
        return SynologyDsmHelper.withSidAndRetry(sid -> {
            String url = DsAuth.apiBase() + "/webapi/entry.cgi"
                    + "?api=SYNO.FileStation.Download&version=2&method=download"
                    + "&path=" + URLEncoder.encode(fullPath, "UTF-8")
                    + "&mode=open"
                    + "&_sid=" + sid;
            String body = DsHttp.httpGet(url);
            if (body.isEmpty()) return null;
            if (body.startsWith("<")) return null; // HTML 404
            if (body.startsWith("{") && body.contains("\"success\"")) {
                JSONObject envelope = new JSONObject(body);
                if (!envelope.optBoolean("success", false)) {
                    int code = SynologyDsmHelper.errorCode(envelope);
                    if (code == SynologyDsmHelper.ERROR_NO_SUCH_FILE) return null;
                    throw new DsmException(code, "download failed");
                }
            }
            return new JSONObject(body);
        });
    }

    /** JSON 한 객체를 dir 아래 fileName 으로 업로드 (overwrite=true). */
    public static void uploadJson(String dir, String fileName, JSONObject content) throws Exception {
        String normalized = SynologyDsmHelper.normalizeDir(dir);
        byte[] body = content.toString().getBytes(StandardCharsets.UTF_8);
        Log.i(TAG, "uploadJson: " + normalized + "/" + fileName + " (" + body.length + " bytes)");
        SynologyDsmHelper.withSidAndRetry(sid -> {
            String url = DsAuth.apiBase() + "/webapi/entry.cgi?_sid="
                    + URLEncoder.encode(sid, "UTF-8");
            String resp = DsHttp.uploadFile(url, normalized, fileName, body, sid);
            JSONObject envelope = new JSONObject(resp);
            if (!envelope.optBoolean("success", false)) {
                throw new DsmException(SynologyDsmHelper.errorCode(envelope), "upload failed");
            }
            return null;
        });
    }

    /** 풀 경로의 파일 삭제. 파일 없으면 noop (DSM 408 처리). */
    public static void delete(String fullPath) throws Exception {
        SynologyDsmHelper.withSidAndRetry(sid -> {
            String url = DsAuth.apiBase() + "/webapi/entry.cgi"
                    + "?api=SYNO.FileStation.Delete&version=2&method=delete"
                    + "&path=" + URLEncoder.encode(fullPath, "UTF-8")
                    + "&_sid=" + sid;
            String body = DsHttp.httpGet(url);
            JSONObject envelope = new JSONObject(body);
            if (!envelope.optBoolean("success", false)) {
                int code = SynologyDsmHelper.errorCode(envelope);
                if (code == SynologyDsmHelper.ERROR_NO_SUCH_FILE) return null;
                throw new DsmException(code, "delete failed");
            }
            return null;
        });
    }
}
