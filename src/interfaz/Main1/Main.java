package interfaz.Main1;

import brainstorm.BrainstormStart;
import brainstorm.ConfigManager;
import brainstorm.MATLABDetector;
import brainstorm.MATLABPathSelector;
import brainstorm.info.BrainstormContext;
import com.mathworks.engine.EngineException;
import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;
import javaswingdev.GoogleMaterialDesignIcon;
import javax.swing.JOptionPane;
import interfaz.form.Form_Dashboard;
import interfaz.menu.EventMenuSelected;
import interfaz.menu.ModelMenuItem;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main extends javax.swing.JFrame {

    private static Main main;
    public BrainstormContext bCon;
    public Form_Dashboard form = null;
    public int flagBrainStorm;
    ModelMenuItem studies;

    public Main() throws EngineException, InterruptedException {
        bCon = BrainstormStart.getInstance();
        bCon.startBrainstorm();
        initComponents();
        this.flagBrainStorm = 0;
        init();
    }

    public void init() {
        bCon.addPath();
        bCon.currentProtocolIndex();
        bCon.loadProtocol();
        main = this;
        //Elementos del menu
        menu1.addTitle("MAIN");  //indice 1 en la lista de componentes del panelMenu
        menu1.addMenuItem(new ModelMenuItem(GoogleMaterialDesignIcon.DASHBOARD, "Dashboard"));// indice de evento 0 
        menu1.addTitle("PROTOCOL: "); // indice 2
        menu1.addTitle("STAGE: "); //Indice 3
        menu1.addTitle("FILE: "); // indice 4 en la lista de componentes del panelMenu
        menu1.addMenuItem(new ModelMenuItem(null, "Ready Files"));
        menu1.addMenuItem(new ModelMenuItem(null, "Visualization", "Graphics"));
        menu1.addMenuItemBottom(new ModelMenuItem(null, "Reset Dashboard"));
        //Fin de elementos del menu
        menu1.updateTittleProtocol("");
        menu1.updateTittleSujeto("");

        menu1.addEvent(new EventMenuSelected() {
            @Override
            //Aqui hay que meter un case con la claves de las funciones que vamos a llamar en casa de que el evento seleccionado tenga la clave
            public void menuSelected(int index, int indexSubMenu, String aux, String subKey) {
                // El indice 0 esta guardado para el primer evento del menu, este lo vamos a considerar como el evento que llama al "Inicio"
                if (index == 0 && indexSubMenu == 0) {
                    System.out.println(index);
                    if (form == null) {
                        form = new Form_Dashboard(main);
                        showForm(form);
                    } else {
                        showForm(form);
                    }
                    //Hay que hacer funciones que manden el cambion a la ventana Main, desde Dashboard.
                } else {
                    //Aqui dependiento del evento llamaremos a un caso, tendremos "Recordings", "Sincronizacion", y otras opciones una vez este realizada la sincronizacion
                    System.out.println("El indice es el: " + index);
                    String clave = aux;
                    if (clave.equals("Ready Files")) {

                        if (indexSubMenu > 0 && subKey != "") {
                            bCon.setStudyContext(indexSubMenu + 2);
                            menu1.updateTittleStudy(bCon.getStudy().nombreStudy());
                        }
                        System.out.println(aux); // aux tiene el texto del nombre del dropdown
                        System.out.println(indexSubMenu); //Sub indice de la lista, 
                        System.out.println(subKey); // subkey te arroja el nombre de el objeto seleccionado
                    } else if (clave.equals("Visualization")) {
                        if (indexSubMenu == 1) {
                            bCon.generateTimeSeries();

                        }else{
                        }
                    } else if (clave.equals("Reset Dashboard")) {
                        if (flagBrainStorm == 1) {
                            bCon.startBrainstorm();
                            form.reset();
                            JOptionPane.showMessageDialog(null, "Starting Brainstorm engine",
                                    "Warning", JOptionPane.WARNING_MESSAGE);
                            flagBrainStorm = 0;
                        } else {
                            int respuesta = JOptionPane.showConfirmDialog(null, "Do you want to reload the dashboard?", "Confirmation", JOptionPane.YES_NO_OPTION);

                            if (respuesta == JOptionPane.YES_OPTION) {
                                // Aquí va el código que se ejecutará si el usuario elige "Sí"
                                form.reset();
                                JOptionPane.showMessageDialog(null, "Complete");
                                // Puedes agregar aquí cualquier otra acción que quieras realizar
                            } else {
                                // Aquí puedes agregar código si quieres hacer algo cuando el usuario elige "No"
                                // En este caso, no haremos nada, así que podemos dejarlo en blanco
//                                JOptionPane.showMessageDialog(null, "Acción cancelada.");
                            }
                        }
                    }
                }
            }
        });
        menu1.setSelectedIndex(0, 0, "", "");
    }

    public void showForm(Component com) {
        body.removeAll();
        body.add(com);
        body.repaint();
        body.revalidate();
    }

    public void updateTitleProtocolo() {
        try {

            menu1.updateTittleProtocol(bCon.getProtocol().nombreProtocolo());
        } catch (Exception e) {
            menu1.updateTittleProtocol("Empty");
        }
    }

    public void updateTitleSujeto() {
        try {
            menu1.updateTittleSujeto(bCon.getSubject().nombreSujeto());
        } catch (Exception e) {
            menu1.updateTittleSujeto("Empty");
        }
    }

    public void updateTitleStudy() {
        try {
            String aux = bCon.getStudy().nombreStudy().replaceAll("@raw", "");
            menu1.updateTittleStudy(aux);
        } catch (Exception e) {
            menu1.updateTittleStudy("Empty");
        }
    }

    public void addMenuItem(String[] studiesList) {
        int index = menu1.removeMenuItem();
        String[] aux = this.cleanStudies(studiesList);
        ModelMenuItem studies = new ModelMenuItem(null, "Ready Files");
        studies.setSubMenu(aux);
        menu1.addMenuItemAgain(studies, index);
    }

    public String[] cleanStudies(String[] studiesList) {
        if (studiesList.length - 2 <= 0) {
            String[] clean = {""};
            return clean;
        }
        String[] clean = new String[studiesList.length - 2];
        int i = 0;
        for (String study : studiesList) {
            if (study.equals("@default_study") || study.equals("@intra")) {

            } else {

                String aux = study.replaceAll("@raw", "");
                clean[i] = aux;
                i += 1;
                System.out.println(aux);
            }
        }
        return clean;
    }

    // Agregar un WindowAdapter
    public static Main getMain() {
        return main;
    }

    private static void generarLogError(Throwable ex) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String timestamp = sdf.format(new Date());
            String logFileName = "error_log_" + timestamp + ".txt";

            try (PrintWriter writer = new PrintWriter(new FileWriter(logFileName, true))) {
                writer.println("=== ERROR [" + timestamp + "] ===");
                ex.printStackTrace(writer);
                writer.println("\nSistema:");
                writer.println("OS: " + System.getProperty("os.name"));
                writer.println("Java: " + System.getProperty("java.version"));
                writer.println("----------------------------------------");
            }

        } catch (IOException e) {
            System.err.println("Error al escribir log: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        background = new javax.swing.JPanel();
        panelMenu = new javax.swing.JPanel();
        menu1 = new interfaz.menu.Menu();
        body = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setAutoRequestFocus(false);

        background.setBackground(new java.awt.Color(245, 245, 245));

        panelMenu.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout panelMenuLayout = new javax.swing.GroupLayout(panelMenu);
        panelMenu.setLayout(panelMenuLayout);
        panelMenuLayout.setHorizontalGroup(
            panelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 202, Short.MAX_VALUE)
            .addGroup(panelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelMenuLayout.createSequentialGroup()
                    .addComponent(menu1, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        panelMenuLayout.setVerticalGroup(
            panelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 705, Short.MAX_VALUE)
            .addGroup(panelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(menu1, javax.swing.GroupLayout.DEFAULT_SIZE, 705, Short.MAX_VALUE))
        );

        body.setOpaque(false);
        body.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout backgroundLayout = new javax.swing.GroupLayout(background);
        background.setLayout(backgroundLayout);
        backgroundLayout.setHorizontalGroup(
            backgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundLayout.createSequentialGroup()
                .addComponent(panelMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(body, javax.swing.GroupLayout.DEFAULT_SIZE, 866, Short.MAX_VALUE)
                .addContainerGap())
        );
        backgroundLayout.setVerticalGroup(
            backgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(body, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(background, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(background, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        try {

            /* Set the Nimbus look and feel */
            //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
            /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
             */
            try {
                for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (ClassNotFoundException ex) {
                java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (javax.swing.UnsupportedLookAndFeelException ex) {
                java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
            //</editor-fold>

            /* Configurar MATLAB antes de abrir la interfaz */
            String matlabPath = ConfigManager.loadPath(); // Cargar ruta guardada

            // Si no hay ruta guardada o es inválida, detectar automáticamente
            if (matlabPath == null || !new File(matlabPath + "\\bin\\matlab.exe").exists()) {
                matlabPath = MATLABDetector.detectMATLABPath();
            }

            // Si la detección automática falla, pedir al usuario
            if (matlabPath == null) {
                int option = JOptionPane.showConfirmDialog(
                        null,
                        "MATLAB no se detectó. ¿Desea seleccionar la ruta manualmente?",
                        "MATLAB no encontrado",
                        JOptionPane.YES_NO_OPTION
                );

                if (option == JOptionPane.YES_OPTION) {
                    matlabPath = MATLABPathSelector.selectMATLABPath();
                    if (matlabPath != null) {
                        ConfigManager.savePath(matlabPath); // Guardar para futuras ejecuciones
                    } else {
                        JOptionPane.showMessageDialog(null, "La aplicación no puede iniciar sin MATLAB.", "Error", JOptionPane.ERROR_MESSAGE);
                        System.exit(1);
                    }
                } else {
                    System.exit(1);
                }
            }

            // Configurar rutas de MATLAB
            System.setProperty("java.library.path", matlabPath + "\\bin\\win64");
            try {
                System.load(matlabPath + "\\bin\\win64\\libeng.dll"); // Ejemplo para una DLL crítica
            } catch (UnsatisfiedLinkError e) {
                JOptionPane.showMessageDialog(null, "Error al cargar bibliotecas de MATLAB.", "Error", JOptionPane.ERROR_MESSAGE);
                generarLogError(e);
                System.exit(1);
            }

            /* Crear y mostrar la interfaz */
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    try {
                        new Main().setVisible(true);
                    } catch (EngineException | InterruptedException ex) {
                        JOptionPane.showMessageDialog(
                                null,
                                "Error al iniciar MATLAB: " + ex.getMessage(),
                                "Error crítico",
                                JOptionPane.ERROR_MESSAGE
                        );
                        generarLogError(ex);
                        System.exit(1);
                    }
                }
            });
        } catch (Throwable t) {
            // Nuevo: Manejo global de errores no capturados
            generarLogError(t);
            JOptionPane.showMessageDialog(
                    null,
                    "Error crítico no esperado. Verifique el archivo de log.",
                    "Error fatal",
                    JOptionPane.ERROR_MESSAGE
            );
            System.exit(1);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel background;
    private javax.swing.JPanel body;
    private interfaz.menu.Menu menu1;
    private javax.swing.JPanel panelMenu;
    // End of variables declaration//GEN-END:variables
}
