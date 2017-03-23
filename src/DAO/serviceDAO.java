/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

public class serviceDAO {

    private DB db;

    /**
     *
     * @param db
     */
    public serviceDAO(DB db) {
        this.db = db;
    }

    /**
     * Получить данные по запросу
     *
     * @return
     */
    public String getRequest() {
        String[] param = new String[0];
        String res = "";

        res = db.ExecFuncPS(db.SQL_GET_REQUEST, param);

        return res;
    }

    /**
     * Передать данные по ответу
     *
     * @param id_request
     * @param code
     * @return
     */
    public String setResponse(String id_request, String code) {
        String[] param = new String[2];
        String res = "";

        if (!id_request.equals("") & !code.equals("")) {
            param[0] = id_request.trim();
            param[1] = code.trim();
            res = db.ExecFuncPS(db.SQL_SET_RESPONSE, param);
        }
        return res;
    }

    /**
     * Передать ошибку по ответу
     *
     * @param id_request
     * @param code
     * @return
     */
    public String setResponseError(String id_request, String code) {
        String[] param = new String[2];
        String res = "";

        if (!id_request.equals("") & !code.equals("")) {
            param[0] = id_request.trim();
            param[1] = code.trim();
            res = db.ExecFuncPS(db.SQL_SET_RESPONSE_ERROR, param);
        }
        return res;
    }

}
