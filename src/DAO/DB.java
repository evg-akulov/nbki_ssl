/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.logging.Level;
import main.Nbki;
import static main.Nbki.log;
import main.util1;

public class DB {

    private String connectionString;
    private String userCft;
    private String passwordCft;

    public final static String SQL_GET_REQUEST = "{ call ? := BIB_NBKI_GET_REQ() }";
    public final static String SQL_SET_RESPONSE = "{ call ? := BIB_NBKI_SET_RES(?,?) }";
    public final static String SQL_SET_RESPONSE_ERROR = "{ call ? := BIB_NBKI_SET_RES_ERR(?,?) }";

    /**
     *
     * @param connectionString
     * @param userCft
     * @param passwordCft
     */
    public DB(String connectionString, String userCft, String passwordCft) {
        this.connectionString = connectionString;
        this.userCft = userCft;
        this.passwordCft = passwordCft;
    }

    /**
     * exec function in oracle with parameters
     *
     * @param SQL
     * @return ResultSet
     */
    public String ExecFuncPS(String SQL, String[] param) {

        Connection connection = null;
        CallableStatement statement = null;
        String result = null;

        try {

            System.setProperty("java.security.egd", "file:///dev/urandom"); //http://blockdump.blogspot.ru/2012/07/connection-problems-inbound-connection.html

            java.sql.DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
            connection = java.sql.DriverManager.getConnection(this.connectionString, this.userCft, this.passwordCft);

            statement = connection.prepareCall(SQL);

            statement.registerOutParameter(1, Types.VARCHAR);
            for (int i = 0; i < param.length; i++) {
                statement.setString(i + 2, param[i]);
            }

            statement.execute();

            result = statement.getString(1);

        } catch (SQLException e) {
            result = e.getMessage();
            log.log(Level.WARNING, "ExecFuncPS " + e.toString());
            util1.createFlagFile(Nbki.workDir, Nbki.typeFlag.SQL_ERROR);

        } finally {

            try {

                statement.close();
                connection.close();

            } catch (SQLException ex) {
                result = ex.getMessage();
                log.log(Level.WARNING, "close connect " + ex.toString());
                util1.createFlagFile(Nbki.workDir, Nbki.typeFlag.SQL_ERROR);

            }

        }
        return result;
    }

}
