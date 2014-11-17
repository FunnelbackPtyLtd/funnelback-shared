package com.funnelback.publicui.search.service.auth;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

@Component
public class DefaultAuthTokenManager implements AuthTokenManager {

    @Override
    public String getToken(String url, String serverSecret) {
        // FIXME The Perl UI expects non standard Base64 encoded
        // (22 bytes long instead of a multiple of 4, see the doco
        // for Perl Digest::MD5::md5_base64()
        // This can change once the auth tokens are java end-to-end
        return StringUtils.removeEnd(new String(Base64.encodeBase64(DigestUtils.md5(serverSecret+url))), "==");
    }

    @Override
    public boolean checkToken(String token, String url, String serverSecret) {
        return getToken(url,serverSecret).equals(token);
    }

}
