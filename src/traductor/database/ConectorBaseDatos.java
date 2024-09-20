package traductor.database;

import ds.desktop.notify.DesktopNotify;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConectorBaseDatos 
{
    private static final ConectorBaseDatos instancia = new ConectorBaseDatos();
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:xe";
    private static final String USER = "traductor";
    private static final String PASSWORD = "traductor";
    private Connection conexion;

    public ConectorBaseDatos()
    {
        conexion = establecerConexion();
    }

    public static ConectorBaseDatos obtenerInstancia()
    {
        return instancia;
    }
    
    public Connection getConexion()
    {
        return conexion;
    }
    
    private Connection establecerConexion()
    {
        try 
        {
            conexion = DriverManager.getConnection(URL, USER, PASSWORD);
        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
        }
        return conexion;
    }

    public void cerrarConexion() 
    {
        try 
        {
            if (conexion != null && !conexion.isClosed()) 
            {
                conexion.close();
            }
        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
        }
    }
}
