package traductor.util;

import ds.desktop.notify.DesktopNotify;
import java.io.File;
import java.util.Properties;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import traductor.database.ConectorBaseDatos;

public class GestionSesion
{
    private static final String ARCHIVO_SESION = "sesion.properties";
    private ConectorBaseDatos conexion = new ConectorBaseDatos();
    private static Properties properties;

    public static boolean cargarArchivoSesion()
    {
        File archivo = new File(ARCHIVO_SESION);
        properties = new Properties();
        boolean datosVacios = false;
        
        if (!archivo.exists())
        {
            Properties properties = new Properties();
            properties.setProperty("usuario", "");
            properties.setProperty("contrasena", "");
            boolean recuerdame = Boolean.parseBoolean(properties.getProperty("recuerdame", "false").trim());

            try (FileOutputStream fileOutputStream = new FileOutputStream(archivo))
            {
                properties.store(fileOutputStream, "Configuración de sesión");
                DesktopNotify.showDesktopMessage("Exito", "Datos de sesion creados correctamente.", DesktopNotify.SUCCESS, 5000);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            try (FileInputStream fileInputStream = new FileInputStream(archivo))
            {
                Properties properties = new Properties();
                properties.load(fileInputStream);
                String usuario = properties.getProperty("usuario", "").trim();
                String contrasena = properties.getProperty("contrasena", "").trim();
                datosVacios = usuario.isEmpty() || contrasena.isEmpty();
                if (datosVacios)
                {
                    DesktopNotify.showDesktopMessage("Advertencia", "No hay sesiones activas.", DesktopNotify.WARNING, 5000);
                }
                else
                {
                    DesktopNotify.showDesktopMessage("Exito", "Datos de sesion cargados correctamente.", DesktopNotify.SUCCESS, 5000);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return datosVacios;
    }
    
    public static void guardarCredenciales(String usuario, String contrasena, boolean recuerdame)
    {
        Properties properties = new Properties();
        properties.setProperty("usuario", usuario);
        properties.setProperty("contrasena", contrasena);
        if (recuerdame)
        {
            properties.setProperty("recuerdame", "true");
        }
        else
        {
            properties.remove("recuerdame");
        }

        try (FileOutputStream fos = new FileOutputStream(ARCHIVO_SESION))
        {
            properties.store(fos, "Datos de inicio de sesión");
            DesktopNotify.showDesktopMessage("Exito", "Datos de sesion guardados.", DesktopNotify.SUCCESS, 5000);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static String[] cargarCredenciales()
    {
        Properties properties = new Properties();
        String[] credenciales = new String[2];

        try (FileInputStream fis = new FileInputStream(ARCHIVO_SESION))
        {
            properties.load(fis);
            credenciales[0] = properties.getProperty("usuario");
            credenciales[1] = properties.getProperty("contrasena");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return credenciales;
    }
    
    public static String obtenerUsuarioDesdeSesion()
    {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(ARCHIVO_SESION))
        {
            properties.load(fis);
            return properties.getProperty("usuario");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean obtenerRecuerdame()
    {
        return Boolean.parseBoolean(properties.getProperty("recuerdame", "false"));
    }
    
    public void cerrarSesion()
    {
        limpiarSesionProperties();
        conexion.cerrarConexion();
    }

    private void limpiarSesionProperties()
    {
        try (FileOutputStream fos = new FileOutputStream("sesion.properties"))
        {
            Properties props = new Properties();
            props.store(fos, null);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
