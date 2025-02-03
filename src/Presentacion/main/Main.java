package presentacion.main;

import brainstorm.BrainstormStart;
import brainstorm.info.BrainstormContext;
import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabEngine;
import java.awt.Component;
import static java.awt.SystemColor.menu;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javaswingdev.GoogleMaterialDesignIcon;
import presentacion.form.Form_Dashboard;
import presentacion.form.Form_Empty;
import presentacion.menu.EventMenuSelected;
import presentacion.menu.ModelMenuItem;

public class Main extends javax.swing.JFrame {

    private static Main main;
    public BrainstormContext bCon;
    ModelMenuItem studies;

    public Main() throws EngineException, InterruptedException {
        bCon = BrainstormStart.getInstance();
        bCon.startBrainstorm();
        initComponents();
        init();
    }

    private void init() {
        bCon.currentProtocolIndex();
        bCon.loadProtocol();
        main = this;
//        System.out.println(bCon.protocolStudies()[1]);
        //Elementos del menu
        menu1.addTitle("MAIN");  //indice 1 en la lista de componentes del panelMenu
        menu1.addMenuItem(new ModelMenuItem(GoogleMaterialDesignIcon.DASHBOARD, "Dashboard"));// indice de evento 0 
        menu1.addTitle("PROTOCOLO: "); // indice 2
        menu1.addTitle("SUJETO: "); //Indice 3
        menu1.addTitle("FILE: "); // indice 4 en la lista de componentes del panelMenu
        menu1.addMenuItem(new ModelMenuItem(null, "Procesados"));
        menu1.addMenuItem(new ModelMenuItem(null, "Visualización","Grafica 1", "Grafica 2","Grafica3"  ));
        menu1.addMenuItemBottom(new ModelMenuItem(null, "Brainstorm" ));
        //Fin de elementos del menu
        menu1.updateTittleProtocol(bCon.currentProtocolName());
        menu1.updateTittleSujeto(bCon.currentSujectName());

        menu1.addEvent(new EventMenuSelected() {
            @Override
            //Aqui hay que meter un case con la claves de las funciones que vamos a llamar en casa de que el evento seleccionado tenga la clave
            public void menuSelected(int index, int indexSubMenu, String aux, String subKey) {
                // El indice 0 esta guardado para el primer evento del menu, este lo vamos a considerar como el evento que llama al "Inicio"
                if (index == 0 && indexSubMenu == 0) {
                    System.out.println(index);
                    showForm(new Form_Dashboard(main));
                    //Hay que hacer funciones que manden el cambion a la ventana Main, desde Dashboard.
                } else {
                    //Aqui dependiento del evento llamaremos a un caso, tendremos "Recordings", "Sincronizacion", y otras opciones una vez este realizada la sincronizacion
                    System.out.println("El indice es el: " + index);
                    String clave = aux;
                    switch (clave) {
                        case "Procesados":
                            if (indexSubMenu > 0) {
                                bCon.setStudyContext(indexSubMenu);
                                bCon.channelStudy();
                                menu1.updateTittleStudy(bCon.getStudy().nombreStudy());
                            }
                            System.out.println(aux); // aux tiene el texto del nombre del dropdown
                            System.out.println(indexSubMenu); //Sub indice de la lista, 
                            System.out.println(subKey); // subkey te arroja el nombre de el objeto seleccionado

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
        menu1.updateTittleProtocol(bCon.getProtocol().nombreProtocolo());
    }

    public void updateTitleSujeto() {
        try {
            menu1.updateTittleSujeto(bCon.getSubject().nombreSujeto());
        } catch (Exception e) {
            menu1.updateTittleSujeto("Vacio");
        }
    }
    public void updateTitleStudy() {
        try {
            menu1.updateTittleStudy(bCon.getStudy().nombreStudy());
        } catch (Exception e) {
            menu1.updateTittleStudy("Vacio");
        }
    }

    public void addMenuItem(String[] studiesList) {
        int index=menu1.removeMenuItem();
        ModelMenuItem studies = new ModelMenuItem(null, "Procesados");
        studies.setSubMenu(studiesList);
        menu1.addMenuItemAgain(studies,index);
    }
    
    // Agregar un WindowAdapter

    public static Main getMain() {
        return main;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        background = new javax.swing.JPanel();
        panelMenu = new javax.swing.JPanel();
        menu1 = new presentacion.menu.Menu();
        body = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setAlwaysOnTop(true);

        background.setBackground(new java.awt.Color(245, 245, 245));

        panelMenu.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout panelMenuLayout = new javax.swing.GroupLayout(panelMenu);
        panelMenu.setLayout(panelMenuLayout);
        panelMenuLayout.setHorizontalGroup(
            panelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 226, Short.MAX_VALUE)
            .addGroup(panelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(menu1, javax.swing.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE))
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
                .addComponent(body, javax.swing.GroupLayout.DEFAULT_SIZE, 1092, Short.MAX_VALUE)
                .addContainerGap())
        );
        backgroundLayout.setVerticalGroup(
            backgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(backgroundLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(body, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
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

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new Main().setVisible(true);
                } catch (EngineException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel background;
    private javax.swing.JPanel body;
    private presentacion.menu.Menu menu1;
    private javax.swing.JPanel panelMenu;
    // End of variables declaration//GEN-END:variables
}
