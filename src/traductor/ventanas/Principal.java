package traductor.ventanas;

import ds.desktop.notify.DesktopNotify;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.math.BigDecimal;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import traductor.database.ConectorBaseDatos;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import traductor.util.GestionSesion;

public final class Principal extends javax.swing.JFrame 
{    
    private final ConectorBaseDatos conector;
    private final Connection conexion;
    
    private static String USUARIO;
    private static int ID_USUARIO;
    private static String TABLA_GUARDADOS_SQL;
    private static String TABLA_HISTORIAL_SQL;
    
    private final CardLayout CARD_LAYOUT;
    private final Color COLOR_GRIS = new Color(82, 81, 81);
    private final Color COLOR_NARANJA = new Color(244, 107, 82);
    private final int TIEMPO_NOTIFICACION = 5000;
    private static final Map<String, String> BANDERAS = new HashMap<>();
    
    private static Queue<String> colaDeMensajes = new LinkedList<>();
    private static Queue<String> colaDeMensajesLogin = new LinkedList<>();
    private static Queue<String> colaDeMensajesRegistro = new LinkedList<>();
    
    private final GestionSesion objetoSesion = new GestionSesion();
    
    public Principal()
    {
        initComponents();
        
        conector = ConectorBaseDatos.obtenerInstancia();
        if (conector != null) {
            System.out.println("Conector obtenido exitosamente");
        } else {
            System.out.println("Error al obtener el conector");
        }

        conexion = conector.getConexion();
        if (conexion != null) {
            System.out.println("Conexión establecida exitosamente");
        } else {
            System.out.println("Error al establecer la conexión");
        }
        
        notificarConexion();
        protocoloInicial();
        
        CARD_LAYOUT = new CardLayout();
        contenedorDeSecciones.setLayout(CARD_LAYOUT);
        contenedorDeSecciones.add(contenedorTraduccion, "traducir");
        contenedorDeSecciones.add(contenedorCuenta, "cuenta");
        contenedorDeSecciones.add(contenedorStaffPanel, "staff_panel");
        contenedorDeSecciones.add(contenedorStaff, "staff");
        contenedorDeSecciones.add(contenedorPalabras, "palabras");
        contenedorDeSecciones.add(contenedorSugerenciasPersonales, "sugerencias_personales");
        contenedorDeSecciones.add(contenedorSugerencias, "lienzo_sugerencias");
        contenedorDeSecciones.add(contenedorHistorial, "historial");
        contenedorDeSecciones.add(contenedorGuardados, "guardados");
        
        lienzoSugerencias.setLayout(CARD_LAYOUT);
        lienzoSugerencias.add(contenedorListasSugerencias, "sugerencias");
        lienzoSugerencias.add(contenedorSugerir, "sugerir");
        
        contenedorPrincipal.setLayout(CARD_LAYOUT);
        contenedorPrincipal.add(contenedorLogin, "login");
        contenedorPrincipal.add(contenedorRegistro, "signup");
        
        setIconImage(getIconoAplicacion());
        setDefaultCloseOperation(Principal.EXIT_ON_CLOSE);
        setDefaultCloseOperation(Principal.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        pack();
        
        Sesion.setIconImage(getIconoAplicacion());
        //Sesion.setDefaultCloseOperation(Sesion.EXIT_ON_CLOSE);
        Sesion.setDefaultCloseOperation(Sesion.DO_NOTHING_ON_CLOSE);
        Sesion.setResizable(false);
        Sesion.setLocationRelativeTo(null);
        Sesion.pack();
        Sesion.setSize(500, 750);
        
        mensajeAccion.setVisible(false);
        etiquetaMensajeErrorLogin.setVisible(false);
        etiquetaMensajeErrorRegistro.setVisible(false);
        
        botonCancelarCambios.setVisible(false);
        tituloCancelarCambios.setVisible(false);
        botonConfirmarCambios.setVisible(false);
        tituloConfirmarCambios.setVisible(false);
        
        // Tamaños Ventana Traductor
        contenedorMensajes.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        lienzo.setPreferredSize(new Dimension(907, 400));
        lienzo.setSize(new Dimension(907, 400));
        contenedorHeader.setPreferredSize(new Dimension(907, 66));
        contenedorHeader.setSize(new Dimension(907, 66));
        contenedorTraduccion.setPreferredSize(new Dimension(907, 308));
        contenedorTraduccion.setSize(new Dimension(907, 308));
        cuadroInput.setPreferredSize(new Dimension(350, 225));
        cuadroInput.setSize(new Dimension(350, 225));
        cuadroOutput.setPreferredSize(new Dimension(350, 225));
        cuadroOutput.setSize(new Dimension(350, 225));
        contenedorFooter.setPreferredSize(new Dimension(907, 25));
        contenedorFooter.setSize(new Dimension(907, 25));
        mensajeAccion.setPreferredSize(new Dimension(126, 15));
        mensajeAccion.setSize(new Dimension(126, 15));
        contenedorMensajes.setPreferredSize(new Dimension (907, 18));
        contenedorMensajes.setSize(new Dimension (907, 18));
        
        panelInformacionLogin.setPreferredSize(new Dimension(458, 390));
        panelInformacionRegistro.setPreferredSize(new Dimension(458, 390));
        etiquetaMensajeErrorLogin.setPreferredSize(new Dimension(72, 16));
        etiquetaMensajeErrorRegistro.setPreferredSize(new Dimension(72, 16));
        
        textAreaInput.setLineWrap(true);
        textAreaInput.setWrapStyleWord(true);
        textAreaInput.setColumns(5);
        textAreaOutput.setLineWrap(true);
        textAreaOutput.setWrapStyleWord(true);
        textAreaOutput.setColumns(5);
        
        ((AbstractDocument) textAreaInput.getDocument()).setDocumentFilter(new DocumentSizeFilter(200));
        ((AbstractDocument) textAreaOutput.getDocument()).setDocumentFilter(new DocumentSizeFilter(200));
        
        textAreaInput.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                updateCharacterCount();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e)
            {
                updateCharacterCount();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e)
            {
                updateCharacterCount();
            }
            
            private void updateCharacterCount()
            {
                int caracteresActuales = textAreaInput.getText().length();
                cantidadCaracteresActuales.setText(caracteresActuales + "/200");
            }
        });
    }
    
    private void cerrarVentanaPrincipal()
    {
        boolean recuerdame = objetoSesion.obtenerRecuerdame();

        if (recuerdame)
        {
            return;
        }

        objetoSesion.cerrarSesion();
        vaciarColasMensajes();

        textFieldNombreDatos.setText("");
        textFieldUsuarioDatos.setText("");
        comboBoxLenguas.setSelectedIndex(0);

        cantidadGuardados.setText("%X%");
        cantidadSugerencias.setText("%X%");
        cantidadSanciones.setText("%X%");
        cantidadBusquedas.setText("%X%");
        this.dispose();
        System.exit(0);
    }
    
    public void notificarConexion()
    {
        if (conexion != null)
        {
            DesktopNotify.showDesktopMessage("Exito", "Conexión exitosa a la base de datos.", DesktopNotify.SUCCESS, TIEMPO_NOTIFICACION);
            //conector.cerrarConexion();
        }
        else
        {
            DesktopNotify.showDesktopMessage("Error", "No se pudo establecer conexion a la base de datos.", DesktopNotify.ERROR, TIEMPO_NOTIFICACION);
        }
    }
    
    public static Image getIconoAplicacion()
    {
        return Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("resources/logo.png"));
    }
    
    static class DocumentSizeFilter extends DocumentFilter 
    {
        private int caracteresMaximos;
        
        public DocumentSizeFilter(int maxCars) 
        {
            caracteresMaximos = maxCars;
        }
        
        @Override
        public void insertString(FilterBypass fb, int offs, String str, AttributeSet a) throws BadLocationException 
        {
            if ((fb.getDocument().getLength() + str.length()) <= caracteresMaximos) 
            {
                super.insertString(fb, offs, str, a);
            } 
            else 
            {
                // No insertar si supera el límite
            }
        }
        
        @Override
        public void replace(FilterBypass fb, int offs, int length, String str, AttributeSet a) throws BadLocationException 
        {
            if ((fb.getDocument().getLength() + str.length() - length) <= caracteresMaximos) 
            {
                super.replace(fb, offs, length, str, a);
            } 
            else 
            {
                // No reemplazar si supera el límite
            }
        }
    }
    
    private void cambiarIconoBandera(JComboBox cajaIdiomas, JLabel ubicacionIcono)
    {
        JComboBox CAJA_IDIOMAS = cajaIdiomas;
        JLabel UBICACION_ICONO = ubicacionIcono;
        
        BANDERAS.put("Español", "/resources/espanol.png");
        BANDERAS.put("Ingles", "/resources/ingles.png");
        BANDERAS.put("Italiano", "/resources/italiano.png");
        BANDERAS.put("Idioma", "/resources/global.png");
        
        String idiomaSeleccionado = (String) CAJA_IDIOMAS.getSelectedItem();
        String RUTA_ICONO = BANDERAS.get(idiomaSeleccionado);
        
        if (RUTA_ICONO != null) 
        {
            ImageIcon ICONO = new ImageIcon(getClass().getResource(RUTA_ICONO));
            UBICACION_ICONO.setIcon(ICONO);
        }
        else
        {
            String iconPath = BANDERAS.get("Idioma");
            ImageIcon icono = new ImageIcon(getClass().getResource(iconPath));
            UBICACION_ICONO.setIcon(icono);
        }
    }
    
    private static void copiarAPortapapeles(String texto) 
    {
        StringSelection seleccion = new StringSelection(texto);
        Clipboard portaPapeles = Toolkit.getDefaultToolkit().getSystemClipboard();
        portaPapeles.setContents(seleccion, null);
    }
    
    private void copiarTexto(JTextArea cuadroTexto, String mensaje)
    {
        JTextArea CUADRO_TEXTO = cuadroTexto;
        String MENSAJE = mensaje;
        
        if(!CUADRO_TEXTO.getText().equals("No se encontró una traducción para la palabra ingresada") && !CUADRO_TEXTO.getText().isEmpty())
        {
            String textoACopiar = CUADRO_TEXTO.getText();
            copiarAPortapapeles(textoACopiar);
            
            colaDeMensajes.offer(MENSAJE);
            notificarAccionBasica(colaDeMensajes, mensajeAccion);
            DesktopNotify.showDesktopMessage("Exito", MENSAJE, DesktopNotify.SUCCESS, TIEMPO_NOTIFICACION);
        }
        else
        {
            colaDeMensajes.offer("Nada que Copiar");
            notificarAccionBasica(colaDeMensajes, mensajeAccion);
            DesktopNotify.showDesktopMessage("Error", "Nada que copiar.", DesktopNotify.ERROR, TIEMPO_NOTIFICACION);
        }
    }
    
    private void borrarTexto(JTextArea cuadroTexto, String mensaje)
    {
        JTextArea CUADRO_TEXTO = cuadroTexto;
        String MENSAJE = mensaje;
        
        if(!CUADRO_TEXTO.getText().isEmpty())
        {
            CUADRO_TEXTO.setText("");
            colaDeMensajes.offer(MENSAJE);
            notificarAccionBasica(colaDeMensajes, mensajeAccion);
            DesktopNotify.showDesktopMessage("Exito", MENSAJE, DesktopNotify.SUCCESS, TIEMPO_NOTIFICACION);
        }
        else
        {
            colaDeMensajes.offer("Nada que Borrar");
            notificarAccionBasica(colaDeMensajes, mensajeAccion);
            DesktopNotify.showDesktopMessage("Error", "Nada que borrar.", DesktopNotify.ERROR, TIEMPO_NOTIFICACION);
        }
    }
    
    private void notificarAccionBasica(Queue<String> colaMensajes, JLabel etiqueta)
    {
        JLabel ETIQUETA = etiqueta;
        Queue<String> COLA_MENSAJES = colaMensajes;
        
        if(!COLA_MENSAJES.isEmpty() && !ETIQUETA.isVisible())
        {
            String mensaje = COLA_MENSAJES.poll();
            ETIQUETA.setText(mensaje);
            centrarNotificacionBasica();
            ETIQUETA.setVisible(true);
            new Thread(()->
            {
                try 
                {
                    Thread.sleep(1500);
                    SwingUtilities.invokeLater(() -> 
                    {
                        ETIQUETA.setVisible(false);
                        notificarAccionBasica(COLA_MENSAJES, ETIQUETA);
                    });
                } 
                catch (InterruptedException e) 
                {
                    DesktopNotify.showDesktopMessage("Fallo", "No se pudo notificar la accion. (Interrupted Exception)", DesktopNotify.FAIL, TIEMPO_NOTIFICACION);
                }
            }).start();
        }
    }
    
    private void centrarNotificacionBasica()
    {
        mensajeAccion.setHorizontalAlignment(SwingConstants.CENTER);
        mensajeAccion.setVerticalAlignment(SwingConstants.CENTER);
    }
    
    private int obtenerUltimoID(String nombreTabla, String nombreColumnaId) 
    {
        String TABLA = nombreTabla;
        String COLUMNA_ID = nombreColumnaId;
        int ULTIMO_ID = -1;

        try (Connection connection = new ConectorBaseDatos().getConexion()) 
        {
            String sql = "SELECT MAX(ID" + COLUMNA_ID + ") FROM " + TABLA;
            try (PreparedStatement statement = connection.prepareStatement(sql)) 
            {
                ResultSet rs = statement.executeQuery();
                if (rs.next()) 
                {
                    ULTIMO_ID = rs.getInt(1);
                }
            }
        } 
        catch (SQLException e) 
        {
            DesktopNotify.showDesktopMessage("Fallo", "No se pudo buscar el ID de " + TABLA + ". (SQL Exception)", DesktopNotify.FAIL, TIEMPO_NOTIFICACION);
        }

        return ULTIMO_ID;
    }
    
    private int obtenerIDUsuarioActual()
    {
        int ID_CUENTA_ACTIVA = 0;
        String sql = "SELECT idcuenta FROM cuentas WHERE usuario = ?";
        USUARIO = GestionSesion.obtenerUsuarioDesdeSesion();
        
        try (PreparedStatement statement = conexion.prepareStatement(sql))
        {
            statement.setString(1, USUARIO);
            try (ResultSet resultSet = statement.executeQuery())
            {
                if (resultSet.next())
                {
                    ID_CUENTA_ACTIVA = resultSet.getInt("idcuenta");
                }
            }
        }
        catch (SQLException e)
        {
            DesktopNotify.showDesktopMessage("Fallo", "No se pudo buscar el ID de " + USUARIO + ". (SQL Exception)", DesktopNotify.FAIL, TIEMPO_NOTIFICACION);
        }
        return ID_CUENTA_ACTIVA;
    }
    
    private int obtenerIdUsuarioSeleccionado()
    {
        String usuarioSeleccionado = (String) comboBoxUsuarios.getSelectedItem();

        if (usuarioSeleccionado == null || usuarioSeleccionado.isEmpty())
        {
            return -1;
        }
        
        String sql = "SELECT idcuenta FROM cuentas WHERE usuario = ?";
        try (PreparedStatement statement = conexion.prepareStatement(sql))
        {
            statement.setString(1, usuarioSeleccionado);
            try (ResultSet resultSet = statement.executeQuery())
            {
                if (resultSet.next())
                {
                    return resultSet.getInt("idcuenta");
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return -1;
    }
    
    private boolean tablaExistente(String nombreTabla) throws SQLException
    {
        String TABLA = nombreTabla;
        
        DatabaseMetaData metadata = conexion.getMetaData();
        try (ResultSet resultSet = metadata.getTables(null, null, TABLA.toUpperCase(), null))
        {
            return resultSet.next();
        }
    }
    
    private String buscarTraduccion(String textoInput, String idiomaInput, String idiomaOutput)
    {
        String TEXTO_INPUT = textoInput;
        String TEXTO_OUTPUT = "";
        String IDIOMA_INPUT = idiomaInput;
        String IDIOMA_OUTPUT = idiomaOutput;

        try 
        {
            String[] listaPalabras = TEXTO_INPUT.split("\\s+");
            for (String palabraIndividual : listaPalabras)
            {
                String sql = "SELECT Palabra" + IDIOMA_OUTPUT + " FROM Palabras WHERE Palabra" + IDIOMA_INPUT + " = ?";
                System.out.println(sql);
                try (PreparedStatement statement = conexion.prepareStatement(sql))
                {
                    statement.setString(1, palabraIndividual);
                    try (ResultSet rs = statement.executeQuery())
                    {
                        if (rs.next())
                        {
                            TEXTO_OUTPUT += rs.getString(1) + " ";

                            guardarEnHistorial(palabraIndividual, rs.getString(1), IDIOMA_INPUT, IDIOMA_OUTPUT);
                        }
                        else
                        {
                            DesktopNotify.showDesktopMessage("Fallo", "No se encontró una traducción para la palabra ingresada: " + palabraIndividual, DesktopNotify.FAIL, TIEMPO_NOTIFICACION);
                            TEXTO_OUTPUT = "No se encontró una traducción para la palabra ingresada";
                        }
                        rs.close();
                    }
                }
            }
        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
            DesktopNotify.showDesktopMessage("Fallo", "No se pudo realizar la traduccion. (SQL Exception)", DesktopNotify.FAIL, TIEMPO_NOTIFICACION);
        }
        return TEXTO_OUTPUT.trim();
    }
    
    private void cargarDatosTabla(JTable tablaJava, String tablaSQL, String nombreColumnaID) throws SQLException
    {
        JTable TABLA_JAVA = tablaJava;
        String TABLA_SQL = tablaSQL;
        String COLUMNA_ID = nombreColumnaID;
                
        String sql = "SELECT * FROM " + TABLA_SQL + " ORDER BY ID" + COLUMNA_ID;
        
        try (PreparedStatement statement = (PreparedStatement) conexion.prepareStatement(sql))
        {
            try (ResultSet rs = statement.executeQuery())
            {
                DefaultTableModel tableModel = new DefaultTableModel();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                for (int i = 1; i <= columnCount; i++)
                {
                    tableModel.addColumn(metaData.getColumnName(i));
                }

                while (rs.next())
                {
                    Object[] row = new Object[columnCount];
                    for (int i = 1; i <= columnCount; i++)
                    {
                        row[i - 1] = rs.getObject(i);
                    }
                    tableModel.addRow(row);
                }

                rs.close();
                statement.close();

                TABLA_JAVA.setModel(tableModel);
            }
        }
        catch (SQLException e) 
        {
            DesktopNotify.showDesktopMessage("Fallo", "No se han podido recopilar los datos de " + TABLA_SQL + ". (SQL Exception)", DesktopNotify.FAIL, TIEMPO_NOTIFICACION);
        }
    }
    
    private void cargarDatosTablaSugerencias()
    {
        try
        {
            String sql = "SELECT sugerencias.idsugerencia, cuentas.usuario AS nombre_usuario, sugerencias.palabra_original, sugerencias.palabra_traducida, "
                    + "sugerencias.sugerencia, sugerencias.idioma_original, sugerencias.idioma_traduccion, sugerencias.fecha, sugerencias.estatus "
                    + "FROM sugerencias "
                    + "JOIN cuentas ON sugerencias.idusuario = cuentas.idcuenta "
                    + "ORDER BY IDSUGERENCIA";

            try (PreparedStatement statement = (PreparedStatement) conexion.prepareStatement(sql); ResultSet rs = statement.executeQuery();)
            {
                DefaultTableModel tableModel = new DefaultTableModel();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                for (int i = 1; i <= columnCount; i++)
                {
                    if (!metaData.getColumnName(i).equals("idusuario"))
                    {
                        tableModel.addColumn(metaData.getColumnName(i));
                    }
                }

                while (rs.next())
                {
                    Object[] row = new Object[tableModel.getColumnCount()];
                    int index = 0;
                    for (int i = 1; i <= columnCount; i++)
                    {
                        if (!metaData.getColumnName(i).equals("idusuario"))
                        {
                            if (metaData.getColumnName(i).equals("nombre_usuario"))
                            {
                                row[index] = rs.getString("nombre_usuario");
                            }
                            else
                            {
                                row[index] = rs.getObject(i);
                            }
                            index++;
                        }
                    }
                    tableModel.addRow(row);
                }

                rs.close();
                statement.close();
                conector.cerrarConexion();

                tablaSugerencias.setModel(tableModel);
                DesktopNotify.showDesktopMessage("Exito", "Recopiladas las sugerencias correctamente.", DesktopNotify.SUCCESS, TIEMPO_NOTIFICACION);
            }
        }
        catch (SQLException e)
        {
            DesktopNotify.showDesktopMessage("Fallo", "No se han podido recopilar las sugerencias. (SQL Exception)", DesktopNotify.FAIL, TIEMPO_NOTIFICACION);
        }
    }
    
    private void insertarEnGuardados(String textoInput, String textoOutput, String idiomaInput, String idiomaOutput) 
    {
        String TEXTO_INPUT = textoInput;
        String TEXTO_OUTPUT = textoOutput;
        String IDIOMA_INPUT = idiomaInput;
        String IDIOMA_OUTPUT = idiomaOutput;
        TABLA_GUARDADOS_SQL = "GUARDADOS_" + USUARIO.toUpperCase();
        
        int ULTIMO_ID = obtenerUltimoID(TABLA_GUARDADOS_SQL , "GUARDADO");
        int NUEVO_ID = ULTIMO_ID + 1;

        String sql = "INSERT INTO " + TABLA_GUARDADOS_SQL + " (IDGUARDADO, TEXTOORIGINAL, TEXTOTRADUCIDO, IDIOMAORIGEN, IDIOMADESTINO) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement statement = (PreparedStatement) conexion.prepareStatement(sql))
        {
            statement.setInt(1, NUEVO_ID);
            statement.setString(2, TEXTO_INPUT);
            statement.setString(3, TEXTO_OUTPUT);
            statement.setString(4, IDIOMA_INPUT);
            statement.setString(5, IDIOMA_OUTPUT);
            statement.executeUpdate();
            
            DesktopNotify.showDesktopMessage("Exito", "Traduccion guardada exitosamente.", DesktopNotify.SUCCESS, TIEMPO_NOTIFICACION);
            colaDeMensajes.offer("Traduccion Guardada");
            notificarAccionBasica(colaDeMensajes, mensajeAccion);
        }
        catch (SQLException e) 
        {
            DesktopNotify.showDesktopMessage("Fallo", "No se ha podido guardar la traduccion. (SQL Exception)", DesktopNotify.FAIL, TIEMPO_NOTIFICACION);
        }
    }
    
    private void guardarEnHistorial(String palabraInput, String palabraOutput, String idiomaInput, String idiomaOutput)
    {
        String PALABRA_INPUT = palabraInput;
        String PALABRA_OUTPUT = palabraOutput;
        String IDIOMA_INPUT = idiomaInput;
        String IDIOMA_OUTPUT = idiomaOutput;
        TABLA_HISTORIAL_SQL = "HISTORIAL_" + USUARIO.toUpperCase();
        
        try
        {
            int ultimoID = obtenerUltimoID(TABLA_HISTORIAL_SQL, "TRADUCCION");
            int nuevoID = ultimoID + 1;

            String sql = "INSERT INTO " + TABLA_HISTORIAL_SQL + " (IDTRADUCCION, TEXTOORIGINAL, TEXTOTRADUCIDO, IDIOMAORIGEN, IDIOMADESTINO, FECHA) VALUES (?, ?, ?, ?, ?, SYSDATE)";
            System.out.println(sql);
            try (PreparedStatement statement = conexion.prepareStatement(sql))
            {
                statement.setInt(1, nuevoID);
                statement.setString(2, PALABRA_INPUT);
                statement.setString(3, PALABRA_OUTPUT);
                statement.setString(4, IDIOMA_INPUT);
                statement.setString(5, IDIOMA_OUTPUT);
                statement.executeUpdate();
            }
        }
        catch(SQLException e)
        {
            DesktopNotify.showDesktopMessage("Fallo", "No se ha podido almacenar en busquedas la traduccion. (SQL Exception)", DesktopNotify.FAIL, TIEMPO_NOTIFICACION);
        }
    }
    
    private void crearTablaGuardados() throws SQLException
    {
        TABLA_GUARDADOS_SQL = "GUARDADOS_" + USUARIO.toUpperCase();
        String sql = "CREATE TABLE " + TABLA_GUARDADOS_SQL + " (IDGUARDADO NUMBER PRIMARY KEY, TEXTOORIGINAL VARCHAR(255),"
                + " TEXTOTRADUCIDO VARCHAR(255), IDIOMAORIGEN VARCHAR(50), IDIOMADESTINO VARCHAR(50))";
        System.out.println(sql);
        
        try (PreparedStatement statement = conexion.prepareStatement(sql))
        {
            statement.executeUpdate();
            DesktopNotify.showDesktopMessage("Exito", "Tabla guardados creada exitosamente.", DesktopNotify.SUCCESS, TIEMPO_NOTIFICACION);
        }
        catch(SQLException e)
        {
            System.out.println(e);
            DesktopNotify.showDesktopMessage("Fallo", "No se ha podido crear la tabla guardados para el usuario. (SQL Exception)", DesktopNotify.FAIL, TIEMPO_NOTIFICACION);
        }
    }
    
    private void crearTablaHistorial() throws SQLException
    {
        TABLA_HISTORIAL_SQL = "HISTORIAL_" + USUARIO.toUpperCase();
        String sql = "CREATE TABLE " + TABLA_HISTORIAL_SQL + " (IDTRADUCCION INT PRIMARY KEY, TEXTOORIGINAL VARCHAR(255),"
                + " TEXTOTRADUCIDO VARCHAR(255), IDIOMAORIGEN VARCHAR(50), IDIOMADESTINO VARCHAR(50), FECHA TIMESTAMP)";
        System.out.println(sql);
        
        try (PreparedStatement statement = conexion.prepareStatement(sql))
        {
            statement.executeUpdate(sql);
        }
        catch(SQLException e)
        {
            System.out.println(e);
            DesktopNotify.showDesktopMessage("Fallo", "No se ha podido crear la tabla guardados para el usuario. (SQL Exception)", DesktopNotify.FAIL, TIEMPO_NOTIFICACION);
        }
    }
    
    private boolean actualizarTraduccionesGuardadas()
    {
        boolean guardadosActualizados = false;
        String sql = "UPDATE cuentas SET traducciones_guardadas = ? WHERE usuario = ?";
        USUARIO = GestionSesion.obtenerUsuarioDesdeSesion();
        TABLA_GUARDADOS_SQL = "GUARDADOS_" + USUARIO.toUpperCase();
        
        try
        {
            int NUMERO_DE_GUARDADOS = contarRegistros(TABLA_GUARDADOS_SQL);
            try (PreparedStatement statement = conexion.prepareStatement(sql))
            {
                statement.setInt(1, NUMERO_DE_GUARDADOS);
                statement.setString(2, USUARIO);
                statement.executeUpdate();
                return guardadosActualizados = true;
            }
        }
        catch (SQLException e)
        {
            DesktopNotify.showDesktopMessage("Fallo", "No se han podido actualizar la cantidad de guardados en perfil. (SQL Exception)", DesktopNotify.FAIL, TIEMPO_NOTIFICACION);
        }
        return guardadosActualizados;
    }
    
    private boolean actualizarTraduccionesRealizadas()
    {
        boolean busquedasActualizadas = false;
        USUARIO = GestionSesion.obtenerUsuarioDesdeSesion();
        TABLA_HISTORIAL_SQL = "HISTORIAL_" + USUARIO.toUpperCase();
        
        try
        {
            int NUMERO_DE_BUSQUEDAS = contarRegistros(TABLA_HISTORIAL_SQL);
            String sql = "UPDATE cuentas SET traducciones_realizadas = ? WHERE usuario = ?";
            
            try (PreparedStatement statement = conexion.prepareStatement(sql))
            {
                statement.setInt(1, NUMERO_DE_BUSQUEDAS);
                statement.setString(2, USUARIO);
                statement.executeUpdate();
                return busquedasActualizadas = true;
            }
        }
        catch (SQLException e)
        {
            DesktopNotify.showDesktopMessage("Fallo", "No se han podido actualizar la cantidad de busquedas en perfil. (SQL Exception)", DesktopNotify.FAIL, TIEMPO_NOTIFICACION);
        }
        return busquedasActualizadas;
    }
    
    private boolean actualizarTraduccionesSugeridas()
    {
        boolean sugerenciasActualizadas = false;

        try
        {
            int NUMERO_DE_SUGERENCIAS = contarSugerenciasRealizadas();
            String sql = "UPDATE cuentas SET sugerencias_realizadas = ? WHERE usuario = ?";
            USUARIO = GestionSesion.obtenerUsuarioDesdeSesion();
            
            try (PreparedStatement statement = conexion.prepareStatement(sql))
            {
                statement.setInt(1, NUMERO_DE_SUGERENCIAS);
                statement.setString(2, USUARIO);
                statement.executeUpdate();
                sugerenciasActualizadas = true;
            }
        }
        catch (SQLException e)
        {
            DesktopNotify.showDesktopMessage("Fallo", "No se han podido actualizar la cantidad de sugerencias en perfil. (SQL Exception)", DesktopNotify.FAIL, TIEMPO_NOTIFICACION);
        }

        return sugerenciasActualizadas;
    }

    private int contarRegistros(String nombreTabla) throws SQLException
    {
        String TABLA = nombreTabla;
        int cantidad = 0;
        String sql = "SELECT COUNT(*) AS cantidad FROM " + nombreTabla;
        
        try (PreparedStatement statement = conexion.prepareStatement(sql); ResultSet rs = statement.executeQuery())
        {
            if (rs.next())
            {
                cantidad = rs.getInt("cantidad");
            }
        }
        catch(SQLException e)
        {
            DesktopNotify.showDesktopMessage("Fallo", "No se han podido contar los registros en " + TABLA + ". (SQL Exception)", DesktopNotify.FAIL, TIEMPO_NOTIFICACION);
        }
        return cantidad;
    }
    
    private int contarSugerenciasRealizadas() throws SQLException
    {
        int cantidad = 0;
        String sql = "SELECT COUNT(*) AS cantidad FROM sugerencias WHERE idusuario = ?";
        ID_USUARIO = obtenerIDUsuarioActual();
        
        try (PreparedStatement statement = conexion.prepareStatement(sql))
        {
            statement.setInt(1, ID_USUARIO);
            try (ResultSet rs = statement.executeQuery())
            {
                if (rs.next())
                {
                    cantidad = rs.getInt("cantidad");
                }
            }
            catch(SQLException e)
            {
                DesktopNotify.showDesktopMessage("Fallo", "No se han podido contar las sugerencias personales. (SQL Exception)", DesktopNotify.FAIL, TIEMPO_NOTIFICACION);
            }
        }
        catch(SQLException e)
        {
            DesktopNotify.showDesktopMessage("Fallo", "No se han podido contar las sugerencias personales. (SQL Exception)", DesktopNotify.FAIL, TIEMPO_NOTIFICACION);
        }
        return cantidad;
    }
    
    private int contarSugerenciasStaff(int idUsuario, char estatus) throws SQLException
    {
        int cantidad = 0;
        String sql = "SELECT COUNT(*) AS cantidad FROM sugerencias WHERE idusuario = ? AND estatus = ?";

        try (PreparedStatement statement = conexion.prepareStatement(sql))
        {
            statement.setInt(1, idUsuario);
            statement.setString(2, String.valueOf(estatus));
            try (ResultSet rs = statement.executeQuery())
            {
                if (rs.next()) {
                    cantidad = rs.getInt("cantidad");
                }
            }
        }
        return cantidad;
    }
    
    private int contarRegistrosSancionesPersonales() throws SQLException
    {
        int cantidad = 0;
        String sql = "SELECT COUNT(*) AS cantidad FROM sanciones WHERE usuario_sancionado = ?";

        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setString(1, USUARIO.toUpperCase());
            try (ResultSet rs = statement.executeQuery())
            {
                if (rs.next())
                {
                    cantidad = rs.getInt("cantidad");
                }
            }
        }
        return cantidad;
    }
    
    private void eliminarRegistro(int id, String tablaSQL, String nombreID)
    {
        int ID = id;
        String TABLA_SQL = tablaSQL;
        String COLUMNA_ID = nombreID;
        
        try
        {
            String sql = "DELETE FROM " + TABLA_SQL + " WHERE ID" + COLUMNA_ID + " = ?";
            PreparedStatement statement = conexion.prepareStatement(sql);
            statement.setInt(1, ID);
            statement.executeUpdate();
            DesktopNotify.showDesktopMessage("Exito", "Elemento eliminado exitosamente.", DesktopNotify.SUCCESS, TIEMPO_NOTIFICACION);
        } 
        catch (SQLException e) 
        {
            DesktopNotify.showDesktopMessage("Fail", "No se ha podido eliminar el elemento. (SQL Exception)", DesktopNotify.FAIL, TIEMPO_NOTIFICACION);
        }
    }
    
    private void accionBotonVaciarGuardados()
    {
        TABLA_GUARDADOS_SQL = "GUARDADOS_" + USUARIO.toUpperCase();
        
        if (tablaGuardados.getRowCount() == 0) 
        {
            DesktopNotify.showDesktopMessage("Error", "No hay elementos guardados.", DesktopNotify.ERROR, TIEMPO_NOTIFICACION);
            return;
        }
        
        int opcion = JOptionPane.showConfirmDialog(null, "¿Estás seguro de que deseas vaciar todos los registros de la tabla 'guardados'?", "Confirmar vaciado", JOptionPane.YES_NO_OPTION);
        
        if (opcion == JOptionPane.YES_OPTION)
        {
            vaciarTabla(tablaGuardados, TABLA_GUARDADOS_SQL);
        }
    }
    
    private void accionBotonVaciarHistorial()
    {
        TABLA_HISTORIAL_SQL = "HISTORIAL_" + USUARIO.toUpperCase();
        
        if (tablaHistorial.getRowCount() == 0) 
        {
            DesktopNotify.showDesktopMessage("Error", "No hay elementos en el historial.", DesktopNotify.ERROR, TIEMPO_NOTIFICACION);
            return;
        }
        
        int opcion = JOptionPane.showConfirmDialog(null, "¿Estás seguro de que deseas vaciar todos los registros de la tabla 'historial'?", "Confirmar vaciado", JOptionPane.YES_NO_OPTION);

        if (opcion == JOptionPane.YES_OPTION) 
        {
            vaciarTabla(tablaHistorial, TABLA_HISTORIAL_SQL);
        }
    }
    
    private void accionBotonVaciarSugerencias()
    {
        if (tablaSugerencias.getRowCount() == 0) 
        {
            DesktopNotify.showDesktopMessage("Error", "No hay elementos en sugerencias.", DesktopNotify.ERROR, TIEMPO_NOTIFICACION);
            return;
        }
        
        int opcion = JOptionPane.showConfirmDialog(null, "¿Estás seguro de que deseas vaciar todos los registros de la tabla 'sugerencias'?", "Confirmar vaciado", JOptionPane.YES_NO_OPTION);

        if (opcion == JOptionPane.YES_OPTION) 
        {
            vaciarTabla(tablaSugerencias, "SUGERENCIAS");
        }
    }
    
    private void accionBotonVaciarPalabras()
    {
        if (tablaPalabras.getRowCount() == 0) 
        {
            DesktopNotify.showDesktopMessage("Error", "No hay elementos en palabras.", DesktopNotify.ERROR, TIEMPO_NOTIFICACION);
            return;
        }
        
        int opcion = JOptionPane.showConfirmDialog(null, "¿Estás seguro de que deseas vaciar todos los registros de la tabla 'palabras'?\nEsta accion no se puede deshacer.", "Confirmar vaciado", JOptionPane.YES_NO_OPTION);

        if (opcion == JOptionPane.YES_OPTION) 
        {
            vaciarTabla(tablaPalabras, "PALABRAS");
        }
    }
    
    private void vaciarTabla(JTable tabla, String tablaSQL) 
    {
        JTable TABLA_JAVA = tabla;
        String TABLA_SQL = tablaSQL;
        
        String sql = "DELETE FROM " + TABLA_SQL;
        
        try (PreparedStatement statement = conexion.prepareStatement(sql))
        {
            statement.executeUpdate();
            statement.close();

            DefaultTableModel tableModel = (DefaultTableModel) TABLA_JAVA.getModel();
            tableModel.setRowCount(0);
            DesktopNotify.showDesktopMessage("Exito", "Elementos vaciados correctamente.", DesktopNotify.SUCCESS, TIEMPO_NOTIFICACION);
        } 
        catch (SQLException e) 
        {
            DesktopNotify.showDesktopMessage("Fallo", "No se han podido vaciar todos los elementos en la tabla. (SQL Exception)", DesktopNotify.FAIL, TIEMPO_NOTIFICACION);
        }
    }
    
    private void accionBotonRemoverRegistro(JTable tabla, String tablaSQL, String nombreID)
    {
        JTable TABLA_JAVA = tabla;
        String TABLA_SQL = tablaSQL;
        String COLUMNA_ID = nombreID;
        
        DefaultTableModel modelo = (DefaultTableModel) TABLA_JAVA.getModel();
        int indiceSeleccionado = TABLA_JAVA.getSelectedRow();

        if (indiceSeleccionado == -1) 
        {
            DesktopNotify.showDesktopMessage("Error", "Debe seleccionar un elemento a eliminar.", DesktopNotify.ERROR, TIEMPO_NOTIFICACION);
            return;
        }

        int confirmar = JOptionPane.showConfirmDialog(this, "¿Está seguro de que desea eliminar este elemento? Esta accion no se puede deshacer.", "Confirmación", JOptionPane.YES_NO_OPTION);

        if (confirmar == JOptionPane.YES_OPTION) 
        {
            BigDecimal ID = (BigDecimal) modelo.getValueAt(indiceSeleccionado, 0);

            modelo.removeRow(indiceSeleccionado);
            eliminarRegistro(ID.intValue(), TABLA_SQL, COLUMNA_ID);
        }
    }
    
    private boolean verificarTraduccionExistente(String textoInput, String textoOutput)
    {
        String TEXTO_INPUT = textoInput;
        String TEXTO_OUTPUT = textoOutput;
        
        String sql = "SELECT COUNT(*) FROM " + tablaGuardados + " WHERE TEXTOORIGINAL = ? AND IDIOMAORIGEN = ?";
        
        try (PreparedStatement statement = conexion.prepareStatement(sql))
        {
            statement.setString(1, TEXTO_INPUT);
            statement.setString(2, TEXTO_OUTPUT);
            try (ResultSet rs = statement.executeQuery())
            {
                if (rs.next())
                {
                    int cantidad = rs.getInt(1);
                    return cantidad > 0;
                }
            }
        }
        catch (SQLException e) 
        {
            DesktopNotify.showDesktopMessage("Fallo", "No se ha podido verificar la existencia de la traduccion. (SQL Exception)", DesktopNotify.FAIL, TIEMPO_NOTIFICACION);
        }
        return false;
    }
    
    private void actionBotonEnviarSugerencia()
    {
        String TEXTO_INPUT = textFieldPalabra.getText();
        String TEXTO_OUTPUT = textFieldTraduccion.getText();
        String SUGERENCIA = textFieldSugerencia.getText();
        String IDIOMA_INPUT = (String) idiomasInicial.getSelectedItem();
        String IDIOMA_OUTPUT = (String) idiomasFinal.getSelectedItem();
        
        int ultimoID = obtenerUltimoID("SUGERENCIAS", "SUGERENCIA");
        int nuevoID = ultimoID + 1;
        
        Date fechaHoraActual = new Date();
        Timestamp fechaHoraTimestamp = new Timestamp(fechaHoraActual.getTime());
                
        String sql = "INSERT INTO sugerencias (IDSUGERENCIA, IDUSUARIO, PALABRA_ORIGINAL, PALABRA_TRADUCIDA, SUGERENCIA, IDIOMA_ORIGINAL, IDIOMA_TRADUCCION, ESTATUS, FECHA) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        ID_USUARIO = obtenerIDUsuarioActual();
        
        try(PreparedStatement statement = conexion.prepareStatement(sql))
        {
            statement.setInt(1, nuevoID);
            statement.setInt(2, ID_USUARIO);
            statement.setString(3, TEXTO_INPUT);
            statement.setString(4, TEXTO_OUTPUT);
            statement.setString(5, SUGERENCIA);
            statement.setString(6, IDIOMA_INPUT);
            statement.setString(7, IDIOMA_OUTPUT);
            statement.setString(8, "P");
            statement.setTimestamp(9, fechaHoraTimestamp);
            statement.executeUpdate();
            
            textFieldPalabra.setText("");
            textFieldTraduccion.setText("");
            textFieldSugerencia.setText("");
            idiomasInicial.setSelectedItem(1);
            idiomasFinal.setSelectedItem(1);
            textAreaInput.setText("");
            textAreaOutput.setText("");
            idiomasInput.setSelectedItem(1);
            idiomasOutput.setSelectedItem(1);
            
            CARD_LAYOUT.show(contenedorDeSecciones, "traducir");
            tituloSeccion.setText("Traducir");
            DesktopNotify.showDesktopMessage("Exito", "Sugerencia enviada exitosamente.", DesktopNotify.SUCCESS, TIEMPO_NOTIFICACION);
        } 
        catch (SQLException e)
        {
            DesktopNotify.showDesktopMessage("Fallo", "No se ha podido enviar la sugerencia. (SQL Exception)", DesktopNotify.FAIL, TIEMPO_NOTIFICACION);
        }
    }
    
    private void rechazarSugerencia()
    {
        int filaSeleccionada = tablaSugerencias.getSelectedRow();
        if (filaSeleccionada == -1) 
        {
            DesktopNotify.showDesktopMessage("Error", "Debe seleccionar un elemento a rechazar.", DesktopNotify.ERROR, TIEMPO_NOTIFICACION);
            return;
        }

        BigDecimal ID = (BigDecimal) tablaSugerencias.getValueAt(filaSeleccionada, 0);
        int idSugerencia = ID.intValue();

        try
        {
            String sql = "UPDATE sugerencias SET estatus = 'R' WHERE idsugerencia = ?";
            try (PreparedStatement statement = conexion.prepareStatement(sql))
            {
                statement.setInt(1, idSugerencia);
                int filasActualizadas = statement.executeUpdate();
                if (filasActualizadas > 0) 
                {
                    DesktopNotify.showDesktopMessage("Exito", "Sugerencia rechazada exitosamente.", DesktopNotify.SUCCESS, TIEMPO_NOTIFICACION);
                    cargarDatosTabla(tablaSugerencias, "SUGERENCIAS", "SUGERENCIA");
                } 
                else
                {
                    DesktopNotify.showDesktopMessage("Fallo", "No se ha podido rechazar la sugerencia. (SQL Exception)", DesktopNotify.FAIL, TIEMPO_NOTIFICACION);
                }
            }
        } 
        catch (SQLException e) 
        {
            DesktopNotify.showDesktopMessage("Fallo", "No se ha podido rechazar la sugerencia. (SQL Exception)", DesktopNotify.FAIL, TIEMPO_NOTIFICACION);
        }
    }
    
    private void aprobarSugerencia()
    {
        int filaSeleccionada = tablaSugerencias.getSelectedRow();
        if (filaSeleccionada == -1) 
        {
            DesktopNotify.showDesktopMessage("Error", "Seleccione un elemento a aprobar.", DesktopNotify.ERROR, TIEMPO_NOTIFICACION);
            return;
        }
        
        BigDecimal ID_BD = (BigDecimal) tablaSugerencias.getValueAt(filaSeleccionada, 0);
        int ID = ID_BD.intValue();

        String TEXTO_INPUT = (String) tablaSugerencias.getValueAt(filaSeleccionada, 1);
        String TEXTO_OUTPUT = (String) tablaSugerencias.getValueAt(filaSeleccionada, 2);
        String SUGERENCIA = (String) tablaSugerencias.getValueAt(filaSeleccionada, 3);
        String IDIOMA_INPUT = (String) tablaSugerencias.getValueAt(filaSeleccionada, 4);
        String IDIOMA_OUTPUT = (String) tablaSugerencias.getValueAt(filaSeleccionada, 5);
        
        TEXTO_INPUT = TEXTO_INPUT.toLowerCase();
        TEXTO_OUTPUT = TEXTO_OUTPUT.toLowerCase();
        SUGERENCIA = SUGERENCIA.toLowerCase();
        
        String COLUMNA_INPUT;
        if (IDIOMA_INPUT.equalsIgnoreCase("español")) 
        {
            COLUMNA_INPUT = "palabraespanol";
        }
        else
        {
            COLUMNA_INPUT = "palabra" + IDIOMA_INPUT.toLowerCase();
        }
        
        String COLUMNA_OUTPUT;
        if(IDIOMA_OUTPUT.equalsIgnoreCase("español"))
        {
            COLUMNA_OUTPUT = "espanol";
        }
        else
        {
            COLUMNA_OUTPUT = "palabra" + IDIOMA_OUTPUT.toLowerCase();
        }
        
        boolean PALABRA_EXISTENTE = verificarPalabraExistente(TEXTO_INPUT, COLUMNA_INPUT);
        try
        {
            if (PALABRA_EXISTENTE) 
            {         
                String sqlUpdate = "UPDATE palabras SET " + COLUMNA_OUTPUT + " = ? WHERE " + COLUMNA_INPUT + " = ?";
                try (PreparedStatement statement = conexion.prepareStatement(sqlUpdate))
                {
                    statement.setString(1, SUGERENCIA);
                    statement.setString(2, TEXTO_INPUT);
                    statement.executeUpdate();
                }
            } 
            else
            {
                String sqlInsert = "INSERT INTO palabras (idpalabra, " + COLUMNA_INPUT + ", " + COLUMNA_OUTPUT + ") VALUES (?, ?, ?)";
                try (PreparedStatement statement = conexion.prepareStatement(sqlInsert))
                {
                    int ULTIMO_ID = obtenerUltimoID("PALABRAS", "PALABRA");
                    int NUEVO_ID = ULTIMO_ID + 1;
                
                    statement.setInt(1, NUEVO_ID);
                    statement.setString(2, TEXTO_INPUT);
                    statement.setString(3, SUGERENCIA);
                    statement.executeUpdate();
                }
            }
            
            String sqlUpdateSugerencia = "UPDATE sugerencias SET estatus = 'A' WHERE idsugerencia = ?";
            try (PreparedStatement statement = conexion.prepareStatement(sqlUpdateSugerencia))
            {
                statement.setInt(1, ID);
                statement.executeUpdate();
            }
            DesktopNotify.showDesktopMessage("Exito", "Sugerencia aprobada exitosamente.", DesktopNotify.SUCCESS, TIEMPO_NOTIFICACION);
            cargarDatosTabla(tablaSugerencias, "SUGERENCIAS", "SUGERENCIA");
        }
        catch (SQLException e)
        {
            DesktopNotify.showDesktopMessage("Fallo", "No se ha podido aprobar la sugerencia. (SQL Exception)", DesktopNotify.FAIL, TIEMPO_NOTIFICACION);
        }
    }
    
    private boolean verificarPalabraExistente(String textoInput, String idiomaInput) 
    {
        String TEXTO_INPUT = textoInput;
        String IDIOMA_INPUT = idiomaInput;

        String sql = "SELECT COUNT(*) FROM palabras WHERE " + IDIOMA_INPUT + " = ?";

        try (PreparedStatement statement = conexion.prepareStatement(sql))
        {
            statement.setString(1, TEXTO_INPUT);
            try (ResultSet rs = statement.executeQuery())
            {
                if (rs.next())
                {
                    int cantidad = rs.getInt(1);
                    return cantidad > 0;
                }
            }
        }
        catch (SQLException e)
        {
            DesktopNotify.showDesktopMessage("Fallo", "No se ha podido verificar la existencia de la palabra. (SQL Exception)", DesktopNotify.FAIL, TIEMPO_NOTIFICACION);
        }
        return false;
    }
    
    private void cargarSugerenciasPersonales()
    {
        DefaultTableModel model = (DefaultTableModel) tablaSugerenciasPersonales.getModel();
        model.setRowCount(0);
        model.setColumnIdentifiers(new Object[]{"ID Sugerencia", "Palabra Original", "Traduccion Original", "Sugerencia", "Idioma Original", "Idioma Traducción", "Fecha", "Estatus"});

        String sql = "SELECT * FROM sugerencias WHERE idusuario = ?";
        ID_USUARIO = obtenerIDUsuarioActual();
        
        try (PreparedStatement statement = conexion.prepareStatement(sql))
        {
            statement.setInt(1, ID_USUARIO);

            try (ResultSet resultSet = statement.executeQuery())
            {
                while (resultSet.next())
                {
                    int ID_SUGERENCIA = resultSet.getInt("idsugerencia");
                    String PALABRA_INPUT = resultSet.getString("palabra_original");
                    String PALABRA_OUTPUT = resultSet.getString("palabra_traducida");
                    String SUGERENCIA = resultSet.getString("sugerencia");
                    String IDIOMA_INPUT = resultSet.getString("idioma_original");
                    String IDIOMA_OUTPUT = resultSet.getString("idioma_traduccion");
                    Timestamp FECHA = resultSet.getTimestamp("fecha");
                    String ESTATUS = resultSet.getString("estatus");

                    model.addRow(new Object[]{ID_SUGERENCIA, PALABRA_INPUT, PALABRA_OUTPUT, SUGERENCIA, IDIOMA_INPUT, IDIOMA_OUTPUT, FECHA, ESTATUS});
                }
            }
        }
        catch (SQLException e)
        {
            DesktopNotify.showDesktopMessage("Error", "No se pudieron cargar las sugerencias personales. (SQL Exception)", DesktopNotify.FAIL, TIEMPO_NOTIFICACION);
        }
    }
    
    public void llenarComboBoxUsuarios()
    {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        List<String> usuarios = obtenerUsuarios();

        for (String usuario : usuarios)
        {
            model.addElement(usuario);
        }

        comboBoxUsuarios.setModel(model);
    }

    private List<String> obtenerUsuarios()
    {
        List<String> usuarios = new ArrayList<>();
        String sql = "SELECT usuario FROM cuentas";

        try (PreparedStatement statement = conexion.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery())
        {
            while (resultSet.next())
            {
                usuarios.add(resultSet.getString("usuario"));
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return usuarios;
    }
    
    private void llenarDatosUsuario()
    {
        String usuarioSeleccionado = (String) comboBoxUsuarios.getSelectedItem();
        if(usuarioSeleccionado == null)
        {
            restablecerPlaceholders();
            return;
        }
        System.out.println("Llenando datos de staff");
        
        String sql = "SELECT c.usuario, r.idrol FROM cuentas c INNER JOIN roles r ON c.idrol = r.idrol WHERE c.usuario = ?";
        try (PreparedStatement statement = conexion.prepareStatement(sql))
        {
            statement.setString(1, usuarioSeleccionado);
            try (ResultSet resultSet = statement.executeQuery())
            {
                if (resultSet.next())
                {
                    int idRol = resultSet.getInt("idrol");
                    String usuario = resultSet.getString("usuario");

                    placeholderUsuarioStaff.setText(usuario);

                    String esStaff = (idRol == 1) ? "- No" : "- Si";
                    placeholderEsStaff.setText(esStaff);

                    if (idRol == 1)
                    {
                        restablecerPlaceholders();
                    }
                    else
                    {
                        obtenerDatosStaff(usuario);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void obtenerDatosStaff(String usuario)
    {
        String sql = "SELECT * FROM staff WHERE idcuenta = (SELECT idcuenta FROM cuentas WHERE usuario = ?)";
        try (PreparedStatement statement = conexion.prepareStatement(sql))
        {
            statement.setString(1, usuario);
            try (ResultSet resultSet = statement.executeQuery())
            {
                if (resultSet.next())
                {
                    String estatus = resultSet.getString("estado");
                    String fechaInicio = resultSet.getString("fecha_inicio");
                    String fechaFin = resultSet.getString("fecha_fin");

                    placeholderStatusStaff.setText(estatus != null && !estatus.isEmpty() ? estatus : "-");
                    placeholderStaffDesdeStaff.setText(fechaInicio != null && !fechaInicio.isEmpty() ? fechaInicio : "-");
                    placeholderStaffHastaStaff.setText(fechaFin != null && !fechaFin.isEmpty() ? fechaFin : "-");

                    int idUsuario = obtenerIdUsuarioSeleccionado();
                    int sugerenciasAprobadas = contarSugerenciasStaff(idUsuario, 'A');
                    int sugerenciasRechazadas = contarSugerenciasStaff(idUsuario, 'R');
                    placeholderSugerenciasAprobadasStaff.setText(String.valueOf(sugerenciasAprobadas));
                    placeholderSugerenciasRechazadasStaff.setText(String.valueOf(sugerenciasRechazadas));

                    int sancionesEmitidas = contarRegistrosSancionesPersonales();
                    placeholderSancionesEmitidasStaff.setText(String.valueOf(sancionesEmitidas));
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    
    private void restablecerPlaceholders()
    {
        //placeholderUsuarioStaff.setText("-");
        //placeholderEsStaff.setText("-");
        placeholderStatusStaff.setText("-");
        placeholderStaffDesdeStaff.setText("-");
        placeholderStaffHastaStaff.setText("-");
        placeholderSugerenciasAprobadasStaff.setText("0");
        placeholderSugerenciasRechazadasStaff.setText("0");
        placeholderSancionesEmitidasStaff.setText("0");
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Sesion = new javax.swing.JFrame();
        lienzoSesion = new javax.swing.JPanel();
        contenedorTotal = new javax.swing.JPanel();
        contenedorFooterSesion = new javax.swing.JPanel();
        copyrightRegistro = new javax.swing.JLabel();
        contenedorPrincipal = new javax.swing.JPanel();
        contenedorRegistro = new javax.swing.JPanel();
        panelSeparadorRegistro = new javax.swing.JPanel();
        panelPerfilRegistro = new javax.swing.JPanel();
        tituloRegistro = new javax.swing.JLabel();
        panelBotonesRegistro = new javax.swing.JPanel();
        botonRegistro = new javax.swing.JButton();
        etiquetaConCuenta = new javax.swing.JLabel();
        panelInformacionRegistro = new javax.swing.JPanel();
        iconoNombreRegistro = new javax.swing.JLabel();
        tituloNombreRegistro = new javax.swing.JLabel();
        textFieldNombreRegistro = new javax.swing.JTextField();
        iconoUsuarioRegistro = new javax.swing.JLabel();
        tituloUsuarioRegistro = new javax.swing.JLabel();
        textFieldUsuarioRegistro = new javax.swing.JTextField();
        iconoPasswordRegistro = new javax.swing.JLabel();
        tituloPasswordRegistro = new javax.swing.JLabel();
        passwordFieldPasswordRegistro = new javax.swing.JPasswordField();
        iconoCPasswordRegistro = new javax.swing.JLabel();
        tituloCPasswordRegistro = new javax.swing.JLabel();
        passwordFieldCPasswordRegistro = new javax.swing.JPasswordField();
        etiquetaMensajeErrorRegistro = new javax.swing.JLabel();
        contenedorLogin = new javax.swing.JPanel();
        panelBotonesLogin = new javax.swing.JPanel();
        botonLogin = new javax.swing.JButton();
        etiquetaSinCuenta = new javax.swing.JLabel();
        panelInformacionLogin = new javax.swing.JPanel();
        iconoUsuarioLogin = new javax.swing.JLabel();
        tituloUsuarioLogin = new javax.swing.JLabel();
        iconoPasswordLogin = new javax.swing.JLabel();
        tituloPasswordLogin = new javax.swing.JLabel();
        etiquetaMensajeErrorLogin = new javax.swing.JLabel();
        textFieldUsuarioLogin = new javax.swing.JTextField();
        passwordFieldPasswordLogin = new javax.swing.JPasswordField();
        checkBoxRecuerdame = new javax.swing.JCheckBox();
        panelPerfilLogin = new javax.swing.JPanel();
        imagenLogin = new javax.swing.JLabel();
        panelSeparadorLogin = new javax.swing.JPanel();
        lienzo = new javax.swing.JPanel();
        contenedorHeader = new javax.swing.JPanel();
        tituloSeccion = new javax.swing.JLabel();
        seccionCuenta = new javax.swing.JLabel();
        seccionTraducir = new javax.swing.JLabel();
        seccionPalabras = new javax.swing.JLabel();
        seccionSugerenciasStaff = new javax.swing.JLabel();
        jSeparator7 = new javax.swing.JSeparator();
        jSeparator8 = new javax.swing.JSeparator();
        jSeparator9 = new javax.swing.JSeparator();
        contenedorDeSecciones = new javax.swing.JPanel();
        contenedorTraduccion = new javax.swing.JPanel();
        cuadroInput = new javax.swing.JPanel();
        idiomasInput = new javax.swing.JComboBox<>();
        textAreaInput = new javax.swing.JTextArea();
        botonBorrarInput = new javax.swing.JLabel();
        botonTraducir = new javax.swing.JLabel();
        botonCopiarInput = new javax.swing.JLabel();
        iconoIdiomaInput = new javax.swing.JLabel();
        cantidadCaracteresActuales = new javax.swing.JLabel();
        cuadroOutput = new javax.swing.JPanel();
        idiomasOutput = new javax.swing.JComboBox<>();
        textAreaOutput = new javax.swing.JTextArea();
        botonBorrarOutput = new javax.swing.JLabel();
        botonCopiarOutput = new javax.swing.JLabel();
        botonSugerirCambios = new javax.swing.JLabel();
        botonGuardar = new javax.swing.JLabel();
        iconoIdiomaOutput = new javax.swing.JLabel();
        contenedorCentral = new javax.swing.JPanel();
        botonInvertir = new javax.swing.JLabel();
        contenedorMensajes = new javax.swing.JPanel();
        mensajeAccion = new javax.swing.JLabel();
        contenedorCuenta = new javax.swing.JPanel();
        contenedorDatosPersonales = new javax.swing.JPanel();
        tituloDatosPersonales = new javax.swing.JLabel();
        tituloNombre = new javax.swing.JLabel();
        textFieldNombreDatos = new javax.swing.JTextField();
        tituloUsuario = new javax.swing.JLabel();
        textFieldUsuarioDatos = new javax.swing.JTextField();
        tituloLengua = new javax.swing.JLabel();
        comboBoxLenguas = new javax.swing.JComboBox<>();
        iconoLenguaMaterna = new javax.swing.JLabel();
        botonEditarDatos = new javax.swing.JLabel();
        tituloEditarDatos = new javax.swing.JLabel();
        botonConfirmarCambios = new javax.swing.JLabel();
        tituloConfirmarCambios = new javax.swing.JLabel();
        botonCancelarCambios = new javax.swing.JLabel();
        tituloCancelarCambios = new javax.swing.JLabel();
        contenedorDatosGenerales = new javax.swing.JPanel();
        tituloGuardados = new javax.swing.JLabel();
        cantidadGuardados = new javax.swing.JLabel();
        botonGuardados = new javax.swing.JLabel();
        cantidadSugerencias = new javax.swing.JLabel();
        tituloSugerencias = new javax.swing.JLabel();
        botonSugerencias = new javax.swing.JLabel();
        cantidadSanciones = new javax.swing.JLabel();
        tituloSanciones = new javax.swing.JLabel();
        botonSanciones = new javax.swing.JLabel();
        cantidadBusquedas = new javax.swing.JLabel();
        tituloBusquedas = new javax.swing.JLabel();
        botonBusquedas = new javax.swing.JLabel();
        jSeparator5 = new javax.swing.JSeparator();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        etiquetaCerrarSesion = new javax.swing.JLabel();
        etiquetaStaffPanel = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        botonCerrarSesion = new javax.swing.JLabel();
        botonStaffPanel = new javax.swing.JLabel();
        jSeparator6 = new javax.swing.JSeparator();
        contenedorStaffPanel = new javax.swing.JPanel();
        tituloStaff = new javax.swing.JLabel();
        botonStaff = new javax.swing.JLabel();
        tituloSacionesGlobales = new javax.swing.JLabel();
        botonSancionesGlobales = new javax.swing.JLabel();
        tituloSugerenciasGlobales = new javax.swing.JLabel();
        botonSugerenciasGlobales = new javax.swing.JLabel();
        tituloVolverStaffPanel = new javax.swing.JLabel();
        botonVolverStaffPanel = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        jSeparator11 = new javax.swing.JSeparator();
        jSeparator10 = new javax.swing.JSeparator();
        subtituloApartadosStaffPanel = new javax.swing.JLabel();
        contenedorStaff = new javax.swing.JPanel();
        tituloBuscarStaff = new javax.swing.JLabel();
        comboBoxUsuarios = new javax.swing.JComboBox<>();
        iconoUsuarioStaff = new javax.swing.JLabel();
        tituloUsuarioStaff = new javax.swing.JLabel();
        placeholderUsuarioStaff = new javax.swing.JLabel();
        iconoRolStaff = new javax.swing.JLabel();
        tituloRolStaff = new javax.swing.JLabel();
        comboBoxRolStaff = new javax.swing.JComboBox<>();
        iconoEstatusStaff = new javax.swing.JLabel();
        tituloEstatusStaff = new javax.swing.JLabel();
        placeholderStatusStaff = new javax.swing.JLabel();
        iconoSugerenciasAceptadas = new javax.swing.JLabel();
        tituloSugerenciasAprobadas = new javax.swing.JLabel();
        placeholderSugerenciasAprobadasStaff = new javax.swing.JLabel();
        iconoSugerenciasRechazadasStaff = new javax.swing.JLabel();
        tituloSugerenciasRechazadasStaff = new javax.swing.JLabel();
        placeholderSugerenciasRechazadasStaff = new javax.swing.JLabel();
        iconoSancionesEmitidasStaff = new javax.swing.JLabel();
        tituloSancionesEmitidasStaff = new javax.swing.JLabel();
        placeholderSancionesEmitidasStaff = new javax.swing.JLabel();
        iconoStaffDesdeStaff = new javax.swing.JLabel();
        tituloStaffDesdeStaff = new javax.swing.JLabel();
        placeholderStaffDesdeStaff = new javax.swing.JLabel();
        iconoStaffHastaStaff = new javax.swing.JLabel();
        tituloStaffHastaStaff = new javax.swing.JLabel();
        placeholderStaffHastaStaff = new javax.swing.JLabel();
        iconoListarStaff = new javax.swing.JLabel();
        botonEditarRol = new javax.swing.JLabel();
        botonCancelarEditarRol = new javax.swing.JLabel();
        botonConfirmarEditarRol = new javax.swing.JLabel();
        placeholderEsStaff = new javax.swing.JLabel();
        tituloEsStaff = new javax.swing.JLabel();
        iconoEsStaff = new javax.swing.JLabel();
        contenedorPalabras = new javax.swing.JPanel();
        scrollPanelPalabras = new javax.swing.JScrollPane();
        tablaPalabras = new javax.swing.JTable();
        tituloVaciarPalabras = new javax.swing.JLabel();
        botonVaciarPalabras = new javax.swing.JLabel();
        botonRemoverPalabra = new javax.swing.JLabel();
        tituloRemoverPalabra = new javax.swing.JLabel();
        contenedorSugerenciasPersonales = new javax.swing.JPanel();
        scrollPanelSugerenciasPersonales = new javax.swing.JScrollPane();
        tablaSugerenciasPersonales = new javax.swing.JTable();
        tituloVolverSugerenciasPersonales = new javax.swing.JLabel();
        botonVolverSugerenciasPersonales = new javax.swing.JLabel();
        contenedorHistorial = new javax.swing.JPanel();
        scrollPanelHistorial = new javax.swing.JScrollPane();
        tablaHistorial = new javax.swing.JTable();
        tituloVaciarHistorial = new javax.swing.JLabel();
        botonVaciarHistorial = new javax.swing.JLabel();
        botonRemoverElementoHistorial = new javax.swing.JLabel();
        tituloRemoverElementoHistorial = new javax.swing.JLabel();
        tituloVolverHistorial = new javax.swing.JLabel();
        botonVolverHistorial = new javax.swing.JLabel();
        contenedorGuardados = new javax.swing.JPanel();
        scrollPanelGuardados = new javax.swing.JScrollPane();
        tablaGuardados = new javax.swing.JTable();
        botonRemoverGuardado = new javax.swing.JLabel();
        tituloRemoverGuardado = new javax.swing.JLabel();
        tituloVaciarGuardados = new javax.swing.JLabel();
        botonVaciarGuardados = new javax.swing.JLabel();
        tituloVolverGuardados = new javax.swing.JLabel();
        botonVolverGuardados = new javax.swing.JLabel();
        contenedorSugerencias = new javax.swing.JPanel();
        lienzoSugerencias = new javax.swing.JPanel();
        contenedorSugerir = new javax.swing.JPanel();
        tituloPalabra = new javax.swing.JLabel();
        textFieldPalabra = new javax.swing.JTextField();
        tituloTraduccion = new javax.swing.JLabel();
        textFieldTraduccion = new javax.swing.JTextField();
        tituloIdiomaInicial = new javax.swing.JLabel();
        tituloIdiomaFinal = new javax.swing.JLabel();
        botonEnviarSugerencia = new javax.swing.JLabel();
        tituloEnviarSugerencia = new javax.swing.JLabel();
        idiomasFinal = new javax.swing.JComboBox<>();
        idiomasInicial = new javax.swing.JComboBox<>();
        iconoIdiomaFinal = new javax.swing.JLabel();
        iconoIdiomaInicial = new javax.swing.JLabel();
        tituloSugerencia = new javax.swing.JLabel();
        textFieldSugerencia = new javax.swing.JTextField();
        contenedorListasSugerencias = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaSugerencias = new javax.swing.JTable();
        botonVaciarSugerencias = new javax.swing.JLabel();
        tituloVaciarSugerencias = new javax.swing.JLabel();
        botonRemoverSugerencia = new javax.swing.JLabel();
        tituloRemoverSugerencia = new javax.swing.JLabel();
        botonAprobarSugerencia = new javax.swing.JLabel();
        tituloAprobarSugerencia = new javax.swing.JLabel();
        botonRechazarSugerencia = new javax.swing.JLabel();
        tituloRechazarSugerencia = new javax.swing.JLabel();
        contenedorFooter = new javax.swing.JPanel();
        copyright = new javax.swing.JLabel();

        lienzoSesion.setBackground(new java.awt.Color(240, 238, 234));
        lienzoSesion.setLayout(new java.awt.CardLayout());

        contenedorTotal.setBackground(new java.awt.Color(240, 238, 234));

        contenedorFooterSesion.setBackground(new java.awt.Color(240, 238, 234));

        copyrightRegistro.setBackground(new java.awt.Color(82, 81, 81));
        copyrightRegistro.setFont(new java.awt.Font("Titillium Web", 0, 10)); // NOI18N
        copyrightRegistro.setForeground(new java.awt.Color(82, 81, 81));
        copyrightRegistro.setText("© 2024 Omarhvs, LLC");

        javax.swing.GroupLayout contenedorFooterSesionLayout = new javax.swing.GroupLayout(contenedorFooterSesion);
        contenedorFooterSesion.setLayout(contenedorFooterSesionLayout);
        contenedorFooterSesionLayout.setHorizontalGroup(
            contenedorFooterSesionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contenedorFooterSesionLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(copyrightRegistro)
                .addGap(198, 198, 198))
        );
        contenedorFooterSesionLayout.setVerticalGroup(
            contenedorFooterSesionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contenedorFooterSesionLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(copyrightRegistro)
                .addGap(6, 6, 6))
        );

        contenedorPrincipal.setBackground(new java.awt.Color(240, 238, 234));
        contenedorPrincipal.setLayout(new java.awt.CardLayout());

        contenedorRegistro.setBackground(new java.awt.Color(240, 238, 234));

        panelSeparadorRegistro.setBackground(new java.awt.Color(240, 238, 234));

        javax.swing.GroupLayout panelSeparadorRegistroLayout = new javax.swing.GroupLayout(panelSeparadorRegistro);
        panelSeparadorRegistro.setLayout(panelSeparadorRegistroLayout);
        panelSeparadorRegistroLayout.setHorizontalGroup(
            panelSeparadorRegistroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        panelSeparadorRegistroLayout.setVerticalGroup(
            panelSeparadorRegistroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 34, Short.MAX_VALUE)
        );

        panelPerfilRegistro.setBackground(new java.awt.Color(240, 238, 234));

        tituloRegistro.setFont(new java.awt.Font("DM Serif Display", 2, 36)); // NOI18N
        tituloRegistro.setForeground(new java.awt.Color(244, 107, 82));
        tituloRegistro.setText("REGISTRAR");

        javax.swing.GroupLayout panelPerfilRegistroLayout = new javax.swing.GroupLayout(panelPerfilRegistro);
        panelPerfilRegistro.setLayout(panelPerfilRegistroLayout);
        panelPerfilRegistroLayout.setHorizontalGroup(
            panelPerfilRegistroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelPerfilRegistroLayout.createSequentialGroup()
                .addContainerGap(135, Short.MAX_VALUE)
                .addComponent(tituloRegistro)
                .addGap(136, 136, 136))
        );
        panelPerfilRegistroLayout.setVerticalGroup(
            panelPerfilRegistroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPerfilRegistroLayout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addComponent(tituloRegistro)
                .addContainerGap(38, Short.MAX_VALUE))
        );

        panelBotonesRegistro.setBackground(new java.awt.Color(240, 238, 234));
        panelBotonesRegistro.setPreferredSize(new java.awt.Dimension(446, 81));

        botonRegistro.setBackground(new java.awt.Color(244, 107, 82));
        botonRegistro.setFont(new java.awt.Font("Titillium Web", 0, 14)); // NOI18N
        botonRegistro.setForeground(new java.awt.Color(240, 238, 234));
        botonRegistro.setText("Registrarme");
        botonRegistro.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(244, 107, 82)));
        botonRegistro.setBorderPainted(false);
        botonRegistro.setFocusPainted(false);
        botonRegistro.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonRegistroMouseClicked(evt);
            }
        });

        etiquetaConCuenta.setFont(new java.awt.Font("Titillium Web", 1, 10)); // NOI18N
        etiquetaConCuenta.setForeground(new java.awt.Color(82, 81, 81));
        etiquetaConCuenta.setText("Ya tengo cuenta");
        etiquetaConCuenta.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        etiquetaConCuenta.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                etiquetaConCuentaMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout panelBotonesRegistroLayout = new javax.swing.GroupLayout(panelBotonesRegistro);
        panelBotonesRegistro.setLayout(panelBotonesRegistroLayout);
        panelBotonesRegistroLayout.setHorizontalGroup(
            panelBotonesRegistroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBotonesRegistroLayout.createSequentialGroup()
                .addGap(99, 99, 99)
                .addComponent(botonRegistro, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(99, 99, 99))
            .addGroup(panelBotonesRegistroLayout.createSequentialGroup()
                .addGap(196, 196, 196)
                .addComponent(etiquetaConCuenta)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelBotonesRegistroLayout.setVerticalGroup(
            panelBotonesRegistroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBotonesRegistroLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(botonRegistro)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(etiquetaConCuenta)
                .addContainerGap(17, Short.MAX_VALUE))
        );

        panelInformacionRegistro.setBackground(new java.awt.Color(240, 238, 234));
        panelInformacionRegistro.setPreferredSize(new java.awt.Dimension(365, 390));

        iconoNombreRegistro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icono_usuario.png"))); // NOI18N

        tituloNombreRegistro.setFont(new java.awt.Font("Titillium Web", 0, 18)); // NOI18N
        tituloNombreRegistro.setForeground(new java.awt.Color(82, 81, 81));
        tituloNombreRegistro.setText("Nombre");

        textFieldNombreRegistro.setFont(new java.awt.Font("Titillium Web", 0, 14)); // NOI18N
        textFieldNombreRegistro.setForeground(new java.awt.Color(82, 81, 81));
        textFieldNombreRegistro.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(82, 81, 81)));
        textFieldNombreRegistro.setCaretColor(new java.awt.Color(244, 107, 82));
        textFieldNombreRegistro.setSelectionColor(new java.awt.Color(244, 107, 82));

        iconoUsuarioRegistro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icono_usuario.png"))); // NOI18N

        tituloUsuarioRegistro.setFont(new java.awt.Font("Titillium Web", 0, 18)); // NOI18N
        tituloUsuarioRegistro.setForeground(new java.awt.Color(82, 81, 81));
        tituloUsuarioRegistro.setText("Usuario");

        textFieldUsuarioRegistro.setFont(new java.awt.Font("Titillium Web", 0, 14)); // NOI18N
        textFieldUsuarioRegistro.setForeground(new java.awt.Color(82, 81, 81));
        textFieldUsuarioRegistro.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(82, 81, 81)));
        textFieldUsuarioRegistro.setCaretColor(new java.awt.Color(244, 107, 82));
        textFieldUsuarioRegistro.setSelectionColor(new java.awt.Color(244, 107, 82));

        iconoPasswordRegistro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icono_contrasena.png"))); // NOI18N

        tituloPasswordRegistro.setFont(new java.awt.Font("Titillium Web", 0, 18)); // NOI18N
        tituloPasswordRegistro.setForeground(new java.awt.Color(82, 81, 81));
        tituloPasswordRegistro.setText("Contraseña");

        passwordFieldPasswordRegistro.setFont(new java.awt.Font("Titillium Web", 0, 14)); // NOI18N
        passwordFieldPasswordRegistro.setForeground(new java.awt.Color(82, 81, 81));
        passwordFieldPasswordRegistro.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(82, 81, 81)));
        passwordFieldPasswordRegistro.setCaretColor(new java.awt.Color(244, 107, 82));
        passwordFieldPasswordRegistro.setSelectionColor(new java.awt.Color(244, 107, 82));

        iconoCPasswordRegistro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icono_contrasena.png"))); // NOI18N

        tituloCPasswordRegistro.setFont(new java.awt.Font("Titillium Web", 0, 18)); // NOI18N
        tituloCPasswordRegistro.setForeground(new java.awt.Color(82, 81, 81));
        tituloCPasswordRegistro.setText("Confirmar Contraseña");

        passwordFieldCPasswordRegistro.setFont(new java.awt.Font("Titillium Web", 0, 14)); // NOI18N
        passwordFieldCPasswordRegistro.setForeground(new java.awt.Color(82, 81, 81));
        passwordFieldCPasswordRegistro.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(82, 81, 81)));
        passwordFieldCPasswordRegistro.setCaretColor(new java.awt.Color(244, 107, 82));
        passwordFieldCPasswordRegistro.setSelectionColor(new java.awt.Color(244, 107, 82));

        etiquetaMensajeErrorRegistro.setFont(new java.awt.Font("Titillium Web", 0, 10)); // NOI18N
        etiquetaMensajeErrorRegistro.setForeground(new java.awt.Color(204, 51, 0));
        etiquetaMensajeErrorRegistro.setText("errorRegistro");

        javax.swing.GroupLayout panelInformacionRegistroLayout = new javax.swing.GroupLayout(panelInformacionRegistro);
        panelInformacionRegistro.setLayout(panelInformacionRegistroLayout);
        panelInformacionRegistroLayout.setHorizontalGroup(
            panelInformacionRegistroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInformacionRegistroLayout.createSequentialGroup()
                .addGap(103, 103, 103)
                .addGroup(panelInformacionRegistroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelInformacionRegistroLayout.createSequentialGroup()
                        .addComponent(iconoUsuarioRegistro)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tituloUsuarioRegistro))
                    .addGroup(panelInformacionRegistroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(textFieldUsuarioRegistro, javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelInformacionRegistroLayout.createSequentialGroup()
                            .addComponent(iconoNombreRegistro)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(tituloNombreRegistro))
                        .addComponent(passwordFieldPasswordRegistro, javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelInformacionRegistroLayout.createSequentialGroup()
                            .addComponent(iconoPasswordRegistro)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(tituloPasswordRegistro))
                        .addComponent(passwordFieldCPasswordRegistro)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelInformacionRegistroLayout.createSequentialGroup()
                            .addComponent(iconoCPasswordRegistro)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(tituloCPasswordRegistro))
                        .addComponent(textFieldNombreRegistro, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(etiquetaMensajeErrorRegistro))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelInformacionRegistroLayout.setVerticalGroup(
            panelInformacionRegistroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInformacionRegistroLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(panelInformacionRegistroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(iconoNombreRegistro, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tituloNombreRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textFieldNombreRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addGroup(panelInformacionRegistroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(iconoUsuarioRegistro, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tituloUsuarioRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textFieldUsuarioRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addGroup(panelInformacionRegistroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(iconoPasswordRegistro, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tituloPasswordRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(passwordFieldPasswordRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addGroup(panelInformacionRegistroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(iconoCPasswordRegistro, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tituloCPasswordRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(passwordFieldCPasswordRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(etiquetaMensajeErrorRegistro)
                .addContainerGap(44, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout contenedorRegistroLayout = new javax.swing.GroupLayout(contenedorRegistro);
        contenedorRegistro.setLayout(contenedorRegistroLayout);
        contenedorRegistroLayout.setHorizontalGroup(
            contenedorRegistroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contenedorRegistroLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(contenedorRegistroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(panelBotonesRegistro, javax.swing.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)
                    .addComponent(panelPerfilRegistro, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelInformacionRegistro, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)
                    .addComponent(panelSeparadorRegistro, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(6, 6, 6))
        );
        contenedorRegistroLayout.setVerticalGroup(
            contenedorRegistroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contenedorRegistroLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(panelSeparadorRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(panelPerfilRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(panelInformacionRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(panelBotonesRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6))
        );

        contenedorPrincipal.add(contenedorRegistro, "card2");

        contenedorLogin.setBackground(new java.awt.Color(240, 238, 234));
        contenedorLogin.setPreferredSize(new java.awt.Dimension(470, 646));

        panelBotonesLogin.setBackground(new java.awt.Color(240, 238, 234));

        botonLogin.setBackground(new java.awt.Color(244, 107, 82));
        botonLogin.setFont(new java.awt.Font("Titillium Web", 0, 14)); // NOI18N
        botonLogin.setForeground(new java.awt.Color(240, 238, 234));
        botonLogin.setText("Ingresar");
        botonLogin.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(244, 107, 82)));
        botonLogin.setBorderPainted(false);
        botonLogin.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        botonLogin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonLoginMouseClicked(evt);
            }
        });

        etiquetaSinCuenta.setFont(new java.awt.Font("Titillium Web", 1, 10)); // NOI18N
        etiquetaSinCuenta.setForeground(new java.awt.Color(82, 81, 81));
        etiquetaSinCuenta.setText("Aun no tengo cuenta");
        etiquetaSinCuenta.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        etiquetaSinCuenta.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                etiquetaSinCuentaMouseClicked(evt);
            }
        });
        etiquetaSinCuenta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                etiquetaSinCuentaKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout panelBotonesLoginLayout = new javax.swing.GroupLayout(panelBotonesLogin);
        panelBotonesLogin.setLayout(panelBotonesLoginLayout);
        panelBotonesLoginLayout.setHorizontalGroup(
            panelBotonesLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBotonesLoginLayout.createSequentialGroup()
                .addGap(99, 99, 99)
                .addComponent(botonLogin, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                .addGap(99, 99, 99))
            .addGroup(panelBotonesLoginLayout.createSequentialGroup()
                .addGap(181, 181, 181)
                .addComponent(etiquetaSinCuenta)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelBotonesLoginLayout.setVerticalGroup(
            panelBotonesLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelBotonesLoginLayout.createSequentialGroup()
                .addContainerGap(18, Short.MAX_VALUE)
                .addComponent(botonLogin)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(etiquetaSinCuenta)
                .addGap(18, 18, 18))
        );

        panelInformacionLogin.setBackground(new java.awt.Color(240, 238, 234));

        iconoUsuarioLogin.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icono_usuario.png"))); // NOI18N

        tituloUsuarioLogin.setFont(new java.awt.Font("Titillium Web", 0, 18)); // NOI18N
        tituloUsuarioLogin.setForeground(new java.awt.Color(82, 81, 81));
        tituloUsuarioLogin.setText("Usuario");

        iconoPasswordLogin.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icono_contrasena.png"))); // NOI18N

        tituloPasswordLogin.setFont(new java.awt.Font("Titillium Web", 0, 18)); // NOI18N
        tituloPasswordLogin.setForeground(new java.awt.Color(82, 81, 81));
        tituloPasswordLogin.setText("Contraseña");

        etiquetaMensajeErrorLogin.setFont(new java.awt.Font("Titillium Web", 0, 10)); // NOI18N
        etiquetaMensajeErrorLogin.setForeground(new java.awt.Color(204, 51, 0));
        etiquetaMensajeErrorLogin.setText("errorLogin");

        textFieldUsuarioLogin.setFont(new java.awt.Font("Titillium Web", 0, 14)); // NOI18N
        textFieldUsuarioLogin.setForeground(new java.awt.Color(82, 81, 81));
        textFieldUsuarioLogin.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(82, 81, 81)));
        textFieldUsuarioLogin.setCaretColor(new java.awt.Color(244, 107, 82));
        textFieldUsuarioLogin.setSelectionColor(new java.awt.Color(244, 107, 82));

        passwordFieldPasswordLogin.setFont(new java.awt.Font("Titillium Web", 0, 14)); // NOI18N
        passwordFieldPasswordLogin.setForeground(new java.awt.Color(82, 81, 81));
        passwordFieldPasswordLogin.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(82, 81, 81)));
        passwordFieldPasswordLogin.setCaretColor(new java.awt.Color(244, 107, 82));
        passwordFieldPasswordLogin.setSelectionColor(new java.awt.Color(244, 107, 82));

        checkBoxRecuerdame.setFont(new java.awt.Font("Titillium Web", 0, 12)); // NOI18N
        checkBoxRecuerdame.setForeground(new java.awt.Color(82, 81, 81));
        checkBoxRecuerdame.setText("Recordarme");
        checkBoxRecuerdame.setBorder(null);
        checkBoxRecuerdame.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        checkBoxRecuerdame.setFocusPainted(false);

        javax.swing.GroupLayout panelInformacionLoginLayout = new javax.swing.GroupLayout(panelInformacionLogin);
        panelInformacionLogin.setLayout(panelInformacionLoginLayout);
        panelInformacionLoginLayout.setHorizontalGroup(
            panelInformacionLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInformacionLoginLayout.createSequentialGroup()
                .addGroup(panelInformacionLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelInformacionLoginLayout.createSequentialGroup()
                        .addGap(99, 99, 99)
                        .addGroup(panelInformacionLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(panelInformacionLoginLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(etiquetaMensajeErrorLogin))
                            .addGroup(panelInformacionLoginLayout.createSequentialGroup()
                                .addComponent(iconoUsuarioLogin)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tituloUsuarioLogin))
                            .addGroup(panelInformacionLoginLayout.createSequentialGroup()
                                .addComponent(iconoPasswordLogin)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tituloPasswordLogin))
                            .addComponent(passwordFieldPasswordLogin, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                            .addComponent(textFieldUsuarioLogin)))
                    .addGroup(panelInformacionLoginLayout.createSequentialGroup()
                        .addGap(181, 181, 181)
                        .addComponent(checkBoxRecuerdame)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelInformacionLoginLayout.setVerticalGroup(
            panelInformacionLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInformacionLoginLayout.createSequentialGroup()
                .addGap(95, 95, 95)
                .addGroup(panelInformacionLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(iconoUsuarioLogin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tituloUsuarioLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textFieldUsuarioLogin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(49, 49, 49)
                .addGroup(panelInformacionLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(iconoPasswordLogin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tituloPasswordLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(passwordFieldPasswordLogin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(etiquetaMensajeErrorLogin)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 58, Short.MAX_VALUE)
                .addComponent(checkBoxRecuerdame)
                .addGap(43, 43, 43))
        );

        panelPerfilLogin.setBackground(new java.awt.Color(240, 238, 234));

        imagenLogin.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/imagen_perfil.png"))); // NOI18N

        javax.swing.GroupLayout panelPerfilLoginLayout = new javax.swing.GroupLayout(panelPerfilLogin);
        panelPerfilLogin.setLayout(panelPerfilLoginLayout);
        panelPerfilLoginLayout.setHorizontalGroup(
            panelPerfilLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPerfilLoginLayout.createSequentialGroup()
                .addGap(165, 165, 165)
                .addComponent(imagenLogin)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelPerfilLoginLayout.setVerticalGroup(
            panelPerfilLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPerfilLoginLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(imagenLogin)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        panelSeparadorLogin.setBackground(new java.awt.Color(240, 238, 234));

        javax.swing.GroupLayout panelSeparadorLoginLayout = new javax.swing.GroupLayout(panelSeparadorLogin);
        panelSeparadorLogin.setLayout(panelSeparadorLoginLayout);
        panelSeparadorLoginLayout.setHorizontalGroup(
            panelSeparadorLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        panelSeparadorLoginLayout.setVerticalGroup(
            panelSeparadorLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 34, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout contenedorLoginLayout = new javax.swing.GroupLayout(contenedorLogin);
        contenedorLogin.setLayout(contenedorLoginLayout);
        contenedorLoginLayout.setHorizontalGroup(
            contenedorLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contenedorLoginLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(contenedorLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelPerfilLogin, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelBotonesLogin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelInformacionLogin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelSeparadorLogin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(6, 6, 6))
        );
        contenedorLoginLayout.setVerticalGroup(
            contenedorLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contenedorLoginLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(panelSeparadorLogin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(panelPerfilLogin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelInformacionLogin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelBotonesLogin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6))
        );

        contenedorPrincipal.add(contenedorLogin, "card2");

        javax.swing.GroupLayout contenedorTotalLayout = new javax.swing.GroupLayout(contenedorTotal);
        contenedorTotal.setLayout(contenedorTotalLayout);
        contenedorTotalLayout.setHorizontalGroup(
            contenedorTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(contenedorFooterSesion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(contenedorTotalLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(contenedorPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(6, 6, 6))
        );
        contenedorTotalLayout.setVerticalGroup(
            contenedorTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contenedorTotalLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(contenedorPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(6, 6, 6)
                .addComponent(contenedorFooterSesion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        lienzoSesion.add(contenedorTotal, "card2");

        javax.swing.GroupLayout SesionLayout = new javax.swing.GroupLayout(Sesion.getContentPane());
        Sesion.getContentPane().setLayout(SesionLayout);
        SesionLayout.setHorizontalGroup(
            SesionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lienzoSesion, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        SesionLayout.setVerticalGroup(
            SesionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lienzoSesion, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Traductor");
        setBackground(new java.awt.Color(0, 204, 255));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        lienzo.setBackground(new java.awt.Color(240, 238, 234));
        lienzo.setPreferredSize(new java.awt.Dimension(907, 400));

        contenedorHeader.setBackground(new java.awt.Color(240, 238, 234));
        contenedorHeader.setPreferredSize(new java.awt.Dimension(907, 66));

        tituloSeccion.setFont(new java.awt.Font("DM Serif Display", 2, 36)); // NOI18N
        tituloSeccion.setForeground(new java.awt.Color(244, 107, 82));
        tituloSeccion.setText("Traducir");

        seccionCuenta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/cuenta_seccion.png"))); // NOI18N
        seccionCuenta.setToolTipText("Traducir");
        seccionCuenta.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        seccionCuenta.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                seccionCuentaMouseClicked(evt);
            }
        });

        seccionTraducir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/traducir_seccion.png"))); // NOI18N
        seccionTraducir.setToolTipText("Traducir");
        seccionTraducir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        seccionTraducir.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                seccionTraducirMouseClicked(evt);
            }
        });

        seccionPalabras.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/lista_seccion.png"))); // NOI18N
        seccionPalabras.setToolTipText("Palabras");
        seccionPalabras.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        seccionPalabras.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                seccionPalabrasMouseClicked(evt);
            }
        });

        seccionSugerenciasStaff.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/sugerencias_seccion.png"))); // NOI18N
        seccionSugerenciasStaff.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        seccionSugerenciasStaff.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                seccionSugerenciasStaffMouseClicked(evt);
            }
        });

        jSeparator7.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jSeparator8.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jSeparator9.setOrientation(javax.swing.SwingConstants.VERTICAL);

        javax.swing.GroupLayout contenedorHeaderLayout = new javax.swing.GroupLayout(contenedorHeader);
        contenedorHeader.setLayout(contenedorHeaderLayout);
        contenedorHeaderLayout.setHorizontalGroup(
            contenedorHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contenedorHeaderLayout.createSequentialGroup()
                .addGap(70, 70, 70)
                .addComponent(tituloSeccion, javax.swing.GroupLayout.PREFERRED_SIZE, 420, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 105, Short.MAX_VALUE)
                .addComponent(seccionTraducir)
                .addGap(18, 18, 18)
                .addComponent(jSeparator8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(seccionPalabras)
                .addGap(18, 18, 18)
                .addComponent(jSeparator7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(seccionSugerenciasStaff)
                .addGap(18, 18, 18)
                .addComponent(jSeparator9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(seccionCuenta)
                .addGap(70, 70, 70))
        );
        contenedorHeaderLayout.setVerticalGroup(
            contenedorHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contenedorHeaderLayout.createSequentialGroup()
                .addContainerGap(17, Short.MAX_VALUE)
                .addGroup(contenedorHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contenedorHeaderLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(tituloSeccion, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(17, 17, 17))
                    .addGroup(contenedorHeaderLayout.createSequentialGroup()
                        .addGroup(contenedorHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(contenedorHeaderLayout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addComponent(jSeparator9, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(contenedorHeaderLayout.createSequentialGroup()
                                .addGap(9, 9, 9)
                                .addComponent(jSeparator8, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(contenedorHeaderLayout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addComponent(jSeparator7, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(seccionPalabras)
                            .addComponent(seccionTraducir)
                            .addComponent(seccionSugerenciasStaff)
                            .addComponent(seccionCuenta))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );

        contenedorDeSecciones.setBackground(new java.awt.Color(255, 255, 204));
        contenedorDeSecciones.setPreferredSize(new java.awt.Dimension(907, 308));
        contenedorDeSecciones.setLayout(new java.awt.CardLayout());

        contenedorTraduccion.setBackground(new java.awt.Color(240, 238, 234));
        contenedorTraduccion.setPreferredSize(new java.awt.Dimension(907, 308));

        cuadroInput.setBackground(new java.awt.Color(240, 238, 234));
        cuadroInput.setPreferredSize(new java.awt.Dimension(350, 225));

        idiomasInput.setBackground(new java.awt.Color(244, 107, 82));
        idiomasInput.setFont(new java.awt.Font("DM Sans", 0, 14)); // NOI18N
        idiomasInput.setForeground(new java.awt.Color(240, 238, 234));
        idiomasInput.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Idioma", "Español", "Ingles", "Italiano" }));
        idiomasInput.setToolTipText("Idioma de Entrada");
        idiomasInput.setBorder(null);
        idiomasInput.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        idiomasInput.setRequestFocusEnabled(false);
        idiomasInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                idiomasInputActionPerformed(evt);
            }
        });

        textAreaInput.setColumns(20);
        textAreaInput.setFont(new java.awt.Font("Titillium Web", 0, 14)); // NOI18N
        textAreaInput.setRows(5);
        textAreaInput.setToolTipText("Input");
        textAreaInput.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(82, 81, 81)));
        textAreaInput.setCaretColor(new java.awt.Color(244, 107, 82));
        textAreaInput.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        textAreaInput.setMargin(new java.awt.Insets(10, 10, 10, 10));
        textAreaInput.setSelectionColor(new java.awt.Color(244, 107, 82));

        botonBorrarInput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/boton_borrar.png"))); // NOI18N
        botonBorrarInput.setToolTipText("Borrar");
        botonBorrarInput.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonBorrarInput.setName("Borrar"); // NOI18N
        botonBorrarInput.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonBorrarInputMouseClicked(evt);
            }
        });

        botonTraducir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/boton_traducir.png"))); // NOI18N
        botonTraducir.setToolTipText("Traducir");
        botonTraducir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonTraducir.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonTraducirMouseClicked(evt);
            }
        });

        botonCopiarInput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/boton_copia.png"))); // NOI18N
        botonCopiarInput.setToolTipText("Copiar");
        botonCopiarInput.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonCopiarInput.setName("Borrar"); // NOI18N
        botonCopiarInput.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonCopiarInputMouseClicked(evt);
            }
        });

        iconoIdiomaInput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/global.png"))); // NOI18N

        cantidadCaracteresActuales.setBackground(new java.awt.Color(82, 81, 81));
        cantidadCaracteresActuales.setFont(new java.awt.Font("DM Sans", 0, 12)); // NOI18N
        cantidadCaracteresActuales.setForeground(new java.awt.Color(244, 107, 82));
        cantidadCaracteresActuales.setText("0/200");

        javax.swing.GroupLayout cuadroInputLayout = new javax.swing.GroupLayout(cuadroInput);
        cuadroInput.setLayout(cuadroInputLayout);
        cuadroInputLayout.setHorizontalGroup(
            cuadroInputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cuadroInputLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cuadroInputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cuadroInputLayout.createSequentialGroup()
                        .addComponent(iconoIdiomaInput)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(idiomasInput, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(textAreaInput, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cuadroInputLayout.createSequentialGroup()
                        .addComponent(cantidadCaracteresActuales)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(botonTraducir)
                        .addGap(18, 18, 18)
                        .addComponent(botonCopiarInput)
                        .addGap(18, 18, 18)
                        .addComponent(botonBorrarInput)))
                .addContainerGap())
        );
        cuadroInputLayout.setVerticalGroup(
            cuadroInputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cuadroInputLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cuadroInputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(idiomasInput, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(iconoIdiomaInput))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textAreaInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addGroup(cuadroInputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(botonBorrarInput)
                    .addComponent(botonTraducir)
                    .addComponent(botonCopiarInput)
                    .addComponent(cantidadCaracteresActuales))
                .addGap(9, 9, 9))
        );

        cuadroOutput.setBackground(new java.awt.Color(240, 238, 234));
        cuadroOutput.setPreferredSize(new java.awt.Dimension(350, 225));

        idiomasOutput.setBackground(new java.awt.Color(244, 107, 82));
        idiomasOutput.setFont(new java.awt.Font("DM Sans", 0, 14)); // NOI18N
        idiomasOutput.setForeground(new java.awt.Color(240, 238, 234));
        idiomasOutput.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Idioma", "Español", "Ingles", "Italiano" }));
        idiomasOutput.setToolTipText("Idioma de Salida");
        idiomasOutput.setBorder(null);
        idiomasOutput.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        idiomasOutput.setName(""); // NOI18N
        idiomasOutput.setRequestFocusEnabled(false);
        idiomasOutput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                idiomasOutputActionPerformed(evt);
            }
        });

        textAreaOutput.setColumns(20);
        textAreaOutput.setFont(new java.awt.Font("DM Sans", 0, 14)); // NOI18N
        textAreaOutput.setRows(5);
        textAreaOutput.setToolTipText("Output");
        textAreaOutput.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(82, 81, 81)));
        textAreaOutput.setCaretColor(new java.awt.Color(244, 107, 82));
        textAreaOutput.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        textAreaOutput.setFocusable(false);
        textAreaOutput.setMargin(new java.awt.Insets(10, 10, 10, 10));
        textAreaOutput.setPreferredSize(new java.awt.Dimension(242, 112));
        textAreaOutput.setRequestFocusEnabled(false);
        textAreaOutput.setSelectionColor(new java.awt.Color(244, 107, 82));

        botonBorrarOutput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/boton_borrar.png"))); // NOI18N
        botonBorrarOutput.setToolTipText("Borrar");
        botonBorrarOutput.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonBorrarOutput.setName("Borrar"); // NOI18N
        botonBorrarOutput.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonBorrarOutputMouseClicked(evt);
            }
        });

        botonCopiarOutput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/boton_copia.png"))); // NOI18N
        botonCopiarOutput.setToolTipText("Copiar");
        botonCopiarOutput.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonCopiarOutput.setName("Borrar"); // NOI18N
        botonCopiarOutput.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonCopiarOutputMouseClicked(evt);
            }
        });

        botonSugerirCambios.setBackground(new java.awt.Color(82, 81, 81));
        botonSugerirCambios.setFont(new java.awt.Font("DM Sans", 0, 12)); // NOI18N
        botonSugerirCambios.setForeground(new java.awt.Color(244, 107, 82));
        botonSugerirCambios.setText("Sugerir Traducción");
        botonSugerirCambios.setToolTipText("Sugerir");
        botonSugerirCambios.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonSugerirCambios.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                botonSugerirCambiosFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                botonSugerirCambiosFocusLost(evt);
            }
        });
        botonSugerirCambios.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonSugerirCambiosMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                botonSugerirCambiosMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                botonSugerirCambiosMouseExited(evt);
            }
        });

        botonGuardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/boton_guardar.png"))); // NOI18N
        botonGuardar.setToolTipText("Guardar Traduccion");
        botonGuardar.setAlignmentX(1.0F);
        botonGuardar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonGuardar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonGuardarMouseClicked(evt);
            }
        });

        iconoIdiomaOutput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/global.png"))); // NOI18N

        javax.swing.GroupLayout cuadroOutputLayout = new javax.swing.GroupLayout(cuadroOutput);
        cuadroOutput.setLayout(cuadroOutputLayout);
        cuadroOutputLayout.setHorizontalGroup(
            cuadroOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cuadroOutputLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cuadroOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(textAreaOutput, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
                    .addGroup(cuadroOutputLayout.createSequentialGroup()
                        .addComponent(idiomasOutput, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(iconoIdiomaOutput))
                    .addGroup(cuadroOutputLayout.createSequentialGroup()
                        .addComponent(botonBorrarOutput)
                        .addGap(18, 18, 18)
                        .addComponent(botonCopiarOutput)
                        .addGap(18, 18, 18)
                        .addComponent(botonGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(botonSugerirCambios)))
                .addContainerGap())
        );
        cuadroOutputLayout.setVerticalGroup(
            cuadroOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cuadroOutputLayout.createSequentialGroup()
                .addContainerGap(7, Short.MAX_VALUE)
                .addGroup(cuadroOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(idiomasOutput, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(iconoIdiomaOutput, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textAreaOutput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addGroup(cuadroOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(botonBorrarOutput)
                    .addComponent(botonCopiarOutput)
                    .addComponent(botonGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botonSugerirCambios))
                .addGap(9, 9, 9))
        );

        contenedorCentral.setBackground(new java.awt.Color(240, 238, 234));

        botonInvertir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/swap.png"))); // NOI18N
        botonInvertir.setToolTipText("Invertir");
        botonInvertir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonInvertir.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonInvertirMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout contenedorCentralLayout = new javax.swing.GroupLayout(contenedorCentral);
        contenedorCentral.setLayout(contenedorCentralLayout);
        contenedorCentralLayout.setHorizontalGroup(
            contenedorCentralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contenedorCentralLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(botonInvertir))
        );
        contenedorCentralLayout.setVerticalGroup(
            contenedorCentralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contenedorCentralLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(botonInvertir)
                .addGap(70, 70, 70))
        );

        contenedorMensajes.setBackground(new java.awt.Color(240, 238, 234));
        contenedorMensajes.setPreferredSize(new java.awt.Dimension(907, 18));

        mensajeAccion.setBackground(new java.awt.Color(244, 107, 82));
        mensajeAccion.setFont(new java.awt.Font("DM Sans", 0, 12)); // NOI18N
        mensajeAccion.setForeground(new java.awt.Color(244, 107, 82));
        mensajeAccion.setText("accion");
        mensajeAccion.setToolTipText("Sugerir");
        mensajeAccion.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        javax.swing.GroupLayout contenedorMensajesLayout = new javax.swing.GroupLayout(contenedorMensajes);
        contenedorMensajes.setLayout(contenedorMensajesLayout);
        contenedorMensajesLayout.setHorizontalGroup(
            contenedorMensajesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contenedorMensajesLayout.createSequentialGroup()
                .addGap(438, 438, 438)
                .addComponent(mensajeAccion)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        contenedorMensajesLayout.setVerticalGroup(
            contenedorMensajesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mensajeAccion)
        );

        javax.swing.GroupLayout contenedorTraduccionLayout = new javax.swing.GroupLayout(contenedorTraduccion);
        contenedorTraduccion.setLayout(contenedorTraduccionLayout);
        contenedorTraduccionLayout.setHorizontalGroup(
            contenedorTraduccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contenedorTraduccionLayout.createSequentialGroup()
                .addGap(53, 53, 53)
                .addComponent(cuadroInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(contenedorCentral, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(cuadroOutput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(54, Short.MAX_VALUE))
            .addComponent(contenedorMensajes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        contenedorTraduccionLayout.setVerticalGroup(
            contenedorTraduccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contenedorTraduccionLayout.createSequentialGroup()
                .addGroup(contenedorTraduccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(contenedorTraduccionLayout.createSequentialGroup()
                        .addContainerGap(52, Short.MAX_VALUE)
                        .addGroup(contenedorTraduccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cuadroInput, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                            .addComponent(contenedorCentral, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(34, 34, 34))
                    .addGroup(contenedorTraduccionLayout.createSequentialGroup()
                        .addGap(52, 52, 52)
                        .addComponent(cuadroOutput, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addComponent(contenedorMensajes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        contenedorDeSecciones.add(contenedorTraduccion, "cardTraduccion");

        contenedorCuenta.setBackground(new java.awt.Color(240, 238, 234));

        contenedorDatosPersonales.setBackground(new java.awt.Color(240, 238, 234));

        tituloDatosPersonales.setBackground(new java.awt.Color(82, 81, 81));
        tituloDatosPersonales.setFont(new java.awt.Font("DM Sans", 0, 18)); // NOI18N
        tituloDatosPersonales.setForeground(new java.awt.Color(82, 81, 81));
        tituloDatosPersonales.setText("Datos Personales");

        tituloNombre.setFont(new java.awt.Font("DM Sans", 0, 14)); // NOI18N
        tituloNombre.setForeground(new java.awt.Color(82, 81, 81));
        tituloNombre.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        tituloNombre.setText("Nombre:");

        textFieldNombreDatos.setFont(new java.awt.Font("DM Sans", 0, 14)); // NOI18N
        textFieldNombreDatos.setForeground(new java.awt.Color(82, 81, 81));
        textFieldNombreDatos.setToolTipText("");
        textFieldNombreDatos.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(82, 81, 81)));
        textFieldNombreDatos.setCaretColor(new java.awt.Color(244, 107, 82));
        textFieldNombreDatos.setMargin(new java.awt.Insets(10, 10, 10, 10));
        textFieldNombreDatos.setName(""); // NOI18N
        textFieldNombreDatos.setSelectionColor(new java.awt.Color(244, 107, 82));
        textFieldNombreDatos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textFieldNombreDatosActionPerformed(evt);
            }
        });

        tituloUsuario.setFont(new java.awt.Font("DM Sans", 0, 14)); // NOI18N
        tituloUsuario.setForeground(new java.awt.Color(82, 81, 81));
        tituloUsuario.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        tituloUsuario.setText("Usuario:");

        textFieldUsuarioDatos.setFont(new java.awt.Font("DM Sans", 0, 14)); // NOI18N
        textFieldUsuarioDatos.setForeground(new java.awt.Color(82, 81, 81));
        textFieldUsuarioDatos.setToolTipText("");
        textFieldUsuarioDatos.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(82, 81, 81)));
        textFieldUsuarioDatos.setCaretColor(new java.awt.Color(244, 107, 82));
        textFieldUsuarioDatos.setMargin(new java.awt.Insets(10, 10, 10, 10));
        textFieldUsuarioDatos.setName(""); // NOI18N
        textFieldUsuarioDatos.setSelectionColor(new java.awt.Color(244, 107, 82));

        tituloLengua.setFont(new java.awt.Font("DM Sans", 0, 14)); // NOI18N
        tituloLengua.setForeground(new java.awt.Color(82, 81, 81));
        tituloLengua.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        tituloLengua.setText("Lengua:");

        comboBoxLenguas.setBackground(new java.awt.Color(244, 107, 82));
        comboBoxLenguas.setFont(new java.awt.Font("DM Sans", 0, 12)); // NOI18N
        comboBoxLenguas.setForeground(new java.awt.Color(240, 238, 234));
        comboBoxLenguas.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Idioma", "Español", "Ingles", "Italiano" }));
        comboBoxLenguas.setBorder(null);
        comboBoxLenguas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxLenguasActionPerformed(evt);
            }
        });

        iconoLenguaMaterna.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/global.png"))); // NOI18N

        botonEditarDatos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/boton_editar.png"))); // NOI18N
        botonEditarDatos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonEditarDatos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonEditarDatosMouseClicked(evt);
            }
        });

        tituloEditarDatos.setBackground(new java.awt.Color(82, 81, 81));
        tituloEditarDatos.setFont(new java.awt.Font("DM Sans", 0, 12)); // NOI18N
        tituloEditarDatos.setForeground(new java.awt.Color(82, 81, 81));
        tituloEditarDatos.setText("Editar");
        tituloEditarDatos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tituloEditarDatos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tituloEditarDatosMouseClicked(evt);
            }
        });

        botonConfirmarCambios.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/boton_aprobar.png"))); // NOI18N
        botonConfirmarCambios.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonConfirmarCambios.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonConfirmarCambiosMouseClicked(evt);
            }
        });

        tituloConfirmarCambios.setBackground(new java.awt.Color(82, 81, 81));
        tituloConfirmarCambios.setFont(new java.awt.Font("DM Sans", 0, 12)); // NOI18N
        tituloConfirmarCambios.setForeground(new java.awt.Color(82, 81, 81));
        tituloConfirmarCambios.setText("Confirmar");
        tituloConfirmarCambios.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tituloConfirmarCambios.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tituloConfirmarCambiosMouseClicked(evt);
            }
        });

        botonCancelarCambios.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/boton_rechazar.png"))); // NOI18N
        botonCancelarCambios.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonCancelarCambios.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonCancelarCambiosMouseClicked(evt);
            }
        });

        tituloCancelarCambios.setBackground(new java.awt.Color(82, 81, 81));
        tituloCancelarCambios.setFont(new java.awt.Font("DM Sans", 0, 12)); // NOI18N
        tituloCancelarCambios.setForeground(new java.awt.Color(82, 81, 81));
        tituloCancelarCambios.setText("Cancelar");
        tituloCancelarCambios.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tituloCancelarCambios.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tituloCancelarCambiosMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout contenedorDatosPersonalesLayout = new javax.swing.GroupLayout(contenedorDatosPersonales);
        contenedorDatosPersonales.setLayout(contenedorDatosPersonalesLayout);
        contenedorDatosPersonalesLayout.setHorizontalGroup(
            contenedorDatosPersonalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contenedorDatosPersonalesLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(contenedorDatosPersonalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(contenedorDatosPersonalesLayout.createSequentialGroup()
                        .addGroup(contenedorDatosPersonalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(contenedorDatosPersonalesLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(contenedorDatosPersonalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tituloNombre)
                                    .addComponent(tituloLengua, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addGap(18, 18, 18))
                            .addGroup(contenedorDatosPersonalesLayout.createSequentialGroup()
                                .addGap(42, 42, 42)
                                .addComponent(tituloUsuario)
                                .addGap(22, 22, 22)))
                        .addGroup(contenedorDatosPersonalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(textFieldUsuarioDatos)
                            .addGroup(contenedorDatosPersonalesLayout.createSequentialGroup()
                                .addComponent(comboBoxLenguas, 0, 176, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(iconoLenguaMaterna)
                                .addGap(2, 2, 2))
                            .addComponent(textFieldNombreDatos)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contenedorDatosPersonalesLayout.createSequentialGroup()
                                .addGroup(contenedorDatosPersonalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(tituloCancelarCambios)
                                    .addGroup(contenedorDatosPersonalesLayout.createSequentialGroup()
                                        .addComponent(botonCancelarCambios)
                                        .addGap(9, 9, 9)))
                                .addGroup(contenedorDatosPersonalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(contenedorDatosPersonalesLayout.createSequentialGroup()
                                        .addGap(26, 26, 26)
                                        .addComponent(tituloConfirmarCambios)
                                        .addGap(27, 27, 27))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contenedorDatosPersonalesLayout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(botonConfirmarCambios)
                                        .addGap(39, 39, 39)))
                                .addGroup(contenedorDatosPersonalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tituloEditarDatos)
                                    .addComponent(botonEditarDatos))))
                        .addGap(18, 65, Short.MAX_VALUE))
                    .addGroup(contenedorDatosPersonalesLayout.createSequentialGroup()
                        .addComponent(tituloDatosPersonales)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        contenedorDatosPersonalesLayout.setVerticalGroup(
            contenedorDatosPersonalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contenedorDatosPersonalesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tituloDatosPersonales)
                .addGap(34, 34, 34)
                .addGroup(contenedorDatosPersonalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tituloNombre)
                    .addComponent(textFieldNombreDatos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(contenedorDatosPersonalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tituloUsuario)
                    .addComponent(textFieldUsuarioDatos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(contenedorDatosPersonalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(contenedorDatosPersonalesLayout.createSequentialGroup()
                        .addGroup(contenedorDatosPersonalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tituloLengua)
                            .addComponent(comboBoxLenguas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
                        .addGroup(contenedorDatosPersonalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(contenedorDatosPersonalesLayout.createSequentialGroup()
                                .addComponent(botonEditarDatos)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tituloEditarDatos))
                            .addGroup(contenedorDatosPersonalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contenedorDatosPersonalesLayout.createSequentialGroup()
                                    .addComponent(botonCancelarCambios)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(tituloCancelarCambios))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contenedorDatosPersonalesLayout.createSequentialGroup()
                                    .addComponent(botonConfirmarCambios)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(tituloConfirmarCambios))))
                        .addGap(29, 29, 29))
                    .addGroup(contenedorDatosPersonalesLayout.createSequentialGroup()
                        .addComponent(iconoLenguaMaterna)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        contenedorDatosGenerales.setBackground(new java.awt.Color(240, 238, 234));

        tituloGuardados.setFont(new java.awt.Font("DM Sans", 0, 14)); // NOI18N
        tituloGuardados.setForeground(new java.awt.Color(82, 81, 81));
        tituloGuardados.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        tituloGuardados.setText("Guardados");
        tituloGuardados.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        cantidadGuardados.setFont(new java.awt.Font("DM Sans", 0, 12)); // NOI18N
        cantidadGuardados.setForeground(new java.awt.Color(82, 81, 81));
        cantidadGuardados.setText("%X%");

        botonGuardados.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/favorito_seccion.png"))); // NOI18N
        botonGuardados.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonGuardados.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonGuardadosMouseClicked(evt);
            }
        });

        cantidadSugerencias.setFont(new java.awt.Font("DM Sans", 0, 12)); // NOI18N
        cantidadSugerencias.setForeground(new java.awt.Color(82, 81, 81));
        cantidadSugerencias.setText("%X%");

        tituloSugerencias.setFont(new java.awt.Font("DM Sans", 0, 14)); // NOI18N
        tituloSugerencias.setForeground(new java.awt.Color(82, 81, 81));
        tituloSugerencias.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        tituloSugerencias.setText("Sugerencias");
        tituloSugerencias.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        botonSugerencias.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/sugerencia.png"))); // NOI18N
        botonSugerencias.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonSugerencias.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonSugerenciasMouseClicked(evt);
            }
        });

        cantidadSanciones.setFont(new java.awt.Font("DM Sans", 0, 12)); // NOI18N
        cantidadSanciones.setForeground(new java.awt.Color(82, 81, 81));
        cantidadSanciones.setText("%X%");

        tituloSanciones.setFont(new java.awt.Font("DM Sans", 0, 14)); // NOI18N
        tituloSanciones.setForeground(new java.awt.Color(82, 81, 81));
        tituloSanciones.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        tituloSanciones.setText("Sanciones");
        tituloSanciones.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        botonSanciones.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/sancion.png"))); // NOI18N
        botonSanciones.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        cantidadBusquedas.setFont(new java.awt.Font("DM Sans", 0, 12)); // NOI18N
        cantidadBusquedas.setForeground(new java.awt.Color(82, 81, 81));
        cantidadBusquedas.setText("%X%");

        tituloBusquedas.setFont(new java.awt.Font("DM Sans", 0, 14)); // NOI18N
        tituloBusquedas.setForeground(new java.awt.Color(82, 81, 81));
        tituloBusquedas.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        tituloBusquedas.setText("Busquedas");
        tituloBusquedas.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        botonBusquedas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/historial_seccion.png"))); // NOI18N
        botonBusquedas.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonBusquedas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonBusquedasMouseClicked(evt);
            }
        });

        jSeparator5.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        etiquetaCerrarSesion.setFont(new java.awt.Font("DM Sans", 0, 12)); // NOI18N
        etiquetaCerrarSesion.setForeground(new java.awt.Color(82, 81, 81));
        etiquetaCerrarSesion.setText("Cerrar Sesión");
        etiquetaCerrarSesion.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        etiquetaCerrarSesion.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                etiquetaCerrarSesionFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                etiquetaCerrarSesionFocusLost(evt);
            }
        });
        etiquetaCerrarSesion.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                etiquetaCerrarSesionMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                etiquetaCerrarSesionMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                etiquetaCerrarSesionMouseExited(evt);
            }
        });

        etiquetaStaffPanel.setFont(new java.awt.Font("DM Sans", 0, 12)); // NOI18N
        etiquetaStaffPanel.setForeground(new java.awt.Color(82, 81, 81));
        etiquetaStaffPanel.setText("Staff Panel");
        etiquetaStaffPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        etiquetaStaffPanel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                etiquetaStaffPanelFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                etiquetaStaffPanelFocusLost(evt);
            }
        });
        etiquetaStaffPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                etiquetaStaffPanelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                etiquetaStaffPanelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                etiquetaStaffPanelMouseExited(evt);
            }
        });

        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);

        botonCerrarSesion.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/cerrar-sesion.png"))); // NOI18N
        botonCerrarSesion.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonCerrarSesion.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                botonCerrarSesionFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                botonCerrarSesionFocusLost(evt);
            }
        });
        botonCerrarSesion.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonCerrarSesionMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                botonCerrarSesionMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                botonCerrarSesionMouseExited(evt);
            }
        });

        botonStaffPanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/herramienta-de-reparacion.png"))); // NOI18N
        botonStaffPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonStaffPanel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                botonStaffPanelFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                botonStaffPanelFocusLost(evt);
            }
        });
        botonStaffPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonStaffPanelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                botonStaffPanelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                botonStaffPanelMouseExited(evt);
            }
        });

        javax.swing.GroupLayout contenedorDatosGeneralesLayout = new javax.swing.GroupLayout(contenedorDatosGenerales);
        contenedorDatosGenerales.setLayout(contenedorDatosGeneralesLayout);
        contenedorDatosGeneralesLayout.setHorizontalGroup(
            contenedorDatosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contenedorDatosGeneralesLayout.createSequentialGroup()
                .addGroup(contenedorDatosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(contenedorDatosGeneralesLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(tituloGuardados)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(contenedorDatosGeneralesLayout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(botonGuardados))
                    .addGroup(contenedorDatosGeneralesLayout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addComponent(cantidadGuardados)))
                .addGroup(contenedorDatosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(contenedorDatosGeneralesLayout.createSequentialGroup()
                        .addGap(43, 43, 43)
                        .addComponent(botonSugerencias))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contenedorDatosGeneralesLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
                        .addGroup(contenedorDatosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contenedorDatosGeneralesLayout.createSequentialGroup()
                                .addGroup(contenedorDatosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(etiquetaCerrarSesion, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(tituloSugerencias, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addGap(18, 18, 18))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contenedorDatosGeneralesLayout.createSequentialGroup()
                                .addGroup(contenedorDatosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(botonCerrarSesion)
                                    .addComponent(cantidadSugerencias))
                                .addGap(42, 42, 42)))
                        .addGroup(contenedorDatosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGroup(contenedorDatosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(contenedorDatosGeneralesLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(contenedorDatosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(contenedorDatosGeneralesLayout.createSequentialGroup()
                                .addGap(14, 14, 14)
                                .addComponent(botonSanciones))
                            .addGroup(contenedorDatosGeneralesLayout.createSequentialGroup()
                                .addComponent(tituloSanciones)
                                .addGap(18, 18, 18)
                                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(contenedorDatosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tituloBusquedas)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contenedorDatosGeneralesLayout.createSequentialGroup()
                                .addComponent(botonBusquedas)
                                .addGap(23, 23, 23))))
                    .addGroup(contenedorDatosGeneralesLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(contenedorDatosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(etiquetaStaffPanel)
                            .addGroup(contenedorDatosGeneralesLayout.createSequentialGroup()
                                .addGap(13, 13, 13)
                                .addGroup(contenedorDatosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(cantidadSanciones)
                                    .addComponent(botonStaffPanel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cantidadBusquedas)
                                .addGap(21, 21, 21)))))
                .addGap(17, 17, 17))
        );
        contenedorDatosGeneralesLayout.setVerticalGroup(
            contenedorDatosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contenedorDatosGeneralesLayout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addGroup(contenedorDatosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(contenedorDatosGeneralesLayout.createSequentialGroup()
                        .addComponent(botonBusquedas)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tituloBusquedas)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cantidadBusquedas))
                    .addGroup(contenedorDatosGeneralesLayout.createSequentialGroup()
                        .addComponent(botonSanciones)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(contenedorDatosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(tituloSanciones)
                            .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cantidadSanciones))
                    .addGroup(contenedorDatosGeneralesLayout.createSequentialGroup()
                        .addComponent(botonSugerencias)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(contenedorDatosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(tituloSugerencias)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cantidadSugerencias))
                    .addGroup(contenedorDatosGeneralesLayout.createSequentialGroup()
                        .addComponent(botonGuardados)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(contenedorDatosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(tituloGuardados)
                            .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cantidadGuardados)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(contenedorDatosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contenedorDatosGeneralesLayout.createSequentialGroup()
                        .addGroup(contenedorDatosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(botonCerrarSesion, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(9, 9, 9)
                        .addComponent(etiquetaCerrarSesion))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contenedorDatosGeneralesLayout.createSequentialGroup()
                        .addComponent(botonStaffPanel)
                        .addGap(9, 9, 9)
                        .addComponent(etiquetaStaffPanel)))
                .addGap(42, 42, 42))
        );

        jSeparator6.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator6.setPreferredSize(new java.awt.Dimension(1, 0));

        javax.swing.GroupLayout contenedorCuentaLayout = new javax.swing.GroupLayout(contenedorCuenta);
        contenedorCuenta.setLayout(contenedorCuentaLayout);
        contenedorCuentaLayout.setHorizontalGroup(
            contenedorCuentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contenedorCuentaLayout.createSequentialGroup()
                .addContainerGap(39, Short.MAX_VALUE)
                .addComponent(contenedorDatosPersonales, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addComponent(contenedorDatosGenerales, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
        );
        contenedorCuentaLayout.setVerticalGroup(
            contenedorCuentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contenedorCuentaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(contenedorCuentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(contenedorDatosPersonales, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(contenedorDatosGenerales, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(contenedorCuentaLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(23, Short.MAX_VALUE))
        );

        contenedorDeSecciones.add(contenedorCuenta, "card2");

        contenedorStaffPanel.setBackground(new java.awt.Color(240, 238, 234));
        contenedorStaffPanel.setPreferredSize(new java.awt.Dimension(907, 308));

        tituloStaff.setFont(new java.awt.Font("DM Sans", 0, 12)); // NOI18N
        tituloStaff.setForeground(new java.awt.Color(82, 81, 81));
        tituloStaff.setText("Staff");
        tituloStaff.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tituloStaff.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tituloStaffMouseClicked(evt);
            }
        });

        botonStaff.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/recursos-humanos.png"))); // NOI18N
        botonStaff.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonStaff.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonStaffMouseClicked(evt);
            }
        });

        tituloSacionesGlobales.setBackground(new java.awt.Color(244, 107, 82));
        tituloSacionesGlobales.setForeground(new java.awt.Color(82, 81, 81));
        tituloSacionesGlobales.setText("Sanciones");
        tituloSacionesGlobales.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        botonSancionesGlobales.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/sancion.png"))); // NOI18N
        botonSancionesGlobales.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        tituloSugerenciasGlobales.setForeground(new java.awt.Color(82, 81, 81));
        tituloSugerenciasGlobales.setText("Sugerencias");
        tituloSugerenciasGlobales.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        botonSugerenciasGlobales.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/sugerencias_seccion.png"))); // NOI18N
        botonSugerenciasGlobales.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        tituloVolverStaffPanel.setFont(new java.awt.Font("DM Sans", 0, 12)); // NOI18N
        tituloVolverStaffPanel.setForeground(new java.awt.Color(82, 81, 81));
        tituloVolverStaffPanel.setText("Volver");
        tituloVolverStaffPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tituloVolverStaffPanel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tituloVolverStaffPanelKeyPressed(evt);
            }
        });

        botonVolverStaffPanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/boton_volver.png"))); // NOI18N
        botonVolverStaffPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonVolverStaffPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonVolverStaffPanelMouseClicked(evt);
            }
        });

        jSeparator4.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jSeparator11.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jSeparator10.setOrientation(javax.swing.SwingConstants.VERTICAL);

        subtituloApartadosStaffPanel.setBackground(new java.awt.Color(82, 81, 81));
        subtituloApartadosStaffPanel.setFont(new java.awt.Font("DM Sans", 0, 24)); // NOI18N
        subtituloApartadosStaffPanel.setForeground(new java.awt.Color(82, 81, 81));
        subtituloApartadosStaffPanel.setText("Apartados");

        javax.swing.GroupLayout contenedorStaffPanelLayout = new javax.swing.GroupLayout(contenedorStaffPanel);
        contenedorStaffPanel.setLayout(contenedorStaffPanelLayout);
        contenedorStaffPanelLayout.setHorizontalGroup(
            contenedorStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contenedorStaffPanelLayout.createSequentialGroup()
                .addContainerGap(113, Short.MAX_VALUE)
                .addGroup(contenedorStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(tituloVolverStaffPanel)
                    .addComponent(botonVolverStaffPanel, javax.swing.GroupLayout.Alignment.LEADING))
                .addGap(80, 80, 80)
                .addComponent(jSeparator10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(80, 80, 80)
                .addGroup(contenedorStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tituloStaff)
                    .addGroup(contenedorStaffPanelLayout.createSequentialGroup()
                        .addComponent(botonStaff)
                        .addGap(80, 80, 80)
                        .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(80, 80, 80)
                .addGroup(contenedorStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(contenedorStaffPanelLayout.createSequentialGroup()
                        .addGap(13, 13, 13)
                        .addComponent(botonSancionesGlobales)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jSeparator11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(69, 69, 69))
                    .addGroup(contenedorStaffPanelLayout.createSequentialGroup()
                        .addComponent(tituloSacionesGlobales)
                        .addGap(150, 150, 150)))
                .addGap(10, 10, 10)
                .addGroup(contenedorStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tituloSugerenciasGlobales)
                    .addGroup(contenedorStaffPanelLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(botonSugerenciasGlobales)))
                .addGap(114, 114, 114))
            .addGroup(contenedorStaffPanelLayout.createSequentialGroup()
                .addGap(395, 395, 395)
                .addComponent(subtituloApartadosStaffPanel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        contenedorStaffPanelLayout.setVerticalGroup(
            contenedorStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contenedorStaffPanelLayout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addGroup(contenedorStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(contenedorStaffPanelLayout.createSequentialGroup()
                        .addComponent(botonSugerenciasGlobales)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tituloSugerenciasGlobales))
                    .addGroup(contenedorStaffPanelLayout.createSequentialGroup()
                        .addComponent(subtituloApartadosStaffPanel)
                        .addGap(48, 48, 48)
                        .addGroup(contenedorStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(contenedorStaffPanelLayout.createSequentialGroup()
                                .addGroup(contenedorStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(contenedorStaffPanelLayout.createSequentialGroup()
                                        .addComponent(botonStaff)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                                    .addGroup(contenedorStaffPanelLayout.createSequentialGroup()
                                        .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(7, 7, 7)))
                                .addComponent(tituloStaff))
                            .addGroup(contenedorStaffPanelLayout.createSequentialGroup()
                                .addGroup(contenedorStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jSeparator10, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(contenedorStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(botonSancionesGlobales)
                                        .addComponent(botonVolverStaffPanel)
                                        .addComponent(jSeparator11, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(contenedorStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(tituloSacionesGlobales)
                                    .addComponent(tituloVolverStaffPanel))))))
                .addContainerGap(128, Short.MAX_VALUE))
        );

        contenedorDeSecciones.add(contenedorStaffPanel, "cardHistorial");

        contenedorStaff.setBackground(new java.awt.Color(240, 238, 234));

        tituloBuscarStaff.setFont(new java.awt.Font("DM Sans", 0, 14)); // NOI18N
        tituloBuscarStaff.setForeground(new java.awt.Color(82, 81, 81));
        tituloBuscarStaff.setText("Buscar Usuario:");

        comboBoxUsuarios.setBackground(new java.awt.Color(244, 107, 82));
        comboBoxUsuarios.setFont(new java.awt.Font("DM Sans", 0, 14)); // NOI18N
        comboBoxUsuarios.setForeground(new java.awt.Color(240, 238, 234));
        comboBoxUsuarios.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Usuario" }));
        comboBoxUsuarios.setBorder(null);
        comboBoxUsuarios.setFocusable(false);
        comboBoxUsuarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxUsuariosActionPerformed(evt);
            }
        });

        iconoUsuarioStaff.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/cuenta_seccion.png"))); // NOI18N

        tituloUsuarioStaff.setBackground(new java.awt.Color(82, 81, 81));
        tituloUsuarioStaff.setFont(new java.awt.Font("DM Sans", 1, 12)); // NOI18N
        tituloUsuarioStaff.setForeground(new java.awt.Color(82, 81, 81));
        tituloUsuarioStaff.setText("Usuario");

        placeholderUsuarioStaff.setBackground(new java.awt.Color(82, 81, 81));
        placeholderUsuarioStaff.setFont(new java.awt.Font("DM Sans", 0, 12)); // NOI18N
        placeholderUsuarioStaff.setForeground(new java.awt.Color(82, 81, 81));
        placeholderUsuarioStaff.setText("%usuario%");

        iconoRolStaff.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/insignia.png"))); // NOI18N

        tituloRolStaff.setBackground(new java.awt.Color(82, 81, 81));
        tituloRolStaff.setFont(new java.awt.Font("DM Sans", 1, 12)); // NOI18N
        tituloRolStaff.setForeground(new java.awt.Color(82, 81, 81));
        tituloRolStaff.setText("Rol");

        comboBoxRolStaff.setBackground(new java.awt.Color(244, 107, 82));
        comboBoxRolStaff.setFont(new java.awt.Font("DM Sans", 0, 14)); // NOI18N
        comboBoxRolStaff.setForeground(new java.awt.Color(240, 238, 234));
        comboBoxRolStaff.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Rol", "Usuario", "Traductor", "Moderador", "Administrador" }));
        comboBoxRolStaff.setBorder(null);
        comboBoxRolStaff.setEnabled(false);
        comboBoxRolStaff.setFocusable(false);
        comboBoxRolStaff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxRolStaffActionPerformed(evt);
            }
        });

        iconoEstatusStaff.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/comprobado.png"))); // NOI18N

        tituloEstatusStaff.setBackground(new java.awt.Color(82, 81, 81));
        tituloEstatusStaff.setFont(new java.awt.Font("DM Sans", 1, 12)); // NOI18N
        tituloEstatusStaff.setForeground(new java.awt.Color(82, 81, 81));
        tituloEstatusStaff.setText("Estatus");

        placeholderStatusStaff.setBackground(new java.awt.Color(82, 81, 81));
        placeholderStatusStaff.setFont(new java.awt.Font("DM Sans", 0, 12)); // NOI18N
        placeholderStatusStaff.setForeground(new java.awt.Color(82, 81, 81));
        placeholderStatusStaff.setText("%estatus%");

        iconoSugerenciasAceptadas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/sugerencia.png"))); // NOI18N

        tituloSugerenciasAprobadas.setBackground(new java.awt.Color(82, 81, 81));
        tituloSugerenciasAprobadas.setFont(new java.awt.Font("DM Sans", 1, 12)); // NOI18N
        tituloSugerenciasAprobadas.setForeground(new java.awt.Color(82, 81, 81));
        tituloSugerenciasAprobadas.setText("Sugerencias Aprobadas");

        placeholderSugerenciasAprobadasStaff.setBackground(new java.awt.Color(82, 81, 81));
        placeholderSugerenciasAprobadasStaff.setFont(new java.awt.Font("DM Sans", 0, 12)); // NOI18N
        placeholderSugerenciasAprobadasStaff.setForeground(new java.awt.Color(82, 81, 81));
        placeholderSugerenciasAprobadasStaff.setText("%sug_apro%");

        iconoSugerenciasRechazadasStaff.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/sugerencia.png"))); // NOI18N

        tituloSugerenciasRechazadasStaff.setBackground(new java.awt.Color(82, 81, 81));
        tituloSugerenciasRechazadasStaff.setFont(new java.awt.Font("DM Sans", 1, 12)); // NOI18N
        tituloSugerenciasRechazadasStaff.setForeground(new java.awt.Color(82, 81, 81));
        tituloSugerenciasRechazadasStaff.setText("Sugerencias Rechazadas");

        placeholderSugerenciasRechazadasStaff.setBackground(new java.awt.Color(82, 81, 81));
        placeholderSugerenciasRechazadasStaff.setFont(new java.awt.Font("DM Sans", 0, 12)); // NOI18N
        placeholderSugerenciasRechazadasStaff.setForeground(new java.awt.Color(82, 81, 81));
        placeholderSugerenciasRechazadasStaff.setText("%sug_rec%");

        iconoSancionesEmitidasStaff.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/sancion.png"))); // NOI18N

        tituloSancionesEmitidasStaff.setBackground(new java.awt.Color(82, 81, 81));
        tituloSancionesEmitidasStaff.setFont(new java.awt.Font("DM Sans", 1, 12)); // NOI18N
        tituloSancionesEmitidasStaff.setForeground(new java.awt.Color(82, 81, 81));
        tituloSancionesEmitidasStaff.setText("Sanciones Emitidas");

        placeholderSancionesEmitidasStaff.setBackground(new java.awt.Color(82, 81, 81));
        placeholderSancionesEmitidasStaff.setFont(new java.awt.Font("DM Sans", 0, 12)); // NOI18N
        placeholderSancionesEmitidasStaff.setForeground(new java.awt.Color(82, 81, 81));
        placeholderSancionesEmitidasStaff.setText("%san_emi%");

        iconoStaffDesdeStaff.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/sancion.png"))); // NOI18N

        tituloStaffDesdeStaff.setBackground(new java.awt.Color(82, 81, 81));
        tituloStaffDesdeStaff.setFont(new java.awt.Font("DM Sans", 1, 12)); // NOI18N
        tituloStaffDesdeStaff.setForeground(new java.awt.Color(82, 81, 81));
        tituloStaffDesdeStaff.setText("Staff desde");

        placeholderStaffDesdeStaff.setBackground(new java.awt.Color(82, 81, 81));
        placeholderStaffDesdeStaff.setFont(new java.awt.Font("DM Sans", 0, 12)); // NOI18N
        placeholderStaffDesdeStaff.setForeground(new java.awt.Color(82, 81, 81));
        placeholderStaffDesdeStaff.setText("%sta_des%");

        iconoStaffHastaStaff.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/sancion.png"))); // NOI18N

        tituloStaffHastaStaff.setBackground(new java.awt.Color(82, 81, 81));
        tituloStaffHastaStaff.setFont(new java.awt.Font("DM Sans", 1, 12)); // NOI18N
        tituloStaffHastaStaff.setForeground(new java.awt.Color(82, 81, 81));
        tituloStaffHastaStaff.setText("Staff hasta");

        placeholderStaffHastaStaff.setBackground(new java.awt.Color(82, 81, 81));
        placeholderStaffHastaStaff.setFont(new java.awt.Font("DM Sans", 0, 12)); // NOI18N
        placeholderStaffHastaStaff.setForeground(new java.awt.Color(82, 81, 81));
        placeholderStaffHastaStaff.setText("%sta_has%");

        iconoListarStaff.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/lista.png"))); // NOI18N

        botonEditarRol.setFont(new java.awt.Font("DM Sans", 1, 12)); // NOI18N
        botonEditarRol.setText("Editar Rol");

        botonCancelarEditarRol.setText("Cancelar");

        botonConfirmarEditarRol.setText("Confirmar");

        placeholderEsStaff.setBackground(new java.awt.Color(82, 81, 81));
        placeholderEsStaff.setFont(new java.awt.Font("DM Sans", 0, 12)); // NOI18N
        placeholderEsStaff.setForeground(new java.awt.Color(82, 81, 81));
        placeholderEsStaff.setText("%conf%");

        tituloEsStaff.setBackground(new java.awt.Color(82, 81, 81));
        tituloEsStaff.setFont(new java.awt.Font("DM Sans", 1, 12)); // NOI18N
        tituloEsStaff.setForeground(new java.awt.Color(82, 81, 81));
        tituloEsStaff.setText("Staff");

        iconoEsStaff.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/comprobado.png"))); // NOI18N

        javax.swing.GroupLayout contenedorStaffLayout = new javax.swing.GroupLayout(contenedorStaff);
        contenedorStaff.setLayout(contenedorStaffLayout);
        contenedorStaffLayout.setHorizontalGroup(
            contenedorStaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contenedorStaffLayout.createSequentialGroup()
                .addGap(0, 133, Short.MAX_VALUE)
                .addGroup(contenedorStaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(contenedorStaffLayout.createSequentialGroup()
                        .addGroup(contenedorStaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(iconoRolStaff)
                            .addComponent(iconoUsuarioStaff))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(contenedorStaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(placeholderUsuarioStaff)
                            .addComponent(tituloUsuarioStaff)
                            .addComponent(tituloRolStaff)
                            .addComponent(comboBoxRolStaff, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(contenedorStaffLayout.createSequentialGroup()
                        .addComponent(iconoEsStaff)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(contenedorStaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tituloEsStaff)
                            .addComponent(placeholderEsStaff))))
                .addGap(101, 101, 101)
                .addGroup(contenedorStaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(contenedorStaffLayout.createSequentialGroup()
                        .addComponent(iconoSugerenciasRechazadasStaff)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(contenedorStaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tituloSugerenciasRechazadasStaff)
                            .addComponent(placeholderSugerenciasRechazadasStaff)))
                    .addGroup(contenedorStaffLayout.createSequentialGroup()
                        .addComponent(iconoSugerenciasAceptadas)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(contenedorStaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tituloSugerenciasAprobadas)
                            .addComponent(placeholderSugerenciasAprobadasStaff)))
                    .addGroup(contenedorStaffLayout.createSequentialGroup()
                        .addComponent(iconoEstatusStaff)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(contenedorStaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tituloEstatusStaff)
                            .addComponent(placeholderStatusStaff))))
                .addGap(99, 99, 99)
                .addGroup(contenedorStaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(contenedorStaffLayout.createSequentialGroup()
                        .addComponent(iconoStaffDesdeStaff)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(contenedorStaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tituloStaffDesdeStaff)
                            .addComponent(placeholderStaffDesdeStaff)))
                    .addGroup(contenedorStaffLayout.createSequentialGroup()
                        .addComponent(iconoStaffHastaStaff)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(contenedorStaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tituloStaffHastaStaff)
                            .addComponent(placeholderStaffHastaStaff)))
                    .addGroup(contenedorStaffLayout.createSequentialGroup()
                        .addComponent(iconoSancionesEmitidasStaff)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(contenedorStaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tituloSancionesEmitidasStaff)
                            .addComponent(placeholderSancionesEmitidasStaff))))
                .addGap(88, 88, 88))
            .addGroup(contenedorStaffLayout.createSequentialGroup()
                .addGroup(contenedorStaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(contenedorStaffLayout.createSequentialGroup()
                        .addGap(57, 57, 57)
                        .addComponent(tituloBuscarStaff)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboBoxUsuarios, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(iconoListarStaff))
                    .addGroup(contenedorStaffLayout.createSequentialGroup()
                        .addGap(313, 313, 313)
                        .addComponent(botonCancelarEditarRol)
                        .addGap(60, 60, 60)
                        .addComponent(botonEditarRol)
                        .addGap(60, 60, 60)
                        .addComponent(botonConfirmarEditarRol)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        contenedorStaffLayout.setVerticalGroup(
            contenedorStaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contenedorStaffLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(contenedorStaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(iconoListarStaff)
                    .addGroup(contenedorStaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(tituloBuscarStaff)
                        .addComponent(comboBoxUsuarios, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(32, 32, 32)
                .addGroup(contenedorStaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(contenedorStaffLayout.createSequentialGroup()
                        .addComponent(tituloUsuarioStaff)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(placeholderUsuarioStaff))
                    .addComponent(iconoUsuarioStaff)
                    .addGroup(contenedorStaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(iconoStaffDesdeStaff)
                        .addGroup(contenedorStaffLayout.createSequentialGroup()
                            .addComponent(tituloStaffDesdeStaff)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(placeholderStaffDesdeStaff)))
                    .addGroup(contenedorStaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(iconoEstatusStaff)
                        .addGroup(contenedorStaffLayout.createSequentialGroup()
                            .addComponent(tituloEstatusStaff)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(placeholderStatusStaff))))
                .addGap(18, 18, 18)
                .addGroup(contenedorStaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(contenedorStaffLayout.createSequentialGroup()
                        .addGroup(contenedorStaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(iconoRolStaff)
                            .addGroup(contenedorStaffLayout.createSequentialGroup()
                                .addComponent(tituloRolStaff)
                                .addGap(1, 1, 1)
                                .addComponent(comboBoxRolStaff, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(contenedorStaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, contenedorStaffLayout.createSequentialGroup()
                                    .addComponent(tituloStaffHastaStaff)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(placeholderStaffHastaStaff))
                                .addComponent(iconoStaffHastaStaff, javax.swing.GroupLayout.Alignment.LEADING)))
                        .addGap(18, 18, 18)
                        .addGroup(contenedorStaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(contenedorStaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(iconoSancionesEmitidasStaff)
                                .addGroup(contenedorStaffLayout.createSequentialGroup()
                                    .addComponent(tituloSancionesEmitidasStaff)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(placeholderSancionesEmitidasStaff)))
                            .addGroup(contenedorStaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(iconoEsStaff)
                                .addGroup(contenedorStaffLayout.createSequentialGroup()
                                    .addComponent(tituloEsStaff)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(placeholderEsStaff)))))
                    .addGroup(contenedorStaffLayout.createSequentialGroup()
                        .addGroup(contenedorStaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(iconoSugerenciasAceptadas)
                            .addGroup(contenedorStaffLayout.createSequentialGroup()
                                .addComponent(tituloSugerenciasAprobadas)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(placeholderSugerenciasAprobadasStaff)))
                        .addGap(18, 18, 18)
                        .addGroup(contenedorStaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(iconoSugerenciasRechazadasStaff)
                            .addGroup(contenedorStaffLayout.createSequentialGroup()
                                .addComponent(tituloSugerenciasRechazadasStaff)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(placeholderSugerenciasRechazadasStaff)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE)
                .addGroup(contenedorStaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(botonEditarRol)
                    .addComponent(botonCancelarEditarRol)
                    .addComponent(botonConfirmarEditarRol))
                .addGap(20, 20, 20))
        );

        contenedorDeSecciones.add(contenedorStaff, "card2");

        contenedorPalabras.setBackground(new java.awt.Color(240, 238, 234));
        contenedorPalabras.setPreferredSize(new java.awt.Dimension(907, 308));

        tablaPalabras.setBackground(new java.awt.Color(240, 238, 234));
        tablaPalabras.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(82, 81, 81)));
        tablaPalabras.setFont(new java.awt.Font("Titillium Web", 0, 12)); // NOI18N
        tablaPalabras.setForeground(new java.awt.Color(82, 81, 81));
        tablaPalabras.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tablaPalabras.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tablaPalabras.setGridColor(new java.awt.Color(82, 81, 81));
        tablaPalabras.setSelectionBackground(new java.awt.Color(244, 107, 82));
        tablaPalabras.setSelectionForeground(new java.awt.Color(82, 81, 81));
        tablaPalabras.getTableHeader().setResizingAllowed(false);
        tablaPalabras.getTableHeader().setReorderingAllowed(false);
        scrollPanelPalabras.setViewportView(tablaPalabras);

        tituloVaciarPalabras.setFont(new java.awt.Font("Titillium Web", 0, 12)); // NOI18N
        tituloVaciarPalabras.setForeground(new java.awt.Color(82, 81, 81));
        tituloVaciarPalabras.setText("Vaciar");
        tituloVaciarPalabras.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tituloVaciarPalabras.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tituloVaciarPalabrasFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tituloVaciarPalabrasFocusLost(evt);
            }
        });
        tituloVaciarPalabras.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tituloVaciarPalabrasMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tituloVaciarPalabrasMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                tituloVaciarPalabrasMouseExited(evt);
            }
        });

        botonVaciarPalabras.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/boton_vaciar.png"))); // NOI18N
        botonVaciarPalabras.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonVaciarPalabras.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonVaciarPalabrasMouseClicked(evt);
            }
        });

        botonRemoverPalabra.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/boton_eliminar.png"))); // NOI18N
        botonRemoverPalabra.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonRemoverPalabra.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonRemoverPalabraMouseClicked(evt);
            }
        });

        tituloRemoverPalabra.setFont(new java.awt.Font("Titillium Web", 0, 12)); // NOI18N
        tituloRemoverPalabra.setForeground(new java.awt.Color(82, 81, 81));
        tituloRemoverPalabra.setText("Quitar");
        tituloRemoverPalabra.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tituloRemoverPalabra.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tituloRemoverPalabraFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tituloRemoverPalabraFocusLost(evt);
            }
        });
        tituloRemoverPalabra.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tituloRemoverPalabraMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tituloRemoverPalabraMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                tituloRemoverPalabraMouseExited(evt);
            }
        });

        javax.swing.GroupLayout contenedorPalabrasLayout = new javax.swing.GroupLayout(contenedorPalabras);
        contenedorPalabras.setLayout(contenedorPalabrasLayout);
        contenedorPalabrasLayout.setHorizontalGroup(
            contenedorPalabrasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contenedorPalabrasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPanelPalabras, javax.swing.GroupLayout.PREFERRED_SIZE, 825, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(contenedorPalabrasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tituloVaciarPalabras)
                    .addComponent(botonVaciarPalabras)
                    .addComponent(tituloRemoverPalabra)
                    .addComponent(botonRemoverPalabra))
                .addContainerGap(26, Short.MAX_VALUE))
        );
        contenedorPalabrasLayout.setVerticalGroup(
            contenedorPalabrasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contenedorPalabrasLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(contenedorPalabrasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(contenedorPalabrasLayout.createSequentialGroup()
                        .addComponent(botonRemoverPalabra)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tituloRemoverPalabra)
                        .addGap(18, 18, 18)
                        .addComponent(botonVaciarPalabras)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tituloVaciarPalabras)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(scrollPanelPalabras, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE))
                .addContainerGap())
        );

        contenedorDeSecciones.add(contenedorPalabras, "cardHistorial");

        contenedorSugerenciasPersonales.setBackground(new java.awt.Color(240, 238, 234));
        contenedorSugerenciasPersonales.setPreferredSize(new java.awt.Dimension(907, 308));

        tablaSugerenciasPersonales.setBackground(new java.awt.Color(240, 238, 234));
        tablaSugerenciasPersonales.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(82, 81, 81)));
        tablaSugerenciasPersonales.setFont(new java.awt.Font("Titillium Web", 0, 12)); // NOI18N
        tablaSugerenciasPersonales.setForeground(new java.awt.Color(82, 81, 81));
        tablaSugerenciasPersonales.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tablaSugerenciasPersonales.setToolTipText("");
        tablaSugerenciasPersonales.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tablaSugerenciasPersonales.setGridColor(new java.awt.Color(82, 81, 81));
        tablaSugerenciasPersonales.setSelectionBackground(new java.awt.Color(244, 107, 82));
        tablaSugerenciasPersonales.setSelectionForeground(new java.awt.Color(82, 81, 81));
        scrollPanelSugerenciasPersonales.setViewportView(tablaSugerenciasPersonales);

        tituloVolverSugerenciasPersonales.setFont(new java.awt.Font("Titillium Web", 0, 12)); // NOI18N
        tituloVolverSugerenciasPersonales.setForeground(new java.awt.Color(82, 81, 81));
        tituloVolverSugerenciasPersonales.setText("Volver");
        tituloVolverSugerenciasPersonales.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tituloVolverSugerenciasPersonales.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tituloVolverSugerenciasPersonalesFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tituloVolverSugerenciasPersonalesFocusLost(evt);
            }
        });
        tituloVolverSugerenciasPersonales.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tituloVolverSugerenciasPersonalesMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tituloVolverSugerenciasPersonalesMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                tituloVolverSugerenciasPersonalesMouseExited(evt);
            }
        });

        botonVolverSugerenciasPersonales.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/boton_volver.png"))); // NOI18N
        botonVolverSugerenciasPersonales.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonVolverSugerenciasPersonales.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonVolverSugerenciasPersonalesMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout contenedorSugerenciasPersonalesLayout = new javax.swing.GroupLayout(contenedorSugerenciasPersonales);
        contenedorSugerenciasPersonales.setLayout(contenedorSugerenciasPersonalesLayout);
        contenedorSugerenciasPersonalesLayout.setHorizontalGroup(
            contenedorSugerenciasPersonalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contenedorSugerenciasPersonalesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPanelSugerenciasPersonales, javax.swing.GroupLayout.PREFERRED_SIZE, 825, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(contenedorSugerenciasPersonalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tituloVolverSugerenciasPersonales)
                    .addComponent(botonVolverSugerenciasPersonales))
                .addContainerGap(26, Short.MAX_VALUE))
        );
        contenedorSugerenciasPersonalesLayout.setVerticalGroup(
            contenedorSugerenciasPersonalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contenedorSugerenciasPersonalesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPanelSugerenciasPersonales, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(contenedorSugerenciasPersonalesLayout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addComponent(botonVolverSugerenciasPersonales)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tituloVolverSugerenciasPersonales)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        contenedorDeSecciones.add(contenedorSugerenciasPersonales, "cardHistorial");

        contenedorHistorial.setBackground(new java.awt.Color(240, 238, 234));
        contenedorHistorial.setPreferredSize(new java.awt.Dimension(907, 308));

        tablaHistorial.setBackground(new java.awt.Color(240, 238, 234));
        tablaHistorial.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(82, 81, 81)));
        tablaHistorial.setFont(new java.awt.Font("Titillium Web", 0, 12)); // NOI18N
        tablaHistorial.setForeground(new java.awt.Color(82, 81, 81));
        tablaHistorial.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tablaHistorial.setToolTipText("");
        tablaHistorial.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tablaHistorial.setGridColor(new java.awt.Color(82, 81, 81));
        tablaHistorial.setSelectionBackground(new java.awt.Color(244, 107, 82));
        tablaHistorial.setSelectionForeground(new java.awt.Color(82, 81, 81));
        scrollPanelHistorial.setViewportView(tablaHistorial);

        tituloVaciarHistorial.setFont(new java.awt.Font("Titillium Web", 0, 12)); // NOI18N
        tituloVaciarHistorial.setForeground(new java.awt.Color(82, 81, 81));
        tituloVaciarHistorial.setText("Vaciar");
        tituloVaciarHistorial.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tituloVaciarHistorial.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tituloVaciarHistorialFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tituloVaciarHistorialFocusLost(evt);
            }
        });
        tituloVaciarHistorial.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tituloVaciarHistorialMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tituloVaciarHistorialMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                tituloVaciarHistorialMouseExited(evt);
            }
        });

        botonVaciarHistorial.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/boton_vaciar.png"))); // NOI18N
        botonVaciarHistorial.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonVaciarHistorial.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonVaciarHistorialMouseClicked(evt);
            }
        });

        botonRemoverElementoHistorial.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/boton_eliminar.png"))); // NOI18N
        botonRemoverElementoHistorial.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonRemoverElementoHistorial.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonRemoverElementoHistorialMouseClicked(evt);
            }
        });

        tituloRemoverElementoHistorial.setFont(new java.awt.Font("Titillium Web", 0, 12)); // NOI18N
        tituloRemoverElementoHistorial.setForeground(new java.awt.Color(82, 81, 81));
        tituloRemoverElementoHistorial.setText("Quitar");
        tituloRemoverElementoHistorial.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tituloRemoverElementoHistorial.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tituloRemoverElementoHistorialFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tituloRemoverElementoHistorialFocusLost(evt);
            }
        });
        tituloRemoverElementoHistorial.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tituloRemoverElementoHistorialMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tituloRemoverElementoHistorialMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                tituloRemoverElementoHistorialMouseExited(evt);
            }
        });

        tituloVolverHistorial.setFont(new java.awt.Font("Titillium Web", 0, 12)); // NOI18N
        tituloVolverHistorial.setForeground(new java.awt.Color(82, 81, 81));
        tituloVolverHistorial.setText("Volver");
        tituloVolverHistorial.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tituloVolverHistorial.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tituloVolverHistorialFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tituloVolverHistorialFocusLost(evt);
            }
        });
        tituloVolverHistorial.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tituloVolverHistorialMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tituloVolverHistorialMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                tituloVolverHistorialMouseExited(evt);
            }
        });

        botonVolverHistorial.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/boton_volver.png"))); // NOI18N
        botonVolverHistorial.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonVolverHistorial.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonVolverHistorialMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout contenedorHistorialLayout = new javax.swing.GroupLayout(contenedorHistorial);
        contenedorHistorial.setLayout(contenedorHistorialLayout);
        contenedorHistorialLayout.setHorizontalGroup(
            contenedorHistorialLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contenedorHistorialLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPanelHistorial, javax.swing.GroupLayout.PREFERRED_SIZE, 825, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(contenedorHistorialLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tituloVaciarHistorial)
                    .addComponent(botonVaciarHistorial)
                    .addComponent(tituloRemoverElementoHistorial)
                    .addComponent(botonRemoverElementoHistorial)
                    .addComponent(tituloVolverHistorial)
                    .addComponent(botonVolverHistorial))
                .addContainerGap(26, Short.MAX_VALUE))
        );
        contenedorHistorialLayout.setVerticalGroup(
            contenedorHistorialLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contenedorHistorialLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPanelHistorial, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(contenedorHistorialLayout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addComponent(botonVolverHistorial)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tituloVolverHistorial)
                .addGap(18, 18, 18)
                .addComponent(botonRemoverElementoHistorial)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tituloRemoverElementoHistorial)
                .addGap(18, 18, 18)
                .addComponent(botonVaciarHistorial)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tituloVaciarHistorial)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        contenedorDeSecciones.add(contenedorHistorial, "cardHistorial");

        contenedorGuardados.setBackground(new java.awt.Color(240, 238, 234));
        contenedorGuardados.setPreferredSize(new java.awt.Dimension(907, 308));

        tablaGuardados.setBackground(new java.awt.Color(240, 238, 234));
        tablaGuardados.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(82, 81, 81)));
        tablaGuardados.setFont(new java.awt.Font("Titillium Web", 0, 12)); // NOI18N
        tablaGuardados.setForeground(new java.awt.Color(82, 81, 81));
        tablaGuardados.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tablaGuardados.setGridColor(new java.awt.Color(82, 81, 81));
        tablaGuardados.setSelectionBackground(new java.awt.Color(244, 107, 82));
        tablaGuardados.setSelectionForeground(new java.awt.Color(82, 81, 81));
        scrollPanelGuardados.setViewportView(tablaGuardados);

        botonRemoverGuardado.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/boton_eliminar.png"))); // NOI18N
        botonRemoverGuardado.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonRemoverGuardado.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonRemoverGuardadoMouseClicked(evt);
            }
        });

        tituloRemoverGuardado.setFont(new java.awt.Font("Titillium Web", 0, 12)); // NOI18N
        tituloRemoverGuardado.setForeground(new java.awt.Color(82, 81, 81));
        tituloRemoverGuardado.setText("Quitar");
        tituloRemoverGuardado.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tituloRemoverGuardado.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tituloRemoverGuardadoFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tituloRemoverGuardadoFocusLost(evt);
            }
        });
        tituloRemoverGuardado.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tituloRemoverGuardadoMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tituloRemoverGuardadoMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                tituloRemoverGuardadoMouseExited(evt);
            }
        });

        tituloVaciarGuardados.setFont(new java.awt.Font("Titillium Web", 0, 12)); // NOI18N
        tituloVaciarGuardados.setForeground(new java.awt.Color(82, 81, 81));
        tituloVaciarGuardados.setText("Vaciar");
        tituloVaciarGuardados.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tituloVaciarGuardados.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tituloVaciarGuardadosFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tituloVaciarGuardadosFocusLost(evt);
            }
        });
        tituloVaciarGuardados.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tituloVaciarGuardadosMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tituloVaciarGuardadosMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                tituloVaciarGuardadosMouseExited(evt);
            }
        });

        botonVaciarGuardados.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/boton_vaciar.png"))); // NOI18N
        botonVaciarGuardados.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonVaciarGuardados.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonVaciarGuardadosMouseClicked(evt);
            }
        });

        tituloVolverGuardados.setFont(new java.awt.Font("Titillium Web", 0, 12)); // NOI18N
        tituloVolverGuardados.setForeground(new java.awt.Color(82, 81, 81));
        tituloVolverGuardados.setText("Volver");
        tituloVolverGuardados.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tituloVolverGuardados.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tituloVolverGuardadosFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tituloVolverGuardadosFocusLost(evt);
            }
        });
        tituloVolverGuardados.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tituloVolverGuardadosMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tituloVolverGuardadosMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                tituloVolverGuardadosMouseExited(evt);
            }
        });

        botonVolverGuardados.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/boton_volver.png"))); // NOI18N
        botonVolverGuardados.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonVolverGuardados.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonVolverGuardadosMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout contenedorGuardadosLayout = new javax.swing.GroupLayout(contenedorGuardados);
        contenedorGuardados.setLayout(contenedorGuardadosLayout);
        contenedorGuardadosLayout.setHorizontalGroup(
            contenedorGuardadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contenedorGuardadosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPanelGuardados, javax.swing.GroupLayout.PREFERRED_SIZE, 825, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(contenedorGuardadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tituloVaciarGuardados)
                    .addComponent(botonRemoverGuardado)
                    .addComponent(tituloRemoverGuardado)
                    .addComponent(botonVaciarGuardados)
                    .addComponent(botonVolverGuardados)
                    .addComponent(tituloVolverGuardados))
                .addContainerGap())
        );
        contenedorGuardadosLayout.setVerticalGroup(
            contenedorGuardadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contenedorGuardadosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPanelGuardados, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contenedorGuardadosLayout.createSequentialGroup()
                .addContainerGap(13, Short.MAX_VALUE)
                .addComponent(botonVolverGuardados)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tituloVolverGuardados)
                .addGap(18, 18, 18)
                .addComponent(botonRemoverGuardado)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tituloRemoverGuardado)
                .addGap(18, 18, 18)
                .addComponent(botonVaciarGuardados)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tituloVaciarGuardados)
                .addGap(88, 88, 88))
        );

        contenedorDeSecciones.add(contenedorGuardados, "cardHistorial");

        contenedorSugerencias.setBackground(new java.awt.Color(240, 238, 234));
        contenedorSugerencias.setPreferredSize(new java.awt.Dimension(907, 308));

        lienzoSugerencias.setLayout(new java.awt.CardLayout());

        contenedorSugerir.setBackground(new java.awt.Color(240, 238, 234));

        tituloPalabra.setFont(new java.awt.Font("Titillium Web", 0, 14)); // NOI18N
        tituloPalabra.setForeground(new java.awt.Color(82, 81, 81));
        tituloPalabra.setText("Palabra");

        textFieldPalabra.setFont(new java.awt.Font("Titillium Web", 0, 14)); // NOI18N
        textFieldPalabra.setForeground(new java.awt.Color(82, 81, 81));
        textFieldPalabra.setToolTipText("");
        textFieldPalabra.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(82, 81, 81)));
        textFieldPalabra.setCaretColor(new java.awt.Color(244, 107, 82));
        textFieldPalabra.setEnabled(false);
        textFieldPalabra.setFocusable(false);
        textFieldPalabra.setMargin(new java.awt.Insets(10, 10, 10, 10));
        textFieldPalabra.setName(""); // NOI18N
        textFieldPalabra.setSelectionColor(new java.awt.Color(244, 107, 82));

        tituloTraduccion.setFont(new java.awt.Font("Titillium Web", 0, 14)); // NOI18N
        tituloTraduccion.setForeground(new java.awt.Color(82, 81, 81));
        tituloTraduccion.setText("Traducción:");

        textFieldTraduccion.setFont(new java.awt.Font("Titillium Web", 0, 14)); // NOI18N
        textFieldTraduccion.setForeground(new java.awt.Color(82, 81, 81));
        textFieldTraduccion.setToolTipText("");
        textFieldTraduccion.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(82, 81, 81)));
        textFieldTraduccion.setCaretColor(new java.awt.Color(244, 107, 82));
        textFieldTraduccion.setEnabled(false);
        textFieldTraduccion.setFocusable(false);
        textFieldTraduccion.setMargin(new java.awt.Insets(10, 10, 10, 10));
        textFieldTraduccion.setName(""); // NOI18N
        textFieldTraduccion.setSelectionColor(new java.awt.Color(244, 107, 82));

        tituloIdiomaInicial.setFont(new java.awt.Font("Titillium Web", 0, 14)); // NOI18N
        tituloIdiomaInicial.setForeground(new java.awt.Color(82, 81, 81));
        tituloIdiomaInicial.setText("Idioma Inicial:");

        tituloIdiomaFinal.setFont(new java.awt.Font("Titillium Web", 0, 14)); // NOI18N
        tituloIdiomaFinal.setForeground(new java.awt.Color(82, 81, 81));
        tituloIdiomaFinal.setText("Idioma Final:");

        botonEnviarSugerencia.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/boton_listo.png"))); // NOI18N
        botonEnviarSugerencia.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonEnviarSugerencia.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonEnviarSugerenciaMouseClicked(evt);
            }
        });

        tituloEnviarSugerencia.setBackground(new java.awt.Color(82, 81, 81));
        tituloEnviarSugerencia.setFont(new java.awt.Font("Titillium Web", 0, 12)); // NOI18N
        tituloEnviarSugerencia.setForeground(new java.awt.Color(82, 81, 81));
        tituloEnviarSugerencia.setText("Enviar");
        tituloEnviarSugerencia.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tituloEnviarSugerencia.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tituloEnviarSugerenciaMouseClicked(evt);
            }
        });

        idiomasFinal.setBackground(new java.awt.Color(244, 107, 82));
        idiomasFinal.setFont(new java.awt.Font("Titillium Web", 0, 14)); // NOI18N
        idiomasFinal.setForeground(new java.awt.Color(240, 238, 234));
        idiomasFinal.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Idioma", "Español", "Ingles", "Italiano" }));
        idiomasFinal.setBorder(null);
        idiomasFinal.setEnabled(false);
        idiomasFinal.setFocusable(false);
        idiomasFinal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                idiomasFinalActionPerformed(evt);
            }
        });

        idiomasInicial.setBackground(new java.awt.Color(244, 107, 82));
        idiomasInicial.setFont(new java.awt.Font("Titillium Web", 0, 14)); // NOI18N
        idiomasInicial.setForeground(new java.awt.Color(240, 238, 234));
        idiomasInicial.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Idioma", "Español", "Ingles", "Italiano" }));
        idiomasInicial.setBorder(null);
        idiomasInicial.setEnabled(false);
        idiomasInicial.setFocusable(false);
        idiomasInicial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                idiomasInicialActionPerformed(evt);
            }
        });

        iconoIdiomaFinal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/global.png"))); // NOI18N

        iconoIdiomaInicial.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/global.png"))); // NOI18N

        tituloSugerencia.setFont(new java.awt.Font("Titillium Web", 0, 14)); // NOI18N
        tituloSugerencia.setForeground(new java.awt.Color(82, 81, 81));
        tituloSugerencia.setText("Sugerencia");

        textFieldSugerencia.setFont(new java.awt.Font("Titillium Web", 0, 14)); // NOI18N
        textFieldSugerencia.setForeground(new java.awt.Color(82, 81, 81));
        textFieldSugerencia.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(82, 81, 81)));
        textFieldSugerencia.setCaretColor(new java.awt.Color(244, 107, 82));
        textFieldSugerencia.setMargin(new java.awt.Insets(10, 10, 10, 10));
        textFieldSugerencia.setName(""); // NOI18N
        textFieldSugerencia.setSelectionColor(new java.awt.Color(244, 107, 82));

        javax.swing.GroupLayout contenedorSugerirLayout = new javax.swing.GroupLayout(contenedorSugerir);
        contenedorSugerir.setLayout(contenedorSugerirLayout);
        contenedorSugerirLayout.setHorizontalGroup(
            contenedorSugerirLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contenedorSugerirLayout.createSequentialGroup()
                .addGap(158, 158, 158)
                .addGroup(contenedorSugerirLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(tituloSugerencia)
                    .addComponent(tituloPalabra)
                    .addComponent(tituloTraduccion))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(contenedorSugerirLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(textFieldSugerencia, javax.swing.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
                    .addComponent(textFieldPalabra, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(textFieldTraduccion, javax.swing.GroupLayout.Alignment.LEADING))
                .addGap(18, 18, 18)
                .addGroup(contenedorSugerirLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(contenedorSugerirLayout.createSequentialGroup()
                        .addGroup(contenedorSugerirLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(tituloIdiomaInicial)
                            .addComponent(tituloIdiomaFinal))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(contenedorSugerirLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(idiomasInicial, 0, 190, Short.MAX_VALUE)
                            .addComponent(idiomasFinal, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(contenedorSugerirLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(iconoIdiomaInicial)
                            .addComponent(iconoIdiomaFinal)))
                    .addGroup(contenedorSugerirLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(tituloEnviarSugerencia)
                        .addComponent(botonEnviarSugerencia)))
                .addContainerGap(124, Short.MAX_VALUE))
        );
        contenedorSugerirLayout.setVerticalGroup(
            contenedorSugerirLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contenedorSugerirLayout.createSequentialGroup()
                .addContainerGap(50, Short.MAX_VALUE)
                .addGroup(contenedorSugerirLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(contenedorSugerirLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(tituloIdiomaInicial)
                        .addComponent(tituloPalabra)
                        .addComponent(textFieldPalabra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(idiomasInicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(iconoIdiomaInicial))
                .addGap(46, 46, 46)
                .addGroup(contenedorSugerirLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(contenedorSugerirLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(tituloIdiomaFinal)
                        .addComponent(tituloTraduccion)
                        .addComponent(textFieldTraduccion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(idiomasFinal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(iconoIdiomaFinal))
                .addGroup(contenedorSugerirLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(contenedorSugerirLayout.createSequentialGroup()
                        .addGap(46, 46, 46)
                        .addGroup(contenedorSugerirLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tituloSugerencia)
                            .addComponent(textFieldSugerencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(contenedorSugerirLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(botonEnviarSugerencia)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tituloEnviarSugerencia)))
                .addGap(69, 69, 69))
        );

        lienzoSugerencias.add(contenedorSugerir, "card2");

        contenedorListasSugerencias.setBackground(new java.awt.Color(240, 238, 234));

        tablaSugerencias.setBackground(new java.awt.Color(240, 238, 234));
        tablaSugerencias.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(82, 81, 81)));
        tablaSugerencias.setFont(new java.awt.Font("Titillium Web", 0, 12)); // NOI18N
        tablaSugerencias.setForeground(new java.awt.Color(82, 81, 81));
        tablaSugerencias.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tablaSugerencias.setToolTipText("Sugerencias");
        tablaSugerencias.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tablaSugerencias.setDragEnabled(true);
        tablaSugerencias.setGridColor(new java.awt.Color(244, 107, 82));
        tablaSugerencias.setSelectionBackground(new java.awt.Color(244, 107, 82));
        tablaSugerencias.setSelectionForeground(new java.awt.Color(82, 81, 81));
        jScrollPane1.setViewportView(tablaSugerencias);

        botonVaciarSugerencias.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/boton_vaciar.png"))); // NOI18N
        botonVaciarSugerencias.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonVaciarSugerencias.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonVaciarSugerenciasMouseClicked(evt);
            }
        });

        tituloVaciarSugerencias.setFont(new java.awt.Font("Titillium Web", 0, 12)); // NOI18N
        tituloVaciarSugerencias.setForeground(new java.awt.Color(82, 81, 81));
        tituloVaciarSugerencias.setText("Vaciar");
        tituloVaciarSugerencias.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tituloVaciarSugerencias.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tituloVaciarSugerenciasFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tituloVaciarSugerenciasFocusLost(evt);
            }
        });
        tituloVaciarSugerencias.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tituloVaciarSugerenciasMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tituloVaciarSugerenciasMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                tituloVaciarSugerenciasMouseExited(evt);
            }
        });

        botonRemoverSugerencia.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/boton_eliminar.png"))); // NOI18N
        botonRemoverSugerencia.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonRemoverSugerencia.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonRemoverSugerenciaMouseClicked(evt);
            }
        });

        tituloRemoverSugerencia.setFont(new java.awt.Font("Titillium Web", 0, 12)); // NOI18N
        tituloRemoverSugerencia.setForeground(new java.awt.Color(82, 81, 81));
        tituloRemoverSugerencia.setText("Quitar");
        tituloRemoverSugerencia.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tituloRemoverSugerencia.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tituloRemoverSugerenciaFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tituloRemoverSugerenciaFocusLost(evt);
            }
        });
        tituloRemoverSugerencia.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tituloRemoverSugerenciaMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tituloRemoverSugerenciaMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                tituloRemoverSugerenciaMouseExited(evt);
            }
        });

        botonAprobarSugerencia.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/boton_aprobar.png"))); // NOI18N
        botonAprobarSugerencia.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonAprobarSugerencia.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonAprobarSugerenciaMouseClicked(evt);
            }
        });

        tituloAprobarSugerencia.setFont(new java.awt.Font("Titillium Web", 0, 12)); // NOI18N
        tituloAprobarSugerencia.setForeground(new java.awt.Color(82, 81, 81));
        tituloAprobarSugerencia.setText("Aprobar");
        tituloAprobarSugerencia.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tituloAprobarSugerencia.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tituloAprobarSugerenciaFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tituloAprobarSugerenciaFocusLost(evt);
            }
        });
        tituloAprobarSugerencia.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tituloAprobarSugerenciaMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tituloAprobarSugerenciaMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                tituloAprobarSugerenciaMouseExited(evt);
            }
        });

        botonRechazarSugerencia.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/boton_rechazar.png"))); // NOI18N
        botonRechazarSugerencia.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        botonRechazarSugerencia.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botonRechazarSugerenciaMouseClicked(evt);
            }
        });

        tituloRechazarSugerencia.setFont(new java.awt.Font("Titillium Web", 0, 12)); // NOI18N
        tituloRechazarSugerencia.setForeground(new java.awt.Color(82, 81, 81));
        tituloRechazarSugerencia.setText("Rechazar");
        tituloRechazarSugerencia.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tituloRechazarSugerencia.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tituloRechazarSugerenciaFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tituloRechazarSugerenciaFocusLost(evt);
            }
        });
        tituloRechazarSugerencia.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tituloRechazarSugerenciaMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tituloRechazarSugerenciaMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                tituloRechazarSugerenciaMouseExited(evt);
            }
        });

        javax.swing.GroupLayout contenedorListasSugerenciasLayout = new javax.swing.GroupLayout(contenedorListasSugerencias);
        contenedorListasSugerencias.setLayout(contenedorListasSugerenciasLayout);
        contenedorListasSugerenciasLayout.setHorizontalGroup(
            contenedorListasSugerenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contenedorListasSugerenciasLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 816, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(contenedorListasSugerenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(contenedorListasSugerenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(contenedorListasSugerenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contenedorListasSugerenciasLayout.createSequentialGroup()
                                .addGroup(contenedorListasSugerenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tituloRemoverSugerencia)
                                    .addComponent(botonRemoverSugerencia))
                                .addContainerGap())
                            .addGroup(contenedorListasSugerenciasLayout.createSequentialGroup()
                                .addGroup(contenedorListasSugerenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tituloRechazarSugerencia)
                                    .addGroup(contenedorListasSugerenciasLayout.createSequentialGroup()
                                        .addGap(7, 7, 7)
                                        .addGroup(contenedorListasSugerenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(botonRechazarSugerencia)
                                            .addComponent(botonAprobarSugerencia))))
                                .addGap(1, 1, 1)))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contenedorListasSugerenciasLayout.createSequentialGroup()
                            .addGroup(contenedorListasSugerenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(tituloVaciarSugerencias)
                                .addComponent(botonVaciarSugerencias))
                            .addContainerGap()))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contenedorListasSugerenciasLayout.createSequentialGroup()
                        .addComponent(tituloAprobarSugerencia)
                        .addContainerGap())))
        );
        contenedorListasSugerenciasLayout.setVerticalGroup(
            contenedorListasSugerenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contenedorListasSugerenciasLayout.createSequentialGroup()
                .addContainerGap(8, Short.MAX_VALUE)
                .addComponent(botonAprobarSugerencia)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tituloAprobarSugerencia)
                .addGap(18, 18, 18)
                .addComponent(botonRechazarSugerencia)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tituloRechazarSugerencia)
                .addGap(18, 18, 18)
                .addComponent(botonRemoverSugerencia)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tituloRemoverSugerencia)
                .addGap(18, 18, 18)
                .addComponent(botonVaciarSugerencias)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tituloVaciarSugerencias)
                .addContainerGap())
        );

        lienzoSugerencias.add(contenedorListasSugerencias, "card2");

        javax.swing.GroupLayout contenedorSugerenciasLayout = new javax.swing.GroupLayout(contenedorSugerencias);
        contenedorSugerencias.setLayout(contenedorSugerenciasLayout);
        contenedorSugerenciasLayout.setHorizontalGroup(
            contenedorSugerenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contenedorSugerenciasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lienzoSugerencias, javax.swing.GroupLayout.PREFERRED_SIZE, 880, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );
        contenedorSugerenciasLayout.setVerticalGroup(
            contenedorSugerenciasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contenedorSugerenciasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lienzoSugerencias, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        contenedorDeSecciones.add(contenedorSugerencias, "cardHistorial");

        contenedorFooter.setBackground(new java.awt.Color(240, 238, 234));
        contenedorFooter.setPreferredSize(new java.awt.Dimension(907, 25));

        copyright.setBackground(new java.awt.Color(82, 81, 81));
        copyright.setFont(new java.awt.Font("DM Sans", 0, 10)); // NOI18N
        copyright.setForeground(new java.awt.Color(82, 81, 81));
        copyright.setText("© 2024 Omarhvs, LLC");
        copyright.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                copyrightMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout contenedorFooterLayout = new javax.swing.GroupLayout(contenedorFooter);
        contenedorFooter.setLayout(contenedorFooterLayout);
        contenedorFooterLayout.setHorizontalGroup(
            contenedorFooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contenedorFooterLayout.createSequentialGroup()
                .addContainerGap(402, Short.MAX_VALUE)
                .addComponent(copyright)
                .addGap(402, 402, 402))
        );
        contenedorFooterLayout.setVerticalGroup(
            contenedorFooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contenedorFooterLayout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addComponent(copyright))
        );

        javax.swing.GroupLayout lienzoLayout = new javax.swing.GroupLayout(lienzo);
        lienzo.setLayout(lienzoLayout);
        lienzoLayout.setHorizontalGroup(
            lienzoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(contenedorHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(contenedorFooter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(contenedorDeSecciones, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        lienzoLayout.setVerticalGroup(
            lienzoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lienzoLayout.createSequentialGroup()
                .addComponent(contenedorHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(contenedorDeSecciones, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(contenedorFooter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lienzo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lienzo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botonCopiarOutputMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonCopiarOutputMouseClicked
        copiarTexto(textAreaOutput, "Texto Copiado");
    }//GEN-LAST:event_botonCopiarOutputMouseClicked

    private void botonBorrarOutputMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonBorrarOutputMouseClicked
        borrarTexto(textAreaOutput, "Output Limpiado");
    }//GEN-LAST:event_botonBorrarOutputMouseClicked

    private void botonBorrarInputMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonBorrarInputMouseClicked
        borrarTexto(textAreaInput, "Input Limpiado");
    }//GEN-LAST:event_botonBorrarInputMouseClicked

    private void botonGuardarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonGuardarMouseClicked
        String TEXTO_INPUT = textAreaInput.getText();
        String TEXTO_OUTPUT = textAreaOutput.getText();
        String IDIOMA_INPUT = (String) idiomasInput.getSelectedItem();
        String IDIOMA_OUTPUT = (String) idiomasOutput.getSelectedItem();
        
        if(IDIOMA_INPUT.contains("ñ"))
        {
            IDIOMA_INPUT = "Espanol";
        }
        else if(IDIOMA_OUTPUT.contains("ñ"))
        {
            IDIOMA_OUTPUT = "Espanol";
        }
        
        if(!textAreaInput.getText().isEmpty() && !textAreaOutput.getText().isEmpty())
        {
            if (verificarTraduccionExistente(TEXTO_INPUT, IDIOMA_INPUT)) 
            {
                colaDeMensajes.offer("Ya guardada");
                notificarAccionBasica(colaDeMensajes, mensajeAccion);
                DesktopNotify.showDesktopMessage("Error", "Ya se  ha guardado esta traducción (Idioma Origen y Destino).", DesktopNotify.ERROR, TIEMPO_NOTIFICACION);
                return;
            }
            insertarEnGuardados(TEXTO_INPUT, TEXTO_OUTPUT, IDIOMA_INPUT, IDIOMA_OUTPUT);
            actualizarTraduccionesGuardadas();
        }
        else
        {
            colaDeMensajes.offer("Nada para Guardar");
            notificarAccionBasica(colaDeMensajes, mensajeAccion);
            DesktopNotify.showDesktopMessage("Error", "No hay traducciones completos a guardar.", DesktopNotify.ERROR, TIEMPO_NOTIFICACION);
        }
    }//GEN-LAST:event_botonGuardarMouseClicked
        
    private void botonCopiarInputMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonCopiarInputMouseClicked
        copiarTexto(textAreaOutput, "Texto Copiado");
    }//GEN-LAST:event_botonCopiarInputMouseClicked

    private void idiomasInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_idiomasInputActionPerformed
        cambiarIconoBandera(idiomasInput, iconoIdiomaInput);
    }//GEN-LAST:event_idiomasInputActionPerformed

    private void idiomasOutputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_idiomasOutputActionPerformed
        cambiarIconoBandera(idiomasOutput, iconoIdiomaOutput);
    }//GEN-LAST:event_idiomasOutputActionPerformed

    private void botonInvertirMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonInvertirMouseClicked
        String IDIOMA_INPUT_SELECCIONADO = (String) idiomasInput.getSelectedItem();
        String IDIOMA_OUTPUT_SELECCIONADO = (String) idiomasOutput.getSelectedItem();
        
        idiomasOutput.setSelectedItem(IDIOMA_INPUT_SELECCIONADO);
        idiomasInput.setSelectedItem(IDIOMA_OUTPUT_SELECCIONADO);
        
        String TEXTO_INPUT = textAreaInput.getText();
        String TEXTO_OUTPUT = textAreaOutput.getText();
    
        textAreaInput.setText(TEXTO_INPUT);
        textAreaOutput.setText(TEXTO_OUTPUT);
    }//GEN-LAST:event_botonInvertirMouseClicked

    private void botonTraducirMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonTraducirMouseClicked
        String TEXTO_INPUT = textAreaInput.getText().toLowerCase();
        String IDIOMA_INPUT = (String) idiomasInput.getSelectedItem();
        String IDIOMA_OUTPUT = (String) idiomasOutput.getSelectedItem();
                
        if(IDIOMA_INPUT.contains("ñ"))
        {
            IDIOMA_INPUT = "Espanol";
        }
        else if(IDIOMA_OUTPUT.contains("ñ"))
        {
            IDIOMA_OUTPUT = "Espanol";
        }
        else if(IDIOMA_INPUT.equals("Idioma"))
        {
            DesktopNotify.showDesktopMessage("Error", "Seleccione un idioma de entrada.", DesktopNotify.ERROR, TIEMPO_NOTIFICACION);
        }
        else if(IDIOMA_INPUT.equals("Idioma"))
        {
            DesktopNotify.showDesktopMessage("Error", "Seleccione un idioma de salida.", DesktopNotify.ERROR, TIEMPO_NOTIFICACION);
        }
        
        if(!TEXTO_INPUT.isEmpty())
        {
            textAreaOutput.setText("");
            String TRADUCCION = buscarTraduccion(TEXTO_INPUT, IDIOMA_INPUT, IDIOMA_OUTPUT);
            if (TRADUCCION != null)
            {
                textAreaOutput.setText(TRADUCCION);
                actualizarTraduccionesRealizadas();
            }
            else
            {
                DesktopNotify.showDesktopMessage("Error", "No se encontro traduccion para el texto ingresado.", DesktopNotify.ERROR, TIEMPO_NOTIFICACION);
            }
        }
        else
        {
            DesktopNotify.showDesktopMessage("Error", "Debe introducir al menos una palabra.", DesktopNotify.ERROR, TIEMPO_NOTIFICACION);
        }
    }//GEN-LAST:event_botonTraducirMouseClicked

    private void seccionTraducirMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_seccionTraducirMouseClicked
        CARD_LAYOUT.show(contenedorDeSecciones, "traducir");
        tituloSeccion.setText("Traducir");
    }//GEN-LAST:event_seccionTraducirMouseClicked
        
    private void botonRemoverGuardadoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonRemoverGuardadoMouseClicked
        accionBotonRemoverRegistro(tablaGuardados, TABLA_GUARDADOS_SQL, "GUARDADO");
        actualizarTraduccionesGuardadas();
    }//GEN-LAST:event_botonRemoverGuardadoMouseClicked

    private void tituloRemoverGuardadoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloRemoverGuardadoMouseClicked
        accionBotonRemoverRegistro(tablaGuardados, TABLA_GUARDADOS_SQL, "GUARDADO");
        actualizarTraduccionesGuardadas();
    }//GEN-LAST:event_tituloRemoverGuardadoMouseClicked

    private void botonVaciarGuardadosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonVaciarGuardadosMouseClicked
        accionBotonVaciarGuardados();
        actualizarTraduccionesGuardadas();
    }//GEN-LAST:event_botonVaciarGuardadosMouseClicked

    private void tituloVaciarGuardadosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloVaciarGuardadosMouseClicked
        accionBotonVaciarGuardados();
        actualizarTraduccionesGuardadas();
    }//GEN-LAST:event_tituloVaciarGuardadosMouseClicked

    private void botonRemoverElementoHistorialMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonRemoverElementoHistorialMouseClicked
        accionBotonRemoverRegistro(tablaHistorial, TABLA_HISTORIAL_SQL, "TRADUCCION");
        actualizarTraduccionesRealizadas();
    }//GEN-LAST:event_botonRemoverElementoHistorialMouseClicked

    private void tituloRemoverElementoHistorialMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloRemoverElementoHistorialMouseClicked
        accionBotonRemoverRegistro(tablaHistorial, TABLA_HISTORIAL_SQL, "TRADUCCION");
        actualizarTraduccionesRealizadas();
    }//GEN-LAST:event_tituloRemoverElementoHistorialMouseClicked

    private void botonVaciarHistorialMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonVaciarHistorialMouseClicked
        accionBotonVaciarHistorial();
    }//GEN-LAST:event_botonVaciarHistorialMouseClicked

    private void tituloVaciarHistorialMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloVaciarHistorialMouseClicked
        accionBotonVaciarHistorial();
    }//GEN-LAST:event_tituloVaciarHistorialMouseClicked

    private void botonRemoverSugerenciaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonRemoverSugerenciaMouseClicked
        accionBotonRemoverRegistro(tablaSugerencias, "SUGERENCIAS", "SUGERENCIA");
    }//GEN-LAST:event_botonRemoverSugerenciaMouseClicked

    private void tituloRemoverSugerenciaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloRemoverSugerenciaMouseClicked
        accionBotonRemoverRegistro(tablaSugerencias, "SUGERENCIAS", "SUGERENCIA");
    }//GEN-LAST:event_tituloRemoverSugerenciaMouseClicked

    private void tituloVaciarSugerenciasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloVaciarSugerenciasMouseClicked
        accionBotonVaciarSugerencias();
    }//GEN-LAST:event_tituloVaciarSugerenciasMouseClicked

    private void botonVaciarSugerenciasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonVaciarSugerenciasMouseClicked
        accionBotonVaciarSugerencias();
    }//GEN-LAST:event_botonVaciarSugerenciasMouseClicked

    private void botonSugerirCambiosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonSugerirCambiosMouseClicked
        String TEXTO_INPUT = textAreaInput.getText().trim();
        String TEXTO_OUTPUT = textAreaOutput.getText().trim();
        
        if (!TEXTO_INPUT.isEmpty() && !TEXTO_OUTPUT.isEmpty())
        {
            textFieldPalabra.setText(TEXTO_INPUT);
            if(textAreaOutput.getText().equalsIgnoreCase("No se encontró una traducción para la palabra ingresada"))
            {
                textFieldTraduccion.setText("-");
            }
            else
            {
                textFieldTraduccion.setText(TEXTO_OUTPUT);
            }
            
            String IDIOMA_INPUT = (String) idiomasInput.getSelectedItem();
            String IDIOMA_OUTPUT = (String) idiomasOutput.getSelectedItem();

            idiomasInicial.setSelectedItem(IDIOMA_INPUT);
            idiomasFinal.setSelectedItem(IDIOMA_OUTPUT);
            
            CARD_LAYOUT.show(lienzoSugerencias, "sugerir");
            CARD_LAYOUT.show(contenedorDeSecciones, "lienzo_sugerencias");
            tituloSeccion.setText("Surgerir");
        } 
        else
        {
            DesktopNotify.showDesktopMessage("Error", "Debe ingresar al menos una palabra.", DesktopNotify.ERROR, TIEMPO_NOTIFICACION);
        }
    }//GEN-LAST:event_botonSugerirCambiosMouseClicked

    private void idiomasFinalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_idiomasFinalActionPerformed
        cambiarIconoBandera(idiomasFinal, iconoIdiomaFinal);
    }//GEN-LAST:event_idiomasFinalActionPerformed

    private void idiomasInicialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_idiomasInicialActionPerformed
        cambiarIconoBandera(idiomasInicial, iconoIdiomaInicial);
    }//GEN-LAST:event_idiomasInicialActionPerformed

    private void botonEnviarSugerenciaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonEnviarSugerenciaMouseClicked
        actionBotonEnviarSugerencia();
    }//GEN-LAST:event_botonEnviarSugerenciaMouseClicked

    private void tituloEnviarSugerenciaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloEnviarSugerenciaMouseClicked
        actionBotonEnviarSugerencia();
    }//GEN-LAST:event_tituloEnviarSugerenciaMouseClicked

    private void botonRechazarSugerenciaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonRechazarSugerenciaMouseClicked
        rechazarSugerencia();
    }//GEN-LAST:event_botonRechazarSugerenciaMouseClicked

    private void tituloRechazarSugerenciaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloRechazarSugerenciaMouseClicked
        rechazarSugerencia();
    }//GEN-LAST:event_tituloRechazarSugerenciaMouseClicked

    private void botonAprobarSugerenciaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonAprobarSugerenciaMouseClicked
        aprobarSugerencia();
    }//GEN-LAST:event_botonAprobarSugerenciaMouseClicked

    private void tituloAprobarSugerenciaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloAprobarSugerenciaMouseClicked
        aprobarSugerencia();
    }//GEN-LAST:event_tituloAprobarSugerenciaMouseClicked

    private void seccionPalabrasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_seccionPalabrasMouseClicked
        CARD_LAYOUT.show(contenedorDeSecciones, "palabras");
        tituloSeccion.setText("Palabras");
        try
        {
            cargarDatosTabla(tablaPalabras, "PALABRAS", "PALABRA");
        }
        catch (SQLException ex)
        {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_seccionPalabrasMouseClicked

    private void tituloVaciarPalabrasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloVaciarPalabrasMouseClicked
        accionBotonVaciarPalabras();
    }//GEN-LAST:event_tituloVaciarPalabrasMouseClicked

    private void botonVaciarPalabrasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonVaciarPalabrasMouseClicked
        accionBotonVaciarPalabras();
    }//GEN-LAST:event_botonVaciarPalabrasMouseClicked

    private void botonRemoverPalabraMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonRemoverPalabraMouseClicked
        accionBotonRemoverRegistro(tablaPalabras, "PALABRAS", "PALABRA");
    }//GEN-LAST:event_botonRemoverPalabraMouseClicked

    private void tituloRemoverPalabraMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloRemoverPalabraMouseClicked
        accionBotonRemoverRegistro(tablaPalabras, "PALABRAS", "PALABRA");
    }//GEN-LAST:event_tituloRemoverPalabraMouseClicked

    private void botonSugerirCambiosMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonSugerirCambiosMouseEntered
        botonSugerirCambios.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_botonSugerirCambiosMouseEntered

    private void botonSugerirCambiosMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonSugerirCambiosMouseExited
        botonSugerirCambios.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_botonSugerirCambiosMouseExited

    private void botonSugerirCambiosFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_botonSugerirCambiosFocusGained
        botonSugerirCambios.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_botonSugerirCambiosFocusGained

    private void botonSugerirCambiosFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_botonSugerirCambiosFocusLost
        botonSugerirCambios.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_botonSugerirCambiosFocusLost

    private void tituloRemoverElementoHistorialFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tituloRemoverElementoHistorialFocusGained
        tituloRemoverElementoHistorial.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_tituloRemoverElementoHistorialFocusGained

    private void tituloRemoverElementoHistorialMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloRemoverElementoHistorialMouseEntered
        tituloRemoverElementoHistorial.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_tituloRemoverElementoHistorialMouseEntered

    private void tituloRemoverElementoHistorialFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tituloRemoverElementoHistorialFocusLost
        tituloRemoverElementoHistorial.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_tituloRemoverElementoHistorialFocusLost

    private void tituloRemoverElementoHistorialMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloRemoverElementoHistorialMouseExited
        tituloRemoverElementoHistorial.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_tituloRemoverElementoHistorialMouseExited

    private void tituloVaciarHistorialFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tituloVaciarHistorialFocusGained
        tituloVaciarHistorial.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_tituloVaciarHistorialFocusGained

    private void tituloVaciarHistorialMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloVaciarHistorialMouseEntered
        tituloVaciarHistorial.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_tituloVaciarHistorialMouseEntered

    private void tituloVaciarHistorialFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tituloVaciarHistorialFocusLost
        tituloVaciarHistorial.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_tituloVaciarHistorialFocusLost

    private void tituloVaciarHistorialMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloVaciarHistorialMouseExited
        tituloVaciarHistorial.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_tituloVaciarHistorialMouseExited

    private void tituloRemoverGuardadoFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tituloRemoverGuardadoFocusGained
        tituloRemoverGuardado.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_tituloRemoverGuardadoFocusGained

    private void tituloRemoverGuardadoMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloRemoverGuardadoMouseEntered
        tituloRemoverGuardado.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_tituloRemoverGuardadoMouseEntered

    private void tituloRemoverGuardadoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tituloRemoverGuardadoFocusLost
        tituloRemoverGuardado.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_tituloRemoverGuardadoFocusLost

    private void tituloRemoverGuardadoMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloRemoverGuardadoMouseExited
        tituloRemoverGuardado.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_tituloRemoverGuardadoMouseExited

    private void tituloVaciarGuardadosFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tituloVaciarGuardadosFocusGained
        tituloVaciarGuardados.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_tituloVaciarGuardadosFocusGained

    private void tituloVaciarGuardadosMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloVaciarGuardadosMouseEntered
        tituloVaciarGuardados.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_tituloVaciarGuardadosMouseEntered

    private void tituloVaciarGuardadosFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tituloVaciarGuardadosFocusLost
        tituloVaciarGuardados.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_tituloVaciarGuardadosFocusLost

    private void tituloVaciarGuardadosMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloVaciarGuardadosMouseExited
        tituloVaciarGuardados.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_tituloVaciarGuardadosMouseExited

    private void tituloRemoverPalabraFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tituloRemoverPalabraFocusGained
        tituloRemoverPalabra.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_tituloRemoverPalabraFocusGained

    private void tituloRemoverPalabraMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloRemoverPalabraMouseEntered
        tituloRemoverPalabra.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_tituloRemoverPalabraMouseEntered

    private void tituloRemoverPalabraMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloRemoverPalabraMouseExited
        tituloRemoverPalabra.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_tituloRemoverPalabraMouseExited

    private void tituloRemoverPalabraFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tituloRemoverPalabraFocusLost
        tituloRemoverPalabra.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_tituloRemoverPalabraFocusLost

    private void tituloVaciarPalabrasFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tituloVaciarPalabrasFocusGained
        tituloVaciarPalabras.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_tituloVaciarPalabrasFocusGained

    private void tituloVaciarPalabrasMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloVaciarPalabrasMouseEntered
        tituloVaciarPalabras.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_tituloVaciarPalabrasMouseEntered

    private void tituloVaciarPalabrasFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tituloVaciarPalabrasFocusLost
        tituloVaciarPalabras.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_tituloVaciarPalabrasFocusLost

    private void tituloVaciarPalabrasMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloVaciarPalabrasMouseExited
        tituloVaciarPalabras.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_tituloVaciarPalabrasMouseExited

    private void tituloAprobarSugerenciaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tituloAprobarSugerenciaFocusGained
        tituloAprobarSugerencia.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_tituloAprobarSugerenciaFocusGained

    private void tituloAprobarSugerenciaMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloAprobarSugerenciaMouseEntered
        tituloAprobarSugerencia.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_tituloAprobarSugerenciaMouseEntered

    private void tituloAprobarSugerenciaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tituloAprobarSugerenciaFocusLost
        tituloAprobarSugerencia.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_tituloAprobarSugerenciaFocusLost

    private void tituloAprobarSugerenciaMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloAprobarSugerenciaMouseExited
        tituloAprobarSugerencia.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_tituloAprobarSugerenciaMouseExited

    private void tituloRechazarSugerenciaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tituloRechazarSugerenciaFocusGained
        tituloRechazarSugerencia.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_tituloRechazarSugerenciaFocusGained

    private void tituloRechazarSugerenciaMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloRechazarSugerenciaMouseEntered
        tituloRechazarSugerencia.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_tituloRechazarSugerenciaMouseEntered

    private void tituloRechazarSugerenciaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tituloRechazarSugerenciaFocusLost
        tituloRechazarSugerencia.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_tituloRechazarSugerenciaFocusLost

    private void tituloRechazarSugerenciaMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloRechazarSugerenciaMouseExited
        tituloRechazarSugerencia.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_tituloRechazarSugerenciaMouseExited

    private void tituloRemoverSugerenciaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tituloRemoverSugerenciaFocusGained
        tituloRemoverSugerencia.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_tituloRemoverSugerenciaFocusGained

    private void tituloRemoverSugerenciaMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloRemoverSugerenciaMouseEntered
        tituloRemoverSugerencia.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_tituloRemoverSugerenciaMouseEntered

    private void tituloRemoverSugerenciaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tituloRemoverSugerenciaFocusLost
        tituloRemoverSugerencia.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_tituloRemoverSugerenciaFocusLost

    private void tituloRemoverSugerenciaMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloRemoverSugerenciaMouseExited
        tituloRemoverSugerencia.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_tituloRemoverSugerenciaMouseExited

    private void tituloVaciarSugerenciasFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tituloVaciarSugerenciasFocusGained
        tituloVaciarSugerencias.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_tituloVaciarSugerenciasFocusGained

    private void tituloVaciarSugerenciasMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloVaciarSugerenciasMouseEntered
        tituloVaciarSugerencias.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_tituloVaciarSugerenciasMouseEntered

    private void tituloVaciarSugerenciasFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tituloVaciarSugerenciasFocusLost
        tituloVaciarSugerencias.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_tituloVaciarSugerenciasFocusLost

    private void tituloVaciarSugerenciasMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloVaciarSugerenciasMouseExited
        tituloVaciarSugerencias.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_tituloVaciarSugerenciasMouseExited

    private void etiquetaSinCuentaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_etiquetaSinCuentaKeyPressed
        
    }//GEN-LAST:event_etiquetaSinCuentaKeyPressed

    private void etiquetaSinCuentaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_etiquetaSinCuentaMouseClicked
        CARD_LAYOUT.show(contenedorPrincipal, "signup");
    }//GEN-LAST:event_etiquetaSinCuentaMouseClicked

    private void etiquetaConCuentaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_etiquetaConCuentaMouseClicked
        CARD_LAYOUT.show(contenedorPrincipal, "login");
    }//GEN-LAST:event_etiquetaConCuentaMouseClicked

    private void botonLoginMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonLoginMouseClicked
        iniciarSesion();
    }//GEN-LAST:event_botonLoginMouseClicked

    private void botonRegistroMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonRegistroMouseClicked
        crearNuevaCuenta();
    }//GEN-LAST:event_botonRegistroMouseClicked

    private void seccionCuentaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_seccionCuentaMouseClicked
        tituloSeccion.setText("Cuenta");
        CARD_LAYOUT.show(contenedorDeSecciones, "cuenta");
        
        try
        {
            cargarApartadoCuenta();
        }
        catch (SQLException ex)
        {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_seccionCuentaMouseClicked

    private void botonEditarDatosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonEditarDatosMouseClicked
        edicionHabilitadaDeDatos();
    }//GEN-LAST:event_botonEditarDatosMouseClicked

    private void tituloEditarDatosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloEditarDatosMouseClicked
        edicionHabilitadaDeDatos();
    }//GEN-LAST:event_tituloEditarDatosMouseClicked

    private void botonCancelarCambiosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonCancelarCambiosMouseClicked
        cargarDatosApartadoCuenta();
        edicionDesbilitadaDeDatos();
    }//GEN-LAST:event_botonCancelarCambiosMouseClicked

    private void tituloCancelarCambiosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloCancelarCambiosMouseClicked
        cargarDatosApartadoCuenta();
        edicionDesbilitadaDeDatos();
    }//GEN-LAST:event_tituloCancelarCambiosMouseClicked

    private void botonConfirmarCambiosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonConfirmarCambiosMouseClicked
        actualizarDatosCuenta();
    }//GEN-LAST:event_botonConfirmarCambiosMouseClicked

    private void tituloConfirmarCambiosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloConfirmarCambiosMouseClicked
        actualizarDatosCuenta();
    }//GEN-LAST:event_tituloConfirmarCambiosMouseClicked

    private void textFieldNombreDatosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textFieldNombreDatosActionPerformed

    }//GEN-LAST:event_textFieldNombreDatosActionPerformed

    private void botonGuardadosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonGuardadosMouseClicked
        CARD_LAYOUT.show(contenedorDeSecciones, "guardados");
        tituloSeccion.setText("Guardados");
        try
        {
            cargarDatosTabla(tablaGuardados, TABLA_GUARDADOS_SQL, "GUARDADO");
        }
        catch (SQLException ex)
        {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_botonGuardadosMouseClicked

    private void botonBusquedasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonBusquedasMouseClicked
        CARD_LAYOUT.show(contenedorDeSecciones, "historial");
        tituloSeccion.setText("Historial");
        try
        {
            cargarDatosTabla(tablaHistorial, TABLA_HISTORIAL_SQL, "TRADUCCION");
        }
        catch (SQLException ex)
        {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_botonBusquedasMouseClicked

    private void botonSugerenciasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonSugerenciasMouseClicked
        CARD_LAYOUT.show(contenedorDeSecciones, "sugerencias_personales");
        tituloSeccion.setText("Mis Sugerencias");
        cargarSugerenciasPersonales();
    }//GEN-LAST:event_botonSugerenciasMouseClicked

    private void comboBoxLenguasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxLenguasActionPerformed
        cambiarIconoBandera(comboBoxLenguas, iconoLenguaMaterna);
    }//GEN-LAST:event_comboBoxLenguasActionPerformed

    private void tituloVolverGuardadosFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tituloVolverGuardadosFocusGained
        tituloRemoverGuardado.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_tituloVolverGuardadosFocusGained

    private void tituloVolverGuardadosFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tituloVolverGuardadosFocusLost
        tituloRemoverGuardado.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_tituloVolverGuardadosFocusLost

    private void tituloVolverGuardadosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloVolverGuardadosMouseClicked
        volverSeccionCuenta();
    }//GEN-LAST:event_tituloVolverGuardadosMouseClicked

    private void tituloVolverGuardadosMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloVolverGuardadosMouseEntered
        tituloVolverGuardados.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_tituloVolverGuardadosMouseEntered

    private void tituloVolverGuardadosMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloVolverGuardadosMouseExited
        tituloVolverGuardados.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_tituloVolverGuardadosMouseExited

    private void botonVolverGuardadosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonVolverGuardadosMouseClicked
        volverSeccionCuenta();
    }//GEN-LAST:event_botonVolverGuardadosMouseClicked

    private void tituloVolverHistorialFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tituloVolverHistorialFocusGained
        tituloVolverHistorial.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_tituloVolverHistorialFocusGained

    private void tituloVolverHistorialFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tituloVolverHistorialFocusLost
        tituloVolverHistorial.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_tituloVolverHistorialFocusLost

    private void tituloVolverHistorialMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloVolverHistorialMouseClicked
        volverSeccionCuenta();
    }//GEN-LAST:event_tituloVolverHistorialMouseClicked

    private void tituloVolverHistorialMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloVolverHistorialMouseEntered
        tituloVolverHistorial.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_tituloVolverHistorialMouseEntered

    private void tituloVolverHistorialMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloVolverHistorialMouseExited
        tituloVolverHistorial.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_tituloVolverHistorialMouseExited

    private void botonVolverHistorialMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonVolverHistorialMouseClicked
        volverSeccionCuenta();
    }//GEN-LAST:event_botonVolverHistorialMouseClicked

    private void tituloVolverSugerenciasPersonalesFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tituloVolverSugerenciasPersonalesFocusGained
        tituloVolverSugerenciasPersonales.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_tituloVolverSugerenciasPersonalesFocusGained

    private void tituloVolverSugerenciasPersonalesFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tituloVolverSugerenciasPersonalesFocusLost
        tituloVolverSugerenciasPersonales.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_tituloVolverSugerenciasPersonalesFocusLost

    private void tituloVolverSugerenciasPersonalesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloVolverSugerenciasPersonalesMouseClicked
        volverSeccionCuenta();
    }//GEN-LAST:event_tituloVolverSugerenciasPersonalesMouseClicked

    private void tituloVolverSugerenciasPersonalesMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloVolverSugerenciasPersonalesMouseEntered
        tituloVolverSugerenciasPersonales.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_tituloVolverSugerenciasPersonalesMouseEntered

    private void tituloVolverSugerenciasPersonalesMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloVolverSugerenciasPersonalesMouseExited
        tituloVolverSugerenciasPersonales.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_tituloVolverSugerenciasPersonalesMouseExited

    private void botonVolverSugerenciasPersonalesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonVolverSugerenciasPersonalesMouseClicked
        volverSeccionCuenta();
    }//GEN-LAST:event_botonVolverSugerenciasPersonalesMouseClicked

    private void seccionSugerenciasStaffMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_seccionSugerenciasStaffMouseClicked
        CARD_LAYOUT.show(lienzoSugerencias, "sugerencias");
        CARD_LAYOUT.show(contenedorDeSecciones, "lienzo_sugerencias");
        cargarDatosTablaSugerencias();
    }//GEN-LAST:event_seccionSugerenciasStaffMouseClicked

    private void etiquetaCerrarSesionMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_etiquetaCerrarSesionMouseClicked
        confirmarCierreDeSesion();
    }//GEN-LAST:event_etiquetaCerrarSesionMouseClicked

    private void etiquetaCerrarSesionFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_etiquetaCerrarSesionFocusGained
        etiquetaCerrarSesion.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_etiquetaCerrarSesionFocusGained

    private void etiquetaCerrarSesionFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_etiquetaCerrarSesionFocusLost
        etiquetaCerrarSesion.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_etiquetaCerrarSesionFocusLost

    private void etiquetaCerrarSesionMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_etiquetaCerrarSesionMouseEntered
        etiquetaCerrarSesion.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_etiquetaCerrarSesionMouseEntered

    private void etiquetaCerrarSesionMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_etiquetaCerrarSesionMouseExited
        etiquetaCerrarSesion.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_etiquetaCerrarSesionMouseExited

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cerrarVentanaPrincipal();
    }//GEN-LAST:event_formWindowClosing

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated

    }//GEN-LAST:event_formWindowActivated

    private void botonCerrarSesionMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonCerrarSesionMouseClicked
        confirmarCierreDeSesion();
    }//GEN-LAST:event_botonCerrarSesionMouseClicked

    private void botonVolverStaffPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonVolverStaffPanelMouseClicked
        volverSeccionCuenta();
    }//GEN-LAST:event_botonVolverStaffPanelMouseClicked

    private void tituloVolverStaffPanelKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tituloVolverStaffPanelKeyPressed
        volverSeccionCuenta();
    }//GEN-LAST:event_tituloVolverStaffPanelKeyPressed

    private void etiquetaStaffPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_etiquetaStaffPanelMouseClicked
        tituloSeccion.setText("Staff Panel");
        CARD_LAYOUT.show(contenedorDeSecciones, "staff_panel");
    }//GEN-LAST:event_etiquetaStaffPanelMouseClicked

    private void botonStaffPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonStaffPanelMouseClicked
        tituloSeccion.setText("Staff Panel");
        CARD_LAYOUT.show(contenedorDeSecciones, "staff_panel");
    }//GEN-LAST:event_botonStaffPanelMouseClicked

    private void etiquetaStaffPanelFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_etiquetaStaffPanelFocusGained
        etiquetaStaffPanel.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_etiquetaStaffPanelFocusGained

    private void etiquetaStaffPanelFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_etiquetaStaffPanelFocusLost
        etiquetaStaffPanel.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_etiquetaStaffPanelFocusLost

    private void etiquetaStaffPanelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_etiquetaStaffPanelMouseEntered
        etiquetaStaffPanel.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_etiquetaStaffPanelMouseEntered

    private void etiquetaStaffPanelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_etiquetaStaffPanelMouseExited
        etiquetaStaffPanel.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_etiquetaStaffPanelMouseExited

    private void botonCerrarSesionFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_botonCerrarSesionFocusGained
        etiquetaCerrarSesion.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_botonCerrarSesionFocusGained

    private void botonCerrarSesionFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_botonCerrarSesionFocusLost
        etiquetaCerrarSesion.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_botonCerrarSesionFocusLost

    private void botonCerrarSesionMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonCerrarSesionMouseEntered
        etiquetaCerrarSesion.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_botonCerrarSesionMouseEntered

    private void botonCerrarSesionMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonCerrarSesionMouseExited
        etiquetaCerrarSesion.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_botonCerrarSesionMouseExited

    private void botonStaffPanelFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_botonStaffPanelFocusGained
        etiquetaStaffPanel.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_botonStaffPanelFocusGained

    private void botonStaffPanelFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_botonStaffPanelFocusLost
        etiquetaStaffPanel.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_botonStaffPanelFocusLost

    private void botonStaffPanelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonStaffPanelMouseEntered
        etiquetaStaffPanel.setForeground(COLOR_NARANJA);
    }//GEN-LAST:event_botonStaffPanelMouseEntered

    private void botonStaffPanelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonStaffPanelMouseExited
        etiquetaStaffPanel.setForeground(COLOR_GRIS);
    }//GEN-LAST:event_botonStaffPanelMouseExited

    private void copyrightMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_copyrightMouseClicked
        String url = "https://x.com/blasifero";
        try
        {
            Desktop.getDesktop().browse(new URI(url));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }//GEN-LAST:event_copyrightMouseClicked

    private void comboBoxUsuariosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxUsuariosActionPerformed
        String usuarioSeleccionado = (String) comboBoxUsuarios.getSelectedItem();
        if (usuarioSeleccionado != null)
        {
            llenarDatosUsuario();
        }
         else
        {
            DesktopNotify.showDesktopMessage("Info", "Sin usuario especificado.", DesktopNotify.INFORMATION, TIEMPO_NOTIFICACION);
            placeholderUsuarioStaff.setText("-");
            placeholderStatusStaff.setText("-");
            placeholderStaffDesdeStaff.setText("-");
            placeholderStaffHastaStaff.setText("-");
            placeholderSugerenciasAprobadasStaff.setText("0");
            placeholderSugerenciasRechazadasStaff.setText("0");
            placeholderSancionesEmitidasStaff.setText("0");
        }
    }//GEN-LAST:event_comboBoxUsuariosActionPerformed

    private void comboBoxRolStaffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxRolStaffActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_comboBoxRolStaffActionPerformed

    private void botonStaffMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonStaffMouseClicked
        cargarApartadoGestionarStaff();
    }//GEN-LAST:event_botonStaffMouseClicked

    private void tituloStaffMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tituloStaffMouseClicked
        cargarApartadoGestionarStaff();
    }//GEN-LAST:event_tituloStaffMouseClicked

    private void cargarApartadoGestionarStaff()
    {
        tituloSeccion.setText("Gestionar Staff");
        CARD_LAYOUT.show(contenedorDeSecciones, "staff");
        llenarComboBoxUsuarios();
        
        String usuarioSeleccionado = (String) comboBoxUsuarios.getSelectedItem();
        if (usuarioSeleccionado != null)
        {
            llenarDatosUsuario();
        }
         else
        {
            restablecerPlaceholders();
        }
    }
    
    private void volverSeccionCuenta()
    {
        tituloSeccion.setText("Cuenta");
        CARD_LAYOUT.show(contenedorDeSecciones, "cuenta");
        
        try
        {
            cargarApartadoCuenta();
        }
        catch (SQLException ex)
        {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void confirmarCierreDeSesion()
    {
        int respuesta = JOptionPane.showConfirmDialog(null, "¿Estás seguro que deseas cerrar sesión?", "Confirmar cierre de sesión", JOptionPane.YES_NO_OPTION);
        
        if (respuesta == JOptionPane.YES_OPTION)
        {
            objetoSesion.cerrarSesion();
            vaciarColasMensajes();
            
            textFieldNombreDatos.setText("");
            textFieldUsuarioDatos.setText("");
            comboBoxLenguas.setSelectedIndex(0);
                    
            cantidadGuardados.setText("%X%");
            cantidadSugerencias.setText("%X%");
            cantidadSanciones.setText("%X%");
            cantidadBusquedas.setText("%X%");
                    
            dispose();
            Sesion.setVisible(true);
        }
    }
    
    private void vaciarColasMensajes()
    {
        colaDeMensajes.clear();
        colaDeMensajesLogin.clear();
        colaDeMensajesRegistro.clear();
    }
    
    private void protocoloInicial()
    {
        try
        {
            if(isVisible())
            {
                cargarApartadoCuenta();
            }
        }
        catch (SQLException ex)
        {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void cargarApartadoCuenta() throws SQLException
    {
        cargarDatosApartadoCuenta();
        boolean GUARDADOS_ACTUALIZADOS = actualizarTraduccionesGuardadas();
        boolean BUSQUEDAS_ACTUALIZADAS = actualizarTraduccionesRealizadas();
        boolean SUGERENCIAS_ACTUALIZADAS = actualizarTraduccionesSugeridas();
        
        if(GUARDADOS_ACTUALIZADOS && BUSQUEDAS_ACTUALIZADAS && SUGERENCIAS_ACTUALIZADAS)
        {
            DesktopNotify.showDesktopMessage("Exito", "Estadisticas actualizas correctamente.", DesktopNotify.SUCCESS, TIEMPO_NOTIFICACION);
        }
        else
        {
            DesktopNotify.showDesktopMessage("Fallo", "No se pudieron actualizar las estadisticas.", DesktopNotify.FAIL, TIEMPO_NOTIFICACION);
        }

        edicionDesbilitadaDeDatos();
    }
    
    private void edicionDesbilitadaDeDatos()
    {
        textFieldNombreDatos.setEnabled(false);
        textFieldUsuarioDatos.setEnabled(false);
        comboBoxLenguas.setEnabled(false);
        
        botonEditarDatos.setVisible(true);
        tituloEditarDatos.setVisible(true);
        botonCancelarCambios.setVisible(false);
        tituloCancelarCambios.setVisible(false);
        botonConfirmarCambios.setVisible(false);
        tituloConfirmarCambios.setVisible(false);
    }
    
    private void edicionHabilitadaDeDatos()
    {
        textFieldNombreDatos.requestFocus();
        
        textFieldNombreDatos.setEnabled(true);
        textFieldNombreDatos.setEditable(true);
        textFieldUsuarioDatos.setEnabled(true);
        textFieldUsuarioDatos.setEditable(true);
        comboBoxLenguas.setEnabled(true);
        comboBoxLenguas.setEditable(true);
        
        botonEditarDatos.setVisible(false);
        tituloEditarDatos.setVisible(false);
        botonCancelarCambios.setVisible(true);
        tituloCancelarCambios.setVisible(true);
        botonConfirmarCambios.setVisible(true);
        tituloConfirmarCambios.setVisible(true);
    }
    
    private void cargarDatosApartadoCuenta()
    {
        String usuarioActivo = GestionSesion.obtenerUsuarioDesdeSesion();
        
        String sql = "SELECT c.nombre, c.usuario, c.traducciones_guardadas, c.sugerencias_realizadas, c.sanciones_recibidas, c.traducciones_realizadas, lengua_materna " +
             "FROM cuentas c " +
             "WHERE c.usuario = ?";
                
        try (Connection connection = new ConectorBaseDatos().getConexion())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, usuarioActivo);
            try (ResultSet resultSet = statement.executeQuery())
            {
                if (resultSet.next())
                {
                    String nombre = resultSet.getString("nombre");
                    String usuario = resultSet.getString("usuario");
                    int traduccionesGuardadas = resultSet.getInt("traducciones_guardadas");
                    int sugerenciasRealizadas = resultSet.getInt("sugerencias_realizadas");
                    int sancionesRecibidas = resultSet.getInt("sanciones_recibidas");
                    int traduccionesRealizadas = resultSet.getInt("traducciones_realizadas");
                    int lenguaMaternaID = resultSet.getInt("lengua_materna");
                    
                    String lenguaMaterna;
                    switch (lenguaMaternaID)
                    {
                        case 1 -> lenguaMaterna = "Español";
                        case 2 -> lenguaMaterna = "Ingles";
                        case 3 -> lenguaMaterna = "Italiano";
                        default -> lenguaMaterna = "Español";
                    }

                    textFieldNombreDatos.setText(nombre);
                    textFieldUsuarioDatos.setText(usuario);
                    comboBoxLenguas.setSelectedItem(lenguaMaterna);
                    
                    cantidadGuardados.setText(String.valueOf(traduccionesGuardadas));
                    cantidadSugerencias.setText(String.valueOf(sugerenciasRealizadas));
                    cantidadSanciones.setText(String.valueOf(sancionesRecibidas));
                    cantidadBusquedas.setText(String.valueOf(traduccionesRealizadas));
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            DesktopNotify.showDesktopMessage("Fallo", "No se han podido cargar los datos de la cuenta: " + e, DesktopNotify.FAIL, 5000);
        }
    }
    
    private void actualizarDatosCuenta()
    {
        int idCuentaActiva = obtenerIDUsuarioActual();
        String usuarioActivo = GestionSesion.obtenerUsuarioDesdeSesion();
        
        String nuevoNombre = textFieldNombreDatos.getText();
        String nuevoUsuario = textFieldUsuarioDatos.getText();
        String nuevaLenguaMaterna = comboBoxLenguas.getSelectedItem().toString();
        
        int nuevoIDLenguaMaterna;
        switch (nuevaLenguaMaterna)
        {
            case "Español":
                nuevoIDLenguaMaterna = 1;
                break;
                
            case "Ingles":
                nuevoIDLenguaMaterna = 2;
                break;
                
            case "Italiano":
                nuevoIDLenguaMaterna = 3;
                break;
                
            default:
                nuevoIDLenguaMaterna = 1;
                break;
        }
        
        System.out.println(nuevoNombre + ", " + nuevoUsuario + ", " + nuevoIDLenguaMaterna + ", " + idCuentaActiva);
        
        boolean usuarioOcupado = existeCuenta(nuevoUsuario);
        
        if(usuarioOcupado && nuevoNombre.equals(usuarioActivo))
        {
            String sqlActualizar = "UPDATE cuentas SET nombre = ?, usuario = ?, lengua_materna = ? WHERE idcuenta = ?";
            try (Connection connection = new ConectorBaseDatos().getConexion(); PreparedStatement statement = connection.prepareStatement(sqlActualizar))
            {
                statement.setString(1, nuevoNombre);
                statement.setString(2, nuevoUsuario);
                statement.setInt(3, nuevoIDLenguaMaterna);
                statement.setInt(4, idCuentaActiva);

                int filasAfectadas = statement.executeUpdate();
                if (filasAfectadas > 0)
                {
                    DesktopNotify.showDesktopMessage("Éxito", "Datos de cuenta actualizados correctamente.", DesktopNotify.SUCCESS, 5000);
                    edicionDesbilitadaDeDatos();
                }
                else
                {
                    DesktopNotify.showDesktopMessage("Error", "No se pudo actualizar los datos de la cuenta.", DesktopNotify.FAIL, 5000);
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        else if(usuarioOcupado && !nuevoNombre.equals(usuarioActivo))
        {
            String sqlActualizar = "UPDATE cuentas SET nombre = ?, usuario = ?, lengua_materna = ? WHERE idcuenta = ?";
            try (Connection connection = new ConectorBaseDatos().getConexion(); PreparedStatement statement = connection.prepareStatement(sqlActualizar))
            {
                statement.setString(1, nuevoNombre);
                statement.setString(2, nuevoUsuario);
                statement.setInt(3, nuevoIDLenguaMaterna);
                statement.setInt(4, idCuentaActiva);

                int filasAfectadas = statement.executeUpdate();
                if (filasAfectadas > 0)
                {
                    DesktopNotify.showDesktopMessage("Éxito", "Datos de cuenta actualizados correctamente.", DesktopNotify.SUCCESS, 5000);
                    edicionDesbilitadaDeDatos();
                }
                else
                {
                    DesktopNotify.showDesktopMessage("Error", "No se pudo actualizar los datos de la cuenta.", DesktopNotify.FAIL, 5000);
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        else if(!usuarioOcupado)
        {
            String sqlActualizar = "UPDATE cuentas SET nombre = ?, usuario = ?, lengua_materna = ? WHERE idcuenta = ?";
            try (Connection connection = new ConectorBaseDatos().getConexion(); PreparedStatement statement = connection.prepareStatement(sqlActualizar))
            {
                statement.setString(1, nuevoNombre);
                statement.setString(2, nuevoUsuario);
                statement.setInt(3, nuevoIDLenguaMaterna);
                statement.setInt(4, idCuentaActiva);

                int filasAfectadas = statement.executeUpdate();
                if (filasAfectadas > 0)
                {
                    DesktopNotify.showDesktopMessage("Éxito", "Datos de cuenta actualizados correctamente.", DesktopNotify.SUCCESS, 5000);
                    edicionDesbilitadaDeDatos();
                }
                else
                {
                    DesktopNotify.showDesktopMessage("Error", "No se pudo actualizar los datos de la cuenta.", DesktopNotify.FAIL, 5000);
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            DesktopNotify.showDesktopMessage("Advertencia", "Pruebe con otro usuario.", DesktopNotify.WARNING, 5000);
        }
    }
    
    private void crearNuevaCuenta()
    {
        String nombre = textFieldNombreRegistro.getText();
        String usuario = textFieldUsuarioRegistro.getText();
        char[] contrasena = passwordFieldPasswordRegistro.getPassword();
        char[] confirmarContrasena = passwordFieldCPasswordRegistro.getPassword();

        int ultimoID = obtenerUltimoID("CUENTAS", "CUENTA");
        int nuevoID = ultimoID + 1;

        String contrasenaStr = new String(contrasena);
        String confirmarContrasenaStr = new String(confirmarContrasena);
        
        if(textFieldNombreRegistro.getText().isEmpty() && textFieldUsuarioRegistro.getText().isEmpty() && passwordFieldPasswordRegistro.getPassword().length == 0 && passwordFieldCPasswordRegistro.getPassword().length == 0)
        {
            textFieldUsuarioLogin.setText("");
            textFieldUsuarioRegistro.setText("");
            passwordFieldPasswordRegistro.setText("");
            passwordFieldCPasswordRegistro.setText("");
            colaDeMensajesLogin.offer("Datos incompletos. Verifiquelos.");
            notificarAccionBasica(colaDeMensajesLogin, etiquetaMensajeErrorRegistro);
        }
        else if(textFieldNombreRegistro.getText().isEmpty())
        {
            textFieldUsuarioLogin.setText("");
            colaDeMensajesLogin.offer("Debe ingresar su nombre.");
            notificarAccionBasica(colaDeMensajesLogin, etiquetaMensajeErrorRegistro);
        }
        else if(textFieldUsuarioRegistro.getText().isEmpty())
        {
            textFieldUsuarioRegistro.setText("");
            colaDeMensajesLogin.offer("Debe ingresar un usuario.");
            notificarAccionBasica(colaDeMensajesLogin, etiquetaMensajeErrorRegistro);
        }
        else if(passwordFieldPasswordRegistro.getPassword().length == 0)
        {
            passwordFieldPasswordRegistro.setText("");
            colaDeMensajesLogin.offer("Debe ingresar una contraseña.");
            notificarAccionBasica(colaDeMensajesLogin, etiquetaMensajeErrorRegistro);
        }
        else if(passwordFieldCPasswordRegistro.getPassword().length == 0)
        {
            passwordFieldCPasswordRegistro.setText("");
            colaDeMensajesLogin.offer("Debe confirmar su contraseña.");
            notificarAccionBasica(colaDeMensajesLogin, etiquetaMensajeErrorRegistro);
        }
        else
        {
            if (existeCuenta(usuario))
            {
                textFieldUsuarioRegistro.setText("");
                colaDeMensajesLogin.offer("Usuario ya existente.");
                notificarAccionBasica(colaDeMensajesLogin, etiquetaMensajeErrorRegistro);
                DesktopNotify.showDesktopMessage("Error", "Ya existe una cuenta con este usuario. Pruebe con otro usuario.", DesktopNotify.ERROR, 5000);
                return;
            }
            else if (!contrasenaStr.equals(confirmarContrasenaStr))
            {
                passwordFieldPasswordRegistro.setText("");
                passwordFieldCPasswordRegistro.setText("");
                colaDeMensajesLogin.offer("Las contraseñas no coinciden. Intente de nuevo.");
                notificarAccionBasica(colaDeMensajesLogin, etiquetaMensajeErrorRegistro);
                DesktopNotify.showDesktopMessage("Error", "La contraseña y la confirmacion de contraseña no coinciden.", DesktopNotify.ERROR, 5000);
                //JOptionPane.showMessageDialog(this, "La contraseña y la confirmación de contraseña no coinciden.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            else
            {
                try (Connection connection = new ConectorBaseDatos().getConexion())
                {
                    String sql = "INSERT INTO cuentas (IDCUENTA, NOMBRE, USUARIO, CONTRASENA, IDROL, TRADUCCIONES_GUARDADAS,"
                            + " SUGERENCIAS_REALIZADAS, SANCIONES_RECIBIDAS, TRADUCCIONES_REALIZADAS, LENGUA_MATERNA) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    System.out.println(sql);
                    try (PreparedStatement statement = connection.prepareStatement(sql))
                    {
                        statement.setInt(1, nuevoID);
                        statement.setString(2, nombre);
                        statement.setString(3, usuario);
                        statement.setString(4, contrasenaStr);
                        statement.setInt(5, 1);
                        statement.setInt(6, 0);
                        statement.setInt(7, 0);
                        statement.setInt(8, 0);
                        statement.setInt(9, 0);
                        statement.setInt(10, 1);
                        
                        int filasAfectadas = statement.executeUpdate();
                        if (filasAfectadas > 0)
                        {
                            USUARIO = usuario.toUpperCase();
                            TABLA_GUARDADOS_SQL = "GUARDADOS_" + USUARIO;
                            TABLA_HISTORIAL_SQL = "HISTORIAL_" + USUARIO;
                            
                            System.out.println("Usuario registrado exitosamente");
                            if (!tablaExistente(TABLA_HISTORIAL_SQL))
                            {
                                System.out.println("Creando tabla Historial");
                                crearTablaHistorial();
                            }
                            if (!tablaExistente(TABLA_GUARDADOS_SQL))
                            {
                                System.out.println("Creando tabla guardados");
                                crearTablaGuardados();
                            }
                            else
                            {
                                DesktopNotify.showDesktopMessage("Exito", "Tablas de usuario ya existentes.", DesktopNotify.SUCCESS, TIEMPO_NOTIFICACION);
                            }
                            
                            DesktopNotify.showDesktopMessage("Exito", "Registro exitoso, inicie sesion para continuar.", DesktopNotify.SUCCESS, 5000);
                            CARD_LAYOUT.show(contenedorPrincipal, "login");
                            textFieldNombreRegistro.setText("");
                            textFieldUsuarioRegistro.setText("");
                            passwordFieldPasswordRegistro.setText("");
                            passwordFieldCPasswordRegistro.setText("");
                        }
                        else
                        {
                            DesktopNotify.showDesktopMessage("Error", "Error al registrar al usuario. Intente de nuevo y si el error persiste contacte soporte.", DesktopNotify.FAIL, 5000);
                        }
                    }
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                    DesktopNotify.showDesktopMessage("Error", "Error al registrar al usuario. ", DesktopNotify.FAIL, 5000);
                }
            }
        }
    }
    
    private boolean existeCuenta(String usuario) 
    {
        boolean existe = false;
        String sql = "SELECT COUNT(*) FROM cuentas WHERE USUARIO = ?";

        try (Connection connection = new ConectorBaseDatos().getConexion(); PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setString(1, usuario);
            try (ResultSet resultSet = statement.executeQuery())
            {
                if (resultSet.next())
                {
                    int cuentaExistente = resultSet.getInt(1);
                    existe = (cuentaExistente > 0);
                    
                    if(existe)
                    {
                        DesktopNotify.showDesktopMessage("Error", "Este usuario ya existe.", DesktopNotify.FAIL, 5000);
                    }
                    else
                    {
                        DesktopNotify.showDesktopMessage("Exito", "Usuario disponible.", DesktopNotify.SUCCESS, 5000);
                    }
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            DesktopNotify.showDesktopMessage("Error", "Error al verificar la existencia de la cuenta.", DesktopNotify.FAIL, 2000);
        }
        return existe;
    }
    
    private void iniciarSesion() 
    {
        String usuario = textFieldUsuarioLogin.getText();
        String contrasena = new String(passwordFieldPasswordLogin.getPassword());
        
//        String[] credencialesGuardadas = GestionSesion.cargarCredenciales();
//        if (credencialesGuardadas[0] != null && credencialesGuardadas[1] != null)
//        {
//            usuario = credencialesGuardadas[0];
//            contrasena = credencialesGuardadas[1];
//        }
//            if (!tablaExistente(TABLA_HISTORIAL_SQL))
//        {
//            crearTablaHistorial();
//        } else if (!tablaExistente(TABLA_GUARDADOS_SQL)) {
//            crearTablaGuardados();
//        } else {
//            DesktopNotify.showDesktopMessage("Exito", "Tablas de usuario ya existentes.", DesktopNotify.SUCCESS, TIEMPO_NOTIFICACION);
//        }
    

        if(textFieldUsuarioLogin.getText().isEmpty() && passwordFieldPasswordLogin.getPassword().length == 0)
        {
            textFieldUsuarioLogin.setText("");
            passwordFieldPasswordLogin.setText("");
            colaDeMensajesLogin.offer("Debe ingresar un usuario y contraseña.");
            notificarAccionBasica(colaDeMensajesLogin, etiquetaMensajeErrorLogin);
            DesktopNotify.showDesktopMessage("Datos", "Debe ingresar un usuario y contraseña.", DesktopNotify.INPUT_REQUEST, 5000);
        }
        else if(textFieldUsuarioLogin.getText().isEmpty())
        {
            textFieldUsuarioLogin.setText("");
            colaDeMensajesLogin.offer("Debe ingresar un usuario.");
            notificarAccionBasica(colaDeMensajesLogin, etiquetaMensajeErrorLogin);
            DesktopNotify.showDesktopMessage("Datos", "Debe ingresar un usuario.", DesktopNotify.INPUT_REQUEST, 5000);
        }
        else if(passwordFieldPasswordLogin.getPassword().length == 0)
        {
            passwordFieldPasswordLogin.setText("");
            colaDeMensajesLogin.offer("Debe ingresar una contraseña.");
            notificarAccionBasica(colaDeMensajesLogin, etiquetaMensajeErrorLogin);
            DesktopNotify.showDesktopMessage("Datos", "Debe ingresar una contraseña.", DesktopNotify.INPUT_REQUEST, 5000);
        }
        else
        {
            if (verificarCredenciales(usuario, contrasena))
            {
                boolean recuerdame = checkBoxRecuerdame.isSelected();
                objetoSesion.guardarCredenciales(usuario, contrasena, recuerdame);
                
                textFieldUsuarioLogin.setText("");
                passwordFieldPasswordLogin.setText("");
                
                Sesion.setVisible(false);
                Principal ventanaTraductor = new Principal();
                ventanaTraductor.setVisible(true);
                Sesion.dispose();
                
                try
                {
                    cargarApartadoCuenta();
                }
                catch (SQLException ex)
                {
                    Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
                }

                DesktopNotify.showDesktopMessage("¡Hola!", "¡Bienvenido de nuevo, " + usuario + "!", DesktopNotify.INFORMATION, 5000);
            }
            else
            {
                colaDeMensajesLogin.offer("Datos erroneos. Intente de nuevo.");
                textFieldUsuarioLogin.setText("");
                passwordFieldPasswordLogin.setText("");
                notificarAccionBasica(colaDeMensajesLogin, etiquetaMensajeErrorLogin);
                DesktopNotify.showDesktopMessage("Datos", "Verifique sus datos", DesktopNotify.INPUT_REQUEST, 5000);
            }
        }
    }
    
    private boolean verificarCredenciales(String usuario, String contrasena) 
    {
        boolean credencialesValidas = false;
        
        try(Connection connection = new ConectorBaseDatos().getConexion())
        {           
            String sql = "SELECT COUNT(*) FROM cuentas WHERE usuario = ? AND contrasena = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) 
            {
                statement.setString(1, usuario);
                statement.setString(2, contrasena);
                try (ResultSet resultSet = statement.executeQuery()) 
                {
                    if (resultSet.next()) 
                    {
                        int count = resultSet.getInt(1);
                        credencialesValidas = count > 0;
                    }
                }
            }            
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        return credencialesValidas;
    }
    
    public static void main(String args[]) 
    {        
        java.awt.EventQueue.invokeLater(new Runnable() 
        {
            public void run() 
            {
                boolean datosVacios = GestionSesion.cargarArchivoSesion();
                
                if (!datosVacios)
                {
                    Principal ventanaPrincipal = new Principal();
                    ventanaPrincipal.setVisible(true);
                }
                else
                {
                    Principal ventanaSesion = new Principal();
                    ventanaSesion.Sesion.setVisible(true);
                }
                //Principal ventanaSesion = new Principal();
                //ventanaSesion.Sesion.setVisible(true);
                //new Principal().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFrame Sesion;
    private javax.swing.JLabel botonAprobarSugerencia;
    private javax.swing.JLabel botonBorrarInput;
    private javax.swing.JLabel botonBorrarOutput;
    private javax.swing.JLabel botonBusquedas;
    private javax.swing.JLabel botonCancelarCambios;
    private javax.swing.JLabel botonCancelarEditarRol;
    private javax.swing.JLabel botonCerrarSesion;
    private javax.swing.JLabel botonConfirmarCambios;
    private javax.swing.JLabel botonConfirmarEditarRol;
    private javax.swing.JLabel botonCopiarInput;
    private javax.swing.JLabel botonCopiarOutput;
    private javax.swing.JLabel botonEditarDatos;
    private javax.swing.JLabel botonEditarRol;
    private javax.swing.JLabel botonEnviarSugerencia;
    private javax.swing.JLabel botonGuardados;
    private javax.swing.JLabel botonGuardar;
    private javax.swing.JLabel botonInvertir;
    private javax.swing.JButton botonLogin;
    private javax.swing.JLabel botonRechazarSugerencia;
    private javax.swing.JButton botonRegistro;
    private javax.swing.JLabel botonRemoverElementoHistorial;
    private javax.swing.JLabel botonRemoverGuardado;
    private javax.swing.JLabel botonRemoverPalabra;
    private javax.swing.JLabel botonRemoverSugerencia;
    private javax.swing.JLabel botonSanciones;
    private javax.swing.JLabel botonSancionesGlobales;
    private javax.swing.JLabel botonStaff;
    private javax.swing.JLabel botonStaffPanel;
    private javax.swing.JLabel botonSugerencias;
    private javax.swing.JLabel botonSugerenciasGlobales;
    private javax.swing.JLabel botonSugerirCambios;
    private javax.swing.JLabel botonTraducir;
    private javax.swing.JLabel botonVaciarGuardados;
    private javax.swing.JLabel botonVaciarHistorial;
    private javax.swing.JLabel botonVaciarPalabras;
    private javax.swing.JLabel botonVaciarSugerencias;
    private javax.swing.JLabel botonVolverGuardados;
    private javax.swing.JLabel botonVolverHistorial;
    private javax.swing.JLabel botonVolverStaffPanel;
    private javax.swing.JLabel botonVolverSugerenciasPersonales;
    private javax.swing.JLabel cantidadBusquedas;
    private javax.swing.JLabel cantidadCaracteresActuales;
    private javax.swing.JLabel cantidadGuardados;
    private javax.swing.JLabel cantidadSanciones;
    private javax.swing.JLabel cantidadSugerencias;
    private javax.swing.JCheckBox checkBoxRecuerdame;
    private javax.swing.JComboBox<String> comboBoxLenguas;
    private javax.swing.JComboBox<String> comboBoxRolStaff;
    private javax.swing.JComboBox<String> comboBoxUsuarios;
    private javax.swing.JPanel contenedorCentral;
    private javax.swing.JPanel contenedorCuenta;
    private javax.swing.JPanel contenedorDatosGenerales;
    private javax.swing.JPanel contenedorDatosPersonales;
    private javax.swing.JPanel contenedorDeSecciones;
    private javax.swing.JPanel contenedorFooter;
    private javax.swing.JPanel contenedorFooterSesion;
    private javax.swing.JPanel contenedorGuardados;
    private javax.swing.JPanel contenedorHeader;
    private javax.swing.JPanel contenedorHistorial;
    private javax.swing.JPanel contenedorListasSugerencias;
    private javax.swing.JPanel contenedorLogin;
    private javax.swing.JPanel contenedorMensajes;
    private javax.swing.JPanel contenedorPalabras;
    private javax.swing.JPanel contenedorPrincipal;
    private javax.swing.JPanel contenedorRegistro;
    private javax.swing.JPanel contenedorStaff;
    private javax.swing.JPanel contenedorStaffPanel;
    private javax.swing.JPanel contenedorSugerencias;
    private javax.swing.JPanel contenedorSugerenciasPersonales;
    private javax.swing.JPanel contenedorSugerir;
    private javax.swing.JPanel contenedorTotal;
    private javax.swing.JPanel contenedorTraduccion;
    private javax.swing.JLabel copyright;
    private javax.swing.JLabel copyrightRegistro;
    private javax.swing.JPanel cuadroInput;
    private javax.swing.JPanel cuadroOutput;
    private javax.swing.JLabel etiquetaCerrarSesion;
    private javax.swing.JLabel etiquetaConCuenta;
    private javax.swing.JLabel etiquetaMensajeErrorLogin;
    private javax.swing.JLabel etiquetaMensajeErrorRegistro;
    private javax.swing.JLabel etiquetaSinCuenta;
    private javax.swing.JLabel etiquetaStaffPanel;
    private javax.swing.JLabel iconoCPasswordRegistro;
    private javax.swing.JLabel iconoEsStaff;
    private javax.swing.JLabel iconoEstatusStaff;
    private javax.swing.JLabel iconoIdiomaFinal;
    private javax.swing.JLabel iconoIdiomaInicial;
    private javax.swing.JLabel iconoIdiomaInput;
    private javax.swing.JLabel iconoIdiomaOutput;
    private javax.swing.JLabel iconoLenguaMaterna;
    private javax.swing.JLabel iconoListarStaff;
    private javax.swing.JLabel iconoNombreRegistro;
    private javax.swing.JLabel iconoPasswordLogin;
    private javax.swing.JLabel iconoPasswordRegistro;
    private javax.swing.JLabel iconoRolStaff;
    private javax.swing.JLabel iconoSancionesEmitidasStaff;
    private javax.swing.JLabel iconoStaffDesdeStaff;
    private javax.swing.JLabel iconoStaffHastaStaff;
    private javax.swing.JLabel iconoSugerenciasAceptadas;
    private javax.swing.JLabel iconoSugerenciasRechazadasStaff;
    private javax.swing.JLabel iconoUsuarioLogin;
    private javax.swing.JLabel iconoUsuarioRegistro;
    private javax.swing.JLabel iconoUsuarioStaff;
    private javax.swing.JComboBox<String> idiomasFinal;
    private javax.swing.JComboBox<String> idiomasInicial;
    private javax.swing.JComboBox<String> idiomasInput;
    private javax.swing.JComboBox<String> idiomasOutput;
    private javax.swing.JLabel imagenLogin;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JPanel lienzo;
    private javax.swing.JPanel lienzoSesion;
    private javax.swing.JPanel lienzoSugerencias;
    private javax.swing.JLabel mensajeAccion;
    private javax.swing.JPanel panelBotonesLogin;
    private javax.swing.JPanel panelBotonesRegistro;
    private javax.swing.JPanel panelInformacionLogin;
    private javax.swing.JPanel panelInformacionRegistro;
    private javax.swing.JPanel panelPerfilLogin;
    private javax.swing.JPanel panelPerfilRegistro;
    private javax.swing.JPanel panelSeparadorLogin;
    private javax.swing.JPanel panelSeparadorRegistro;
    private javax.swing.JPasswordField passwordFieldCPasswordRegistro;
    private javax.swing.JPasswordField passwordFieldPasswordLogin;
    private javax.swing.JPasswordField passwordFieldPasswordRegistro;
    private javax.swing.JLabel placeholderEsStaff;
    private javax.swing.JLabel placeholderSancionesEmitidasStaff;
    private javax.swing.JLabel placeholderStaffDesdeStaff;
    private javax.swing.JLabel placeholderStaffHastaStaff;
    private javax.swing.JLabel placeholderStatusStaff;
    private javax.swing.JLabel placeholderSugerenciasAprobadasStaff;
    private javax.swing.JLabel placeholderSugerenciasRechazadasStaff;
    private javax.swing.JLabel placeholderUsuarioStaff;
    private javax.swing.JScrollPane scrollPanelGuardados;
    private javax.swing.JScrollPane scrollPanelHistorial;
    private javax.swing.JScrollPane scrollPanelPalabras;
    private javax.swing.JScrollPane scrollPanelSugerenciasPersonales;
    private javax.swing.JLabel seccionCuenta;
    private javax.swing.JLabel seccionPalabras;
    private javax.swing.JLabel seccionSugerenciasStaff;
    private javax.swing.JLabel seccionTraducir;
    private javax.swing.JLabel subtituloApartadosStaffPanel;
    private javax.swing.JTable tablaGuardados;
    private javax.swing.JTable tablaHistorial;
    private javax.swing.JTable tablaPalabras;
    private javax.swing.JTable tablaSugerencias;
    private javax.swing.JTable tablaSugerenciasPersonales;
    private javax.swing.JTextArea textAreaInput;
    private javax.swing.JTextArea textAreaOutput;
    private javax.swing.JTextField textFieldNombreDatos;
    private javax.swing.JTextField textFieldNombreRegistro;
    private javax.swing.JTextField textFieldPalabra;
    private javax.swing.JTextField textFieldSugerencia;
    private javax.swing.JTextField textFieldTraduccion;
    private javax.swing.JTextField textFieldUsuarioDatos;
    private javax.swing.JTextField textFieldUsuarioLogin;
    private javax.swing.JTextField textFieldUsuarioRegistro;
    private javax.swing.JLabel tituloAprobarSugerencia;
    private javax.swing.JLabel tituloBuscarStaff;
    private javax.swing.JLabel tituloBusquedas;
    private javax.swing.JLabel tituloCPasswordRegistro;
    private javax.swing.JLabel tituloCancelarCambios;
    private javax.swing.JLabel tituloConfirmarCambios;
    private javax.swing.JLabel tituloDatosPersonales;
    private javax.swing.JLabel tituloEditarDatos;
    private javax.swing.JLabel tituloEnviarSugerencia;
    private javax.swing.JLabel tituloEsStaff;
    private javax.swing.JLabel tituloEstatusStaff;
    private javax.swing.JLabel tituloGuardados;
    private javax.swing.JLabel tituloIdiomaFinal;
    private javax.swing.JLabel tituloIdiomaInicial;
    private javax.swing.JLabel tituloLengua;
    private javax.swing.JLabel tituloNombre;
    private javax.swing.JLabel tituloNombreRegistro;
    private javax.swing.JLabel tituloPalabra;
    private javax.swing.JLabel tituloPasswordLogin;
    private javax.swing.JLabel tituloPasswordRegistro;
    private javax.swing.JLabel tituloRechazarSugerencia;
    private javax.swing.JLabel tituloRegistro;
    private javax.swing.JLabel tituloRemoverElementoHistorial;
    private javax.swing.JLabel tituloRemoverGuardado;
    private javax.swing.JLabel tituloRemoverPalabra;
    private javax.swing.JLabel tituloRemoverSugerencia;
    private javax.swing.JLabel tituloRolStaff;
    private javax.swing.JLabel tituloSacionesGlobales;
    private javax.swing.JLabel tituloSanciones;
    private javax.swing.JLabel tituloSancionesEmitidasStaff;
    private javax.swing.JLabel tituloSeccion;
    private javax.swing.JLabel tituloStaff;
    private javax.swing.JLabel tituloStaffDesdeStaff;
    private javax.swing.JLabel tituloStaffHastaStaff;
    private javax.swing.JLabel tituloSugerencia;
    private javax.swing.JLabel tituloSugerencias;
    private javax.swing.JLabel tituloSugerenciasAprobadas;
    private javax.swing.JLabel tituloSugerenciasGlobales;
    private javax.swing.JLabel tituloSugerenciasRechazadasStaff;
    private javax.swing.JLabel tituloTraduccion;
    private javax.swing.JLabel tituloUsuario;
    private javax.swing.JLabel tituloUsuarioLogin;
    private javax.swing.JLabel tituloUsuarioRegistro;
    private javax.swing.JLabel tituloUsuarioStaff;
    private javax.swing.JLabel tituloVaciarGuardados;
    private javax.swing.JLabel tituloVaciarHistorial;
    private javax.swing.JLabel tituloVaciarPalabras;
    private javax.swing.JLabel tituloVaciarSugerencias;
    private javax.swing.JLabel tituloVolverGuardados;
    private javax.swing.JLabel tituloVolverHistorial;
    private javax.swing.JLabel tituloVolverStaffPanel;
    private javax.swing.JLabel tituloVolverSugerenciasPersonales;
    // End of variables declaration//GEN-END:variables
}
