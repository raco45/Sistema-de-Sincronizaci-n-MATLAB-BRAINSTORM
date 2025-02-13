package presentacion.form;

import Acceso_Datos.emotiv.PreprocesarEmotiv;
import Acceso_Datos.neulog.PreprocesarNeulog;
import brainstorm.BrainstormStart;
import brainstorm.info.BrainstormContext;
import com.mathworks.engine.EngineException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import static java.awt.Frame.ICONIFIED;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
                            sync.resetSync();
                            flag = 0;
                            labelEmotiv.setText("No file loaded");
                            labelNeulog.setText("No file loaded");
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
                            labelEmotiv.setText("File Loaded");
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
                            labelNeulog.setText("File Loaded");
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
            if (bCon.protocol != null) {
                // Crear un JLabel personalizado con texto rojo
                JLabel mensaje = new JLabel(String.format("Do you want to delete Protocol: '%s'", bCon.protocol.nombreProtocolo()));
                mensaje.setForeground(Color.RED);
                mensaje.setFont(new Font("Arial", Font.BOLD, 14));

                // Mostrar el JOptionPane con opciones Sí y No
                int respuesta = JOptionPane.showConfirmDialog(
                        main, // Componente padre
                        mensaje, // Mensaje (puede ser un JLabel)
                        "Warning", // Título de la ventana
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
            } else {
                JOptionPane.showMessageDialog(null, "No protocol selected");
            }
        });
        this.deleteSubject.addActionListener(e -> {
            if (bCon.subject != null) {
                // Crear un JLabel personalizado con texto rojo
                JLabel mensaje = new JLabel(String.format("Do you want to delete Subject: '%s'", bCon.subject.nombreSujeto()));
                mensaje.setForeground(Color.RED);
                mensaje.setFont(new Font("Arial", Font.BOLD, 14));

                // Mostrar el JOptionPane con opciones Sí y No
                int respuesta = JOptionPane.showConfirmDialog(
                        main, // Componente padre
                        mensaje, // Mensaje (puede ser un JLabel)
                        "Warning", // Título de la ventana
                        JOptionPane.YES_NO_OPTION, // Opciones disponibles
                        JOptionPane.WARNING_MESSAGE // Tipo de mensaje
                );

                // Procesar la respuesta del usuario
                if (respuesta == JOptionPane.YES_OPTION) {
                    bCon.deleteSubject();
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
            } else {
                JOptionPane.showMessageDialog(null, "No subject selected");
            }
        });
        this.powerBoton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Condición para ejecutar la acción

                if (bCon.study != null) {
                    // Aquí pones el código que quieres ejecutar
                    if (bCon.study.dataVideoFileName() != null) {
                        int respuesta = JOptionPane.showConfirmDialog(null,
                                "An FFT & Power file already exists.\nDo you want to regenerate them?",
                                "Confirmation",
                                JOptionPane.YES_NO_OPTION);

                        if (respuesta == JOptionPane.YES_OPTION) {
                            // Si elige "Sí", se ejecuta la acción
                            bCon.generarImagenes();
                            update();
                        } else {
                            // Si elige "No", se cancela la acción
                            JOptionPane.showMessageDialog(null, "Nothing happen.");
                        }
                    } else {
                        bCon.generarImagenes();
                        update();
                    }

                } else {
                    JOptionPane.showMessageDialog(null, "No study selected",
                            "Warning", JOptionPane.WARNING_MESSAGE);
                }
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

    public void update() {
        init();
        this.main.updateTitleProtocolo();
        this.main.updateTitleStudy();
        this.main.updateTitleSujeto();
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
        deleteSubject = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator1 = new javax.swing.JSeparator();
        syncLabel = new javax.swing.JLabel();
        syncLabel1 = new javax.swing.JLabel();
        syncButton = new javax.swing.JButton();
        neulogFiles = new javax.swing.JButton();
        emotivFiles = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        powerBoton = new javax.swing.JButton();
        labelEmotiv = new javax.swing.JLabel();
        labelNeulog = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        syncLabel2 = new javax.swing.JLabel();

        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(879, 661));

        card2.setColor1(new java.awt.Color(95, 211, 226));
        card2.setColor2(new java.awt.Color(26, 166, 170));
        card2.setIcon(javaswingdev.GoogleMaterialDesignIcon.PIE_CHART);

        roundPanel1.setBackground(new java.awt.Color(255, 255, 255));
        roundPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        roundPanel1.setPreferredSize(new java.awt.Dimension(819, 451));
        roundPanel1.setRound(10);

        crearProtocolo.setText("New Protocol");
        crearProtocolo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                crearProtocoloActionPerformed(evt);
            }
        });

        eliminarProtocolo.setText("Delete Protocol");
        eliminarProtocolo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eliminarProtocoloActionPerformed(evt);
            }
        });

        jButton3.setText("New Subject");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        deleteSubject.setText("Delete Subject");
        deleteSubject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteSubjectActionPerformed(evt);
            }
        });

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        syncLabel.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        syncLabel.setText("Context");

        syncLabel1.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        syncLabel1.setText("FFT & Powers");

        syncButton.setText("SYNCHRONIZE");
        syncButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                syncButtonActionPerformed(evt);
            }
        });

        neulogFiles.setText(" GSR & FC FILES");
        neulogFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                neulogFilesActionPerformed(evt);
            }
        });

        emotivFiles.setText("EEG FILE");
        emotivFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                emotivFilesActionPerformed(evt);
            }
        });

        jButton1.setText("Update");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        powerBoton.setText("GENERATE FFT & POWERS");
        powerBoton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                powerBotonActionPerformed(evt);
            }
        });

        labelEmotiv.setText("No file uploaded");

        labelNeulog.setText("No file uploaded");

        jButton2.setText("Clean Data");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);

        syncLabel2.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        syncLabel2.setText("Synchronization");

        javax.swing.GroupLayout roundPanel1Layout = new javax.swing.GroupLayout(roundPanel1);
        roundPanel1.setLayout(roundPanel1Layout);
        roundPanel1Layout.setHorizontalGroup(
            roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1)
            .addGroup(roundPanel1Layout.createSequentialGroup()
                .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(roundPanel1Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(emotivFiles, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(neulogFiles, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(91, 91, 91)
                        .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelNeulog)
                            .addComponent(labelEmotiv)))
                    .addGroup(roundPanel1Layout.createSequentialGroup()
                        .addGap(152, 152, 152)
                        .addComponent(syncButton, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(59, 59, 59)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(roundPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(syncLabel1)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, roundPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(powerBoton)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(roundPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(roundPanel1Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(syncLabel))
                    .addGroup(roundPanel1Layout.createSequentialGroup()
                        .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(subjectList, 0, 240, Short.MAX_VALUE)
                            .addComponent(protocolList, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(20, 20, 20)
                        .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(eliminarProtocolo)
                            .addComponent(crearProtocolo, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(deleteSubject, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(34, 34, 34)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(roundPanel1Layout.createSequentialGroup()
                    .addGap(36, 36, 36)
                    .addComponent(syncLabel2)
                    .addContainerGap(608, Short.MAX_VALUE)))
        );
        roundPanel1Layout.setVerticalGroup(
            roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundPanel1Layout.createSequentialGroup()
                .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(roundPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(roundPanel1Layout.createSequentialGroup()
                                .addComponent(syncLabel)
                                .addGap(56, 56, 56)
                                .addComponent(protocolList, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(73, 73, 73))
                            .addGroup(roundPanel1Layout.createSequentialGroup()
                                .addComponent(crearProtocolo)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(eliminarProtocolo)
                                .addGap(61, 61, 61)))
                        .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(roundPanel1Layout.createSequentialGroup()
                                .addComponent(subjectList, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(39, 39, 39))
                            .addGroup(roundPanel1Layout.createSequentialGroup()
                                .addComponent(jButton3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(deleteSubject)
                                .addGap(24, 24, 24))))
                    .addGroup(roundPanel1Layout.createSequentialGroup()
                        .addGap(74, 74, 74)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton1)
                        .addGap(64, 64, 64))
                    .addGroup(roundPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(roundPanel1Layout.createSequentialGroup()
                        .addComponent(syncLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(powerBoton)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(roundPanel1Layout.createSequentialGroup()
                        .addGap(61, 61, 61)
                        .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(emotivFiles)
                            .addComponent(labelEmotiv))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(neulogFiles)
                            .addComponent(labelNeulog))
                        .addGap(33, 33, 33)
                        .addComponent(syncButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, roundPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, roundPanel1Layout.createSequentialGroup()
                    .addContainerGap(304, Short.MAX_VALUE)
                    .addComponent(syncLabel2)
                    .addGap(156, 156, 156)))
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
                        .addComponent(card1, javax.swing.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)
                        .addGap(135, 135, 135)
                        .addComponent(card2, javax.swing.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)))
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
                .addComponent(roundPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE)
                .addGap(30, 30, 30))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void syncButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_syncButtonActionPerformed

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

    private void deleteSubjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteSubjectActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_deleteSubjectActionPerformed

    private void crearProtocoloActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_crearProtocoloActionPerformed
        // TODO add your handling code here:
        bCon.crearProtocolo();
        this.main.setState(ICONIFIED);
    }//GEN-LAST:event_crearProtocoloActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        bCon.creatSujeto();
        this.main.setState(ICONIFIED);

    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:

        this.update();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void powerBotonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_powerBotonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_powerBotonActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        this.bCon.openGUI();
    }//GEN-LAST:event_jButton2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private Presentacion.Card card1;
    private Presentacion.Card card2;
    private javax.swing.JButton crearProtocolo;
    private javax.swing.JButton deleteSubject;
    private javax.swing.JButton eliminarProtocolo;
    private javax.swing.JButton emotivFiles;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel labelEmotiv;
    private javax.swing.JLabel labelNeulog;
    private javax.swing.JButton neulogFiles;
    private javax.swing.JButton powerBoton;
    private javax.swing.JComboBox<String> protocolList;
    private presentacion.swing.RoundPanel roundPanel1;
    private javax.swing.JComboBox<String> subjectList;
    private javax.swing.JButton syncButton;
    private javax.swing.JLabel syncLabel;
    private javax.swing.JLabel syncLabel1;
    private javax.swing.JLabel syncLabel2;
    // End of variables declaration//GEN-END:variables
}
