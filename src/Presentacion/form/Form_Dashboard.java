package presentacion.form;

import Acceso_Datos.emotiv.PreprocesarEmotiv;
import Acceso_Datos.neulog.PreprocesarNeulog;
import brainstorm.BrainstormStart;
import brainstorm.info.BrainstormContext;
import com.mathworks.engine.EngineException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import logica.Sincronizacion;
import presentacion.card.ModelCard;
import presentacion.main.Main;
import presentacion.menu.Menu;

public class Form_Dashboard extends javax.swing.JPanel {

    private BrainstormContext bCon;
    private Main main;
    private Sincronizacion sync;
    private int flag;

    public Form_Dashboard(Main main) {
        try {
            bCon = BrainstormStart.getInstance();
        } catch (EngineException ex) {
            Logger.getLogger(Form_Dashboard.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Form_Dashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.main = main;
        this.sync = new Sincronizacion("", "", "", "");
        this.flag = 0;
        initComponents();
        init();
    }

    private void init() {
        card1.setData(new ModelCard(null, null, null, bCon.currentProtocolName(), "Protocol"));
        card2.setData(new ModelCard(null, null, null, bCon.currentSujectName(), "Subject"));
//        card3.setData(new ModelCard(null, null, null, bCon.currentStudyName(), "Study"));
        this.llenar();
        this.llenarSujetos();
        this.protocolList.setSelectedIndex(-1);
        this.subjectList.setSelectedIndex(-1);

        this.protocolList.addActionListener(e -> {
            String seleccion = (String) this.protocolList.getSelectedItem();
            if (seleccion != null) {
                double index = bCon.protocolIndex(seleccion);
                bCon.setProtocol(index);
                bCon.reload();
                this.actualizar();
            }
        });
        this.syncButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // Verificar la condición antes de ejecutar la sincronización
                if (flag == 4) { // Reemplaza cumpleCondicion() con tu lógica
                    // Deshabilitar el botón para evitar múltiples clics mientras trabaja
                    syncButton.setEnabled(false);
                    // Ejecutar la acción en un SwingWorker (hilo en segundo plano)
                    SwingWorker<Void, Void> worker = new SwingWorker<>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            // Aquí va la lógica de sincronización
                            System.out.println("Sincronizando...");
                            sync.sincronizar(); // Tu lógica de sincronización
                            Thread.sleep(2000); // Simula un trabajo de 2 segundos
                            return null;
                        }

                        @Override
                        protected void done() {
                            // Una vez completado, restaurar el botón
                            syncButton.setEnabled(true);
                            syncButton.getModel().setPressed(false);
                            syncButton.getModel().setArmed(false);
                            JOptionPane.showMessageDialog(null, "Sincronización completada");
                        }
                    };
                    // Iniciar el trabajo en segundo plano
                    worker.execute();
                } else {
                    // Mostrar mensaje de advertencia si no se cumple la condición
                    JOptionPane.showMessageDialog(
                            null,
                            "No se cumplen las condiciones para la sincronización.",
                            "Advertencia",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        });
        this.emotivFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (bCon.getSubject() != null) {
                    System.out.println("Condición cumplida. Ejecutando acción...");
                    try {
                        List<String> valores = PreprocesarEmotiv.generateFiles();
                        String value = valores.get(0);
                        String powers = valores.get(1);
                        if (value != null) {
                            JOptionPane.showMessageDialog(null, value);
                            flag += 2;
                            sync.setRutaEmotiv(value);
                            sync.setRutaMarcadoresEmotiv(PreprocesarEmotiv.generarArchivoMarcadores(value));
                            sync.setRutaPowers(powers);
                        } else {
                            JOptionPane.showMessageDialog(null, "Error de procesamiento");
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, "Error de procesamiento");
                    }
                } else {
                    JOptionPane.showMessageDialog(
                            null,
                            "No se cumplen las condiciones para realizar esta acción.",
                            "Advertencia",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        });
        this.neulogFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (bCon.getSubject() != null) {
                    System.out.println("Condición cumplida. Ejecutando acción...");
                    try {
                        String value = PreprocesarNeulog.traerArchivo();
                        if (value != null) {
                            JOptionPane.showMessageDialog(null, value);
                            flag += 2;
                            sync.setRutaNeulog(value);
                            sync.setRutaMarcadoresNeulog(PreprocesarNeulog.generarArchivoMarcadores(value));
                        } else {
                            JOptionPane.showMessageDialog(null, "Error de procesamiento");
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, "Error de procesamiento");
                    }
                } else {
                    JOptionPane.showMessageDialog(
                            null,
                            "No se cumplen las condiciones para realizar esta acción.",
                            "Advertencia",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        });

        this.eliminarProtocolo.addActionListener(e -> {
            // Crear un JLabel personalizado con texto rojo
            JLabel mensaje = new JLabel("¿Estás seguro de realizar esta acción?");
            mensaje.setForeground(Color.RED);
            mensaje.setFont(new Font("Arial", Font.BOLD, 14));

            // Mostrar el JOptionPane con opciones Sí y No
            int respuesta = JOptionPane.showConfirmDialog(
                    main, // Componente padre
                    mensaje, // Mensaje (puede ser un JLabel)
                    "Advertencia", // Título de la ventana
                    JOptionPane.YES_NO_OPTION, // Opciones disponibles
                    JOptionPane.WARNING_MESSAGE // Tipo de mensaje
            );

            // Procesar la respuesta del usuario
            if (respuesta == JOptionPane.YES_OPTION) {
                bCon.deleteProtocol();
                this.main.showForm(new Form_Dashboard(main));
                this.main.updateTitleProtocolo();
                this.main.updateTitleStudy();
                this.main.updateTitleSujeto();
                System.out.println("El usuario eligió 'Sí'.");
            } else if (respuesta == JOptionPane.NO_OPTION) {
                System.out.println("El usuario eligió 'No'.");
            } else {
                System.out.println("El usuario cerró el cuadro de diálogo.");
            }
        });

        
    }

