package me.zhyd.oauth.request;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSONObject;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.model.AuthSource;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.utils.UrlBuilder;

/**
 * CSDN登录
 *
 * @author yadong.zhang (yadong.zhang0415(a)gmail.com)
 * @version 1.0
 * @since 1.8
 */
public class AuthCsdnRequest extends BaseAuthRequest {

    public AuthCsdnRequest(AuthConfig config) {
        super(config, AuthSource.CSDN);
    }

    @Override
    protected AuthToken getAccessToken(String code) {
        String accessTokenUrl = UrlBuilder.getCsdnAccessTokenUrl(config.getClientId(), config.getClientSecret(), code, config.getRedirectUri());
        HttpResponse response = HttpRequest.post(accessTokenUrl).execute();
        JSONObject accessTokenObject = JSONObject.parseObject(response.body());
        if (accessTokenObject.containsKey("error_code")) {
            throw new AuthException("Unable to get token from csdn using code [" + code + "]");
        }
        return AuthToken.builder()
                .accessToken(accessTokenObject.getString("access_token"))
                .build();
    }

    @Override
    protected AuthUser getUserInfo(AuthToken authToken) {
        String accessToken = authToken.getAccessToken();
        HttpResponse response = HttpRequest.get(UrlBuilder.getCsdnUserInfoUrl(accessToken)).execute();
        JSONObject object = JSONObject.parseObject(response.body());
        if (object.containsKey("error_code")) {
            throw new AuthException(object.getString("error"));
        }
        return AuthUser.builder()
                .username(object.getString("username"))
                .token(authToken)
                .source(AuthSource.CSDN)
                .build();
    }
}
