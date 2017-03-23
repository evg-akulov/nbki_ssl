package main;

import DAO.DB;
import DAO.serviceDAO;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.net.ssl.HttpsURLConnection;
import ru.CryptoPro.ssl.SSLSocketFactoryImpl;

/**
 *
 * @author evgeniy
 */
public class Nbki {

    private static serviceDAO serviceDAO;

    public final static Logger log = Logger.getLogger(Nbki.class.getName());

    private static String connectionString;
    private static String userCft;
    private static String passwordCft;
    public static String workDir;
    private static String uri_nbki;
    private static String log_path;
    private static String log_level;

    /**
     *
     */
    public enum typeFlag {
        SQL_ERROR, NBKI_ERROR
    }

    /**
     * "jdbc:oracle:thin:@172.25.1.52:1521:BIB_CFT" "ibs" "ibs1223"
     * "/home/evgeniy/NetBeansProjects/nbki_ssl"
     * "https://icrs.demo.nbki.ru/score"
     * "/home/evgeniy/NetBeansProjects/nbki_ssl/logi.txt"
     *
     * @param args
     */
    public static void main(String[] args) {

        connectionString = args[0];      // connectionString
        userCft = args[1];               // userCft
        passwordCft = args[2];           // passwordCft
        workDir = args[3];               // workDir            
        uri_nbki = args[4];              // uri_nbki            
        log_path = args[5];              // log_path
        log_level = args[6];             // log_level

        /**
         * Loger
         */
        FileHandler fh;
        try {

            fh = new FileHandler(log_path, true);
            fh.setFormatter(new SimpleFormatter());
            fh.setEncoding("UTF-8");
            log.addHandler(fh);
            log.setLevel(Level.ALL);
            if ("debug".equals(log_level)) {
                log.log(Level.INFO, "new Nbki class");
                log.log(Level.INFO, "connectionString: " + connectionString);
                log.log(Level.INFO, "userCft: " + userCft);
                log.log(Level.INFO, "workDir: " + workDir);
                log.log(Level.INFO, "uri_nbki: " + uri_nbki);
                log.log(Level.INFO, "log_path: " + log_path);
                log.log(Level.INFO, "log_level: " + log_level);
            }

        } catch (IOException e) {

            log.log(Level.WARNING, e.getMessage());

        }


        /* enable gostSSL */
//            System.setProperty("javax.net.ssl.supportGVO", "true");
        System.setProperty("javax.net.ssl.trustStoreType", "CertStore");
        System.setProperty("javax.net.ssl.trustStore", "C:\\.keystore");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456");

        final SSLSocketFactoryImpl sslFact = new SSLSocketFactoryImpl();
        HttpsURLConnection.setDefaultSSLSocketFactory(sslFact);

        /*              */
        serviceDAO = new serviceDAO(new DB(connectionString, userCft, passwordCft));

        String id_req = "";

        log.log(Level.INFO, " ====================================== start ======================================");

        /**
         * from cft
         */
        String res = serviceDAO.getRequest();

        /**
         * if request not null then action else not
         */
        if (res != null && !"".equals(res)) {

            int beg = res.indexOf("<InquiryID>");
            int end = res.indexOf("</InquiryID>");

            if (beg != -1 && end != -1) {
                id_req = res.substring(beg + 11, end);
                log.log(Level.INFO, "id_req: " + id_req);

                /*
                        delete id request                
                 */
                res = res.substring(0, beg);
            }

            if ("debug".equals(log_level)) {
                /*  write request to file */
                try (FileOutputStream fos = new FileOutputStream(workDir + "\\request_" + new Date().getTime() + ".xml")) {
                    fos.write(res.getBytes(Charset.forName("cp1251")));
                    fos.flush();
                } catch (IOException ex) {
                    log.log(Level.WARNING, "write request to file " + ex.getMessage());
                }

            }

            /*
                    send request to nbki
             */
            InputStream response_1 = null;
            HttpsURLConnection connection = null;
            int code = 0;

            try {

                byte[] postData = res.getBytes(Charset.forName("cp1251"));
                int postDataLength = postData.length;
                String request_url = uri_nbki;
                URL url = new URL(request_url);
                connection = (HttpsURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setInstanceFollowRedirects(false);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/xml");
                connection.setRequestProperty("charset", "cp1251");
                connection.setRequestProperty("Content-Length", Integer.toString(postDataLength));
                connection.setUseCaches(false);

                try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                    wr.write(postData);
                }

                connection.connect();
                code = connection.getResponseCode();
                log.log(Level.INFO, "HTTP code " + code);

                /**
                 * answer to cft
                 */
                if (code == 200) {

                    response_1 = connection.getInputStream();

                    if (response_1 != null & !"".equals(response_1)) {

                        String response_2 = "";

                        /*  
                            read from response
                         */
                        StringBuilder sb = new StringBuilder();
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(response_1, Charset.forName("cp1251")));) {
                            //BufferedReader br = new BufferedReader(new InputStreamReader(response_1));    // validation !!!
                            String strLine;
                            while ((strLine = br.readLine()) != null) {
                                if (strLine.indexOf("<") != -1) {
                                    sb.append(strLine);
                                }
                            }
                            response_2 = sb.toString();

                            if ("debug".equals(log_level)) {
                                /*  write response from base64 to file */
                                try (FileOutputStream fos = new FileOutputStream(workDir + "\\response_" + new Date().getTime() + ".xml")) {
                                    fos.write(response_2.getBytes(Charset.forName("cp1251")));
                                    fos.flush();
                                } catch (IOException ex) {
                                    log.log(Level.WARNING, "decoded " + ex.getMessage());
                                }
                            }

                        } catch (IOException ex) {
                            log.log(Level.WARNING, "response read from response " + ex.getMessage());
                            String res3 = serviceDAO.setResponseError(id_req, ex.getMessage());
                            log.log(Level.WARNING, "setResponseError: " + res3);
                        }

                        beg = response_2.indexOf("<score>");
                        end = response_2.indexOf("</score>");

                        /*
                            1) ответ от НБКИ с кодом GetScoringOne(код)
                            2) ответ от НБКИ с логической ошибкой GetErrorText()
                            3) ответ от НБКИ с с пустым кодом (нет кредитной истории)  GetScoringOne( -1 )
                            4) техн. ошибка от НБКИ GetErrorText( описание техн. ошибки)   не отрабатывает                     
                         */
                        /**
                         * only score_code to cft
                         */
                        if (beg != -1 && end != -1) {

                            String score_code = response_2.substring(beg + 7, end);
                            log.log(Level.INFO, "score_code " + score_code);

                            String res2 = serviceDAO.setResponse(id_req, score_code);
                            log.log(Level.INFO, "setResponse: " + res2);

                            /**
                             * logical error text
                             */
                        } else {

                            int beg_err = response_2.indexOf("<err>");
                            int end_err = response_2.indexOf("</err>");

                            if (beg_err != -1 && end_err != -1) {

                                String error_text = response_2.substring(beg_err, end_err);

                                int beg_code = error_text.indexOf("<Code>");
                                int end_code = error_text.indexOf("</Code>");

                                if (beg_code != -1 && end_code != -1) {

                                    String error_code = error_text.substring(beg_code + 6, end_code);
                                    log.log(Level.INFO, "error_code " + error_code);

                                    int beg_text = error_text.indexOf("<Text>");
                                    int end_text = error_text.indexOf("</Text>");

                                    if (beg_text != -1 && end_text != -1) {

                                        String error_message = error_text.substring(beg_text + 6, end_text);
                                        log.log(Level.INFO, "error_message " + error_message);

                                        String res3 = serviceDAO.setResponseError(id_req, error_code + " " + error_message);
                                        log.log(Level.INFO, "setResponseError: " + res3);

                                    }
                                }

                                /**
                                 * -1 score_code to cft
                                 */
                            } else {

                                log.log(Level.INFO, "score_code -1");
                                String res2 = serviceDAO.setResponse(id_req, "-1");
                                log.log(Level.INFO, "setResponse: " + res2);

                            }
                        }
                        /* error text */
                    }
                    /* if (!"".equals(response_1)) { */
                }
                /* if (code == 200) { */

            } catch (Exception ex) {
                /* tech error */

                log.log(Level.WARNING, "response gl " + ex.getMessage());
                String res3 = serviceDAO.setResponseError(id_req, ex.getMessage());
                log.log(Level.WARNING, "setResponseError: " + res3);

            } finally {

                connection.disconnect();
                log.log(Level.INFO, "client.close");

            }

        }
        /* if (res != null && !"".equals(res)) { */

        log.log(Level.INFO, " ====================================== end ======================================");

    }
}