    public String cambio() {
        return "Prueba";
    }

    public void actualizar() {
        card1.setData(new ModelCard(null, null, null, bCon.currentProtocolName(), "Protocol"));
        card2.setData(new ModelCard(null, null, null, bCon.currentSujectName(), "Subject"));
//        card3.setData(new ModelCard(null, null, null, "", "Study"));

        this.main.updateTitleProtocolo();
        this.actionListenerSujetos();
        this.llenarSujetos();
    }

    public void actualizatStudyCard() {
//        card3.setData(new ModelCard(null, null, null, bCon.study.nombreStudy(), "Study"));
    }

    public void actionListenerSujetos() {

        this.subjectList.addActionListener(e -> {
            String seleccion = (String) this.subjectList.getSelectedItem();
            String[] listaSujetos = bCon.protocolSubjects();
            if (seleccion != null) {
                for (int i = 1; i <= listaSujetos.length; i++) {
                    if (seleccion.equals(listaSujetos[i - 1])) {
                        int iSubject = i;
                        bCon.setSubject(iSubject);
                        this.main.updateTitleStudy();
                        this.actualizarSujetos(listaSujetos[i - 1]);
                        String[] prueba = bCon.subjectStudiesArray(listaSujetos[i - 1]);
                        this.main.addMenuItem(prueba);
                    }
                }
            }
        });
    }

    public void actualizarSujetos(String sujeto) {
        this.main.updateTitleSujeto();
        card2.setData(new ModelCard(null, null, null, sujeto, "Subject"));
    }

    public String[] protocolList() {
        return bCon.protocolList();
    }

    public void llenar() {
        for (String opcion : this.protocolList()) {
//            this.protocolList.addItem(opcion);
            if (bCon.protocolIndex(opcion) == 0) {
                System.out.println("Empty");
            } else {
                this.protocolList.addItem(opcion);
            }
        }
    }

    public void llenarSujetos() {
        this.subjectList.removeAllItems();
        String[] sujetos = bCon.protocolSubjects();

        if (sujetos[0] == "") {
            bCon.setSubject(-1);
            String[] prueba = {""};
            this.actualizarSujetos("No subjects");
            this.main.addMenuItem(prueba);

            System.out.println("No hay sujetos");
        } else {
            for (String opcion : sujetos) {
                this.subjectList.addItem(opcion);
            }
        }
//        bCon.protocolStudies();

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        card1 = new Presentacion.Card();
        card2 = new Presentacion.Card();
        roundPanel1 = new presentacion.swing.RoundPanel();
        protocolList = new javax.swing.JComboBox<>();
        subjectList = new javax.swing.JComboBox<>();
        crearProtocolo = new javax.swing.JButton();
        eliminarProtocolo = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        neulogPath = new javax.swing.JLabel();
        emotivPath = new javax.swing.JLabel();
        syncButton = new javax.swing.JButton();
        neulogFiles = new javax.swing.JButton();
        emotivFiles = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(879, 661));

        card2.setColor1(new java.awt.Color(95, 211, 226));
        card2.setColor2(new java.awt.Color(26, 166, 170));
        card2.setIcon(javaswingdev.GoogleMaterialDesignIcon.PIE_CHART);

