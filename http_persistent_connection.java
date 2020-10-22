import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HttpClientTest {

    private static int socketTimeout = 300000;// 请求超时时间
    private static int connectTimeout = 300000;// 传输超时时间
    private static final RequestConfig config = RequestConfig.custom().setConnectTimeout(30000).setSocketTimeout(30000).build();

    public static void main(String[] args) throws IOException {
        Map map = new LinkedHashMap();
        map.put("token", "ce63d525-940e-484f-be9c-2ebb51904a5a");
        String res = "";
        try {
            res = postTest(map, "https://194.100.2.60:1181/AlarmServer/resource/handle/pushAlarmToClient", "GBK", "GBK");
            System.out.println(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //https策略，绕过安全检查
    private static CloseableHttpClient getSingleSSLConnection()
            throws Exception {
        //CloseableHttpClient httpClient = null;
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString) throws CertificateException {
                    return true;
                }
            }).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            //return HttpClients.custom().setDefaultRequestConfig(config).build();
            return HttpClients.custom().setSSLSocketFactory(sslsf).setDefaultRequestConfig(config).build();
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * HTTP Post 获取内容
     *
     * @param params     请求的参数，key-value形式
     * @param url        请求的url地址 ?之前的地址
     * @param reqCharset 编码格式
     * @param resCharset 编码格式
     * @return 页面内容
     */
    public static String postTest(Map<String, String> params, String url,  String reqCharset, String resCharset) throws Exception {
        //获取绕过安全检查的httpClient，以便发送https请求//实际项目不建议，会有安全问题
        CloseableHttpClient httpClient = getSingleSSLConnection();
        CloseableHttpResponse response = null;

        try {
            //创建httppost方法
            HttpPost httpPost = new HttpPost(url);
            //添加head，需要什么填什么
            httpPost.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)");
            httpPost.addHeader("Content-type", "text/html,application/xhtml+xml,application/xml");

            //组装请求参数，key-value形式的
            List<NameValuePair> pairs = null;
            if (params != null && !params.isEmpty()) {
                pairs = new ArrayList<NameValuePair>(params.size());
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    String value = entry.getValue();
                    if (value != null) {
                        pairs.add(new BasicNameValuePair(entry.getKey(), value));
                    }
                }
            }
            if (pairs != null && pairs.size() > 0) {
                httpPost.setEntity(new UrlEncodedFormEntity(pairs, reqCharset));
            }

            //这个是直接post一串字符串的方式，如json串，并不用带key，也可以是xml
			/*StringEntity reqEntity = new StringEntity(reqMsg, "GBK");//解决中文乱码问题
			reqEntity.setContentEncoding("GBK");
			reqEntity.setContentType("application/json");
			httpPost.setEntity(reqEntity);*/

            HttpEntity entity = null;
            String result = null;
            //执行post方法
            response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {//出现链接异常，抛出
                httpPost.abort();
                throw new Exception("HttpClient,error status code :" + statusCode);
            }
            //获得返回结果
            entity = response.getEntity();
            if (entity != null) {
                //返回结果转为字符串
                result = EntityUtils.toString(entity, resCharset);
            }
            EntityUtils.consume(entity);
            response.close();
            return result;
        } catch (Exception e) {
            throw e;
        } finally {
            if (response != null)
                try {
                    response.close();
                } catch (IOException e) {
                }
        }
    }
}
