/*
 * Copyright (c) 2015-2018 Vladimir Schneider <vladimir.schneider@gmail.com>, all rights reserved.
 *
 * This code is private property of the copyright holder and cannot be used without
 * having obtained a license or prior written permission of the of the copyright holder.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package com.vladsch.idea.multimarkdown.license;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.net.HttpConfigurable;
import com.vladsch.idea.multimarkdown.MdPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.crypto.NoSuchPaddingException;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LicenseAgent {
    private static final Logger logger = Logger.getInstance("com.vladsch.idea.multimarkdown.license.agent");

    // RELEASE : set to false for release
    private static final boolean LOG_AGENT_INFO = false;

    final static private String agent_signature = "475f99b03f6ec213729d7f5d577c80aa";
    final static private String license_pub = "-----BEGIN PUBLIC KEY-----\n" +
            "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAlnefMGqNu1Q9hcI2Rd8G\n" +
            "xyKlXQIFyXWIkYODRrLjvEwXYw0yksgjZeIC4g+hakyQNiN+TGE/xvo3fqB0CU4A\n" +
            "aE33Mu7jB27dt1IItcmBhJBwIhmZDc0SWNj6ywvnLeUU/NSWWbJ1SaXzPQJ2Mm5T\n" +
            "Mr3wDFhCTp80pN4svOQmdQPFSKXwdGI+n8gJvc28vRgD8As2XxgkYsZPNjefOsla\n" +
            "GHS8CNw6uI8Ijcf9hfX22twQZ+auYNL/vqtBEKq2jNLwoHTo68s+0JWJu2YILlIe\n" +
            "VQzXcXZyhhAVdZrMNGhBPiXUia6YrJpqZNDZ35CE+Y6ecs9c5AG2wpFJHnic2cjZ\n" +
            "Kh+ba83DpA3GxYa1OGMGZNaIqCjuK7A82ZPriXsoxL6YJzqSlbF/2l2x4Y3VoVTF\n" +
            "LWKEpjvLOuDOev0CH41nzkGD4Yo5CwHPZFun/WekqUBUXtxR/uH0ThoxV93exTLc\n" +
            "YwWC5GqVZfN38Ye7iDljIFVzxxP3unBy0FItg52407CZyH/gTB+Zm++fZJdKbZcl\n" +
            "UFvxtACEJvdgdM30FHuQlvS53mEXOMAzpJPVZu2gRoTl8cSO3GKxaNP9dmPCzD4a\n" +
            "gO/kVrO/c6DerwWvCJJhifKlDc6CfjM3FfWsVI2gw3WduFPJcIsLxlUqzBh95rA1\n" +
            "R+BTr2n3DV41OK5AwtCQO40CAwEAAQ==\n" +
            "-----END PUBLIC KEY-----\n";

    final static private String licenseHeader = "-----BEGIN MARKDOWN-NAVIGATOR LICENSE-----";
    final static private String licenseFooter = "-----END MARKDOWN-NAVIGATOR LICENSE-----";
    final static private String activationHeader = "-----BEGIN MARKDOWN-NAVIGATOR ACTIVATION-----";
    final static private String activationFooter = "-----END MARKDOWN-NAVIGATOR ACTIVATION-----";
    final static private String altLicenseHeader = "-----BEGIN IDEA-MULTIMARKDOWN LICENSE-----";
    final static private String altLicenseFooter = "-----END IDEA-MULTIMARKDOWN LICENSE-----";
    final static private String altActivationHeader = "-----BEGIN IDEA-MULTIMARKDOWN ACTIVATION-----";
    final static private String altActivationFooter = "-----END IDEA-MULTIMARKDOWN ACTIVATION-----";

    // RELEASE : comment out DEV LICENSE SERVER
    final static public String siteURL = "https://vladsch.com";
    final static public String authSiteURL = "auth.vladsch.com";
    final static public String auth1SiteURL = "vladsch.com";
    final static public String auth2SiteURL = "dev.vladsch.com";

    // DEBUG : debug site and licensing URLs
    //final static public String siteURL = "http://vladsch.dev";
    //final static public String authSiteURL = "vladsch.dev";
    //final static public String auth1SiteURL = authSiteURL;
    //final static public String auth2SiteURL = authSiteURL;

    final static public String diagnosticSiteURL = auth1SiteURL;
    final static public String diagnostic1SiteURL = authSiteURL;
    final static public String diagnostic2SiteURL = auth2SiteURL;

    final static public String productPrefixURL = "/product/markdown-navigator";
    final static public String altProductPrefixURL = "/product/multimarkdown";
    final static public String patchRelease = siteURL + productPrefixURL + "/patch-release";
    final static public String eapRelease = siteURL + productPrefixURL + "/eap-release";
    final static public String altPatchRelease = siteURL + altProductPrefixURL + "/patch-release";
    final static public String altEapRelease = siteURL + altProductPrefixURL + "/eap-release";

    final static private String licenseURL = authSiteURL + productPrefixURL + "/json/license";
    final static private String alt1LicenseURL = auth1SiteURL + productPrefixURL + "/json/license";
    final static private String alt2LicenseURL = auth2SiteURL + productPrefixURL + "/json/license";

    final static public String tryPageURL = siteURL + productPrefixURL + "/try";
    final static public String buyPageURL = siteURL + productPrefixURL + "/buy";
    final static public String specialsPageURL = siteURL + productPrefixURL + "/specials";
    final static public String productPageURL = siteURL + productPrefixURL;
    final static public String referralsPageURL = siteURL + productPrefixURL + "/referrals";

    final static public String feedbackURL = diagnosticSiteURL + productPrefixURL + "/json/diagnostic";
    final static public String feedbackURL1 = diagnostic1SiteURL + productPrefixURL + "/json/diagnostic";
    final static public String statusURL = diagnosticSiteURL + productPrefixURL + "/json/diagnostic-status";
    final static public String statusURL1 = diagnostic1SiteURL + productPrefixURL + "/json/diagnostic-status";

    private static final String ACTIVATION_EXPIRES = "activation_expires";
    private static final String LICENSE_EXPIRES = "license_expires";
    private static final String PRODUCT_VERSION = "product_version";
    private static final String PRODUCT_RELEASED_AT = "product_released_at";
    private static final String AGENT_SIGNATURE = "agent_signature";
    private static final String LICENSE_CODE = "license_code";
    private static final String LICENSE_TYPE = "license_type";
    private static final String LICENSE_FEATURES = "license_features";
    private static final String LICENSE_FEATURE_LIST = "feature_list";
    private static final String ACTIVATION_CODE = "activation_code";
    private static final String HOST_PRODUCT = "host_product";
    private static final String HOST_NAME = "host_name";
    private static final String ACTIVATED_ON = "activated_on";
    private static final String STATUS = "status";
    private static final String MESSAGE = "message";
    private static final String STATUS_DISABLE = "disable";
    private static final String STATUS_OK = "ok";
    private static final String STATUS_ERROR = "error";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final String LICENSE_TYPE_TRIAL = "trial";
    public static final String LICENSE_TYPE_SUBSCRIPTION = "subscription";
    public static final String LICENSE_TYPE_LICENSE = "license";

    private String license_code;
    private String activation_code;
    private JsonObject activation;
    private String license_expires;
    private String product_released_at;
    private String product_version;
    private JsonObject json; // last server response
    private boolean remove_license;
    private String license_type;
    private int license_features;
    private Map<String, Integer> featureList = new HashMap<>();
    private String lastCommunicationsError = null;
    private boolean isSecureConnection = true;

    public Map<String, Integer> getFeatureList() {
        return featureList;
    }

    // 调用默认构造器
    public LicenseAgent(LicenseAgent other) {
        this();
        copyFrom(other);
    }

    public boolean isSecureConnection() {
        return isSecureConnection;
    }

    public void setSecureConnection(boolean secureConnection) {
        isSecureConnection = secureConnection;
    }

    public void copyFrom(LicenseAgent other) {
        this.license_code = other.license_code;
        this.activation_code = other.activation_code;
        this.activation = other.activation;
        this.license_expires = other.license_expires;
        this.product_released_at = other.product_released_at;
        this.product_version = other.product_version;
        this.json = other.json;
        this.remove_license = other.remove_license;
        this.license_type = other.license_type;
        this.license_features = other.license_features;
        this.featureList = other.featureList;
        this.isSecureConnection = other.isSecureConnection;
        this.lastCommunicationsError = other.lastCommunicationsError;
    }

    public boolean isRemoveLicense() {
        return remove_license;
    }

    @NotNull
    public static String getTrialLicenseURL() {
        return tryPageURL;
    }

    // 不设置
    public void setLicenseCode(String license_code) { }

    // 不设置
    public void setLicenseActivationCodes(String license_code, String activation_code) { }

    // 不设置
    public void setActivationCode(String activation_code) { }

    @NotNull
    public static String getLicenseURL() {
        return buyPageURL;
    }

    @NotNull
    public String licenseCode() {
        return license_code != null ? license_code : "";
    }

    // 获取过期时间
    @Nullable
    public String getLicenseExpires() {
        return "2025-01-01";
    }

    @Nullable
    public String getProductVersion() {
        return product_version;
    }

    @NotNull
    public String getMessage() {
        String message;
        return json != null ? ((message = json.getString(MESSAGE)) != null ? message : "") : "";
    }

    @Nullable
    public String getStatus() {
        return json != null ? json.getString(STATUS) : null;
    }

    @NotNull
    public String activationCode() {
        return activation_code != null ? activation_code : "";
    }

    @Nullable
    public JsonObject getActivation() {
        return activation;
    }

    @Nullable
    public String getLastCommunicationsError() {
        return lastCommunicationsError;
    }

    // 添加功能
    public LicenseAgent() {
        featureList = new HashMap<>();
        featureList.put("enhanced", 1);
        featureList.put("development", 2);
    }

    // 获取注册码
    public boolean getLicenseCode(LicenseRequest licenseRequest) {
        return true;
    }

    // 是否有效 LICENSE
    public boolean isValidLicense() {
        return true;
    }

    // 是否有效注册码
    public boolean isValidActivation() {
        return true;
    }

    // LICENSE 类型
    @NotNull
    public String getLicenseType() {
        return LICENSE_TYPE_LICENSE;
    }
    // LICENSE 功能
    public int getLicenseFeatures() {
        return 4;
    }

    // 过期时间
    @NotNull
    public String getLicenseExpiration() {
        return "2025-01-01";
    }

    @NotNull
    public String getHostName() {
        if (activation != null && activation.containsKey(HOST_NAME)) {
            return activation.getString(HOST_NAME);
        }
        return "";
    }

    @NotNull
    public String getHostProduct() {
        if (activation != null && activation.containsKey(HOST_PRODUCT)) {
            return activation.getString(HOST_PRODUCT);
        }
        return "";
    }

    // 注册日期
    @NotNull
    public String getActivatedOn() {
        if (activation != null && activation.containsKey(ACTIVATED_ON)) {
            return activation.getString(ACTIVATED_ON);
        }
        return "2019-01-01";
    }

    // 剩余天数
    public int getLicenseExpiringIn() {
        // see if the license expiration is more than i days away
        return 365*5;
    }

    public boolean getProductIsPerpetual() {
        // see if the license is valid because product is perpetually licensed
        return getLicenseExpiringIn() <= 0;
    }

    public static int floorDiv(int var0, int var1) {
        int var2 = var0 / var1;
        if ((var0 ^ var1) < 0 && var2 * var1 != var0) {
            --var2;
        }

        return var2;
    }

    public static long floorDiv(long var0, long var2) {
        long var4 = var0 / var2;
        if ((var0 ^ var2) < 0L && var4 * var2 != var0) {
            --var4;
        }

        return var4;
    }

    // 是否已过期
    public boolean isActivationExpired() {
        // see if the activation has expired
        return false;
    }

    // 执行获取 class 文件
    public static void main(String[] args) {
        System.out.println();
    }
}