        roundPanel1.setBackground(new java.awt.Color(255, 255, 255));
        roundPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        roundPanel1.setPreferredSize(new java.awt.Dimension(819, 451));
        roundPanel1.setRound(10);

        crearProtocolo.setText("Nuevo Protocolo");
        crearProtocolo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                crearProtocoloActionPerformed(evt);
            }
        });

        eliminarProtocolo.setText("Eliminar Protocolo");
        eliminarProtocolo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eliminarProtocoloActionPerformed(evt);
            }
        });

        jButton3.setText("Nuevo Sujeto");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Eliminar Sujeto");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        jLabel1.setText("Sincronización");

        neulogPath.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        neulogPath.setText("Archivo Cargado");

        emotivPath.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        emotivPath.setText("Archivo Cargado");

        syncButton.setText("Sincronizar");
        syncButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                syncButtonActionPerformed(evt);
            }
        });

        neulogFiles.setText("Archivos GSR y FC");
        neulogFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                neulogFilesActionPerformed(evt);
            }
        });

        emotivFiles.setText("Archivo EEG");
        emotivFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                emotivFilesActionPerformed(evt);
            }
        });

        jButton1.setText("Actualizar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout roundPanel1Layout = new javax.swing.GroupLayout(roundPanel1);
        roundPanel1.setLayout(roundPanel1Layout);
        roundPanel1Layout.setHorizontalGroup(
            roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundPanel1Layout.createSequentialGroup()
                .addGap(150, 150, 150)
                .addComponent(protocolList, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
                .addComponent(subjectList, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(150, 150, 150))
            .addGroup(roundPanel1Layout.createSequentialGroup()
                .addGap(222, 222, 222)
                .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(eliminarProtocolo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(crearProtocolo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4))
                .addGap(222, 222, 222))
            .addComponent(jSeparator1)
            .addGroup(roundPanel1Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(roundPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(roundPanel1Layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addComponent(emotivPath)
                        .addGap(141, 141, 141)
                        .addComponent(neulogPath))
                    .addGroup(roundPanel1Layout.createSequentialGroup()
                        .addComponent(emotivFiles, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(83, 83, 83)
                        .addComponent(neulogFiles, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(syncButton, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(roundPanel1Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(jButton1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        roundPanel1Layout.setVerticalGroup(
            roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundPanel1Layout.createSequentialGroup()
                .addGap(66, 66, 66)
                .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(protocolList, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(subjectList, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(crearProtocolo)
                    .addComponent(jButton3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(eliminarProtocolo)
                    .addComponent(jButton4))
                .addGap(52, 52, 52)
                .addComponent(jButton1)
                .addGap(32, 32, 32)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 46, Short.MAX_VALUE)
                .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(neulogFiles)
                    .addComponent(emotivFiles))
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(neulogPath)
                    .addComponent(emotivPath))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(syncButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(roundPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 839, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(card1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(135, 135, 135)
                        .addComponent(card2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(20, 20, 20))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(card2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(card1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(roundPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE)
                .addGap(30, 30, 30))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void syncButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_syncButtonActionPerformed

//        try {
//            if (flag == 2) {
//                JOptionPane.showMessageDialog(null, "Falta un archivo por cargar");
//            } else if (flag == 4) {
//
//            }
//        } catch (Exception e) {
//
//        }
    }//GEN-LAST:event_syncButtonActionPerformed

    private void neulogFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_neulogFilesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_neulogFilesActionPerformed

    private void emotivFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_emotivFilesActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_emotivFilesActionPerformed

    private void eliminarProtocoloActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eliminarProtocoloActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_eliminarProtocoloActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        bCon.deleteSubject();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void crearProtocoloActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_crearProtocoloActionPerformed
        // TODO add your handling code here:
        bCon.crearProtocolo();
    }//GEN-LAST:event_crearProtocoloActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        bCon.creatSujeto();

    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        init();
        this.main.updateTitleProtocolo();
        this.main.updateTitleStudy();
        this.main.updateTitleSujeto();
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private Presentacion.Card card1;
    private Presentacion.Card card2;
    private javax.swing.JButton crearProtocolo;
    private javax.swing.JButton eliminarProtocolo;
    private javax.swing.JButton emotivFiles;
    private javax.swing.JLabel emotivPath;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton neulogFiles;
    private javax.swing.JLabel neulogPath;
    private javax.swing.JComboBox<String> protocolList;
    private presentacion.swing.RoundPanel roundPanel1;
    private javax.swing.JComboBox<String> subjectList;
    private javax.swing.JButton syncButton;
    // End of variables declaration//GEN-END:variables
}
