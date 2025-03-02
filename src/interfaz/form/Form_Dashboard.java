package interfaz.form;

import Acceso_Datos.emotiv.PreprocesarEmotiv;
import Acceso_Datos.neulog.PreprocesarNeulog;

import brainstorm.BrainstormStart;
import brainstorm.info.BrainstormContext;
import com.mathworks.engine.EngineException;
import java.awt.Color;
import java.awt.Font;
import static java.awt.Frame.ICONIFIED;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import logica.Sincronizacion;
import interfaz.card.ModelCard;
import interfaz.Main1.Main;
import java.io.File;

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
        if (bCon.getProtocol() != null) {
            card1.setData(new ModelCard(null, null, null, bCon.getProtocol().nombreProtocolo(), "Protocol"));
            if (bCon.getSubject() != null) {
                card2.setData(new ModelCard(null, null, null, bCon.getSubject().nombreSujeto(), "Subject"));
            }
        } else {
            card1.setData(new ModelCard(null, null, null, "", "Protocol"));
            card2.setData(new ModelCard(null, null, null, "", "Subject"));
        }
//        card3.setData(new ModelCard(null, null, null, bCon.currentStudyName(), "Study"));
        this.llenar();
        this.llenarSujetos();

        if (bCon.getProtocol() != null) {
            int aux = this.findIndex(this.protocolList, bCon.getProtocol().nombreProtocolo());
            this.protocolList.setSelectedIndex(aux);
        } else {
            this.protocolList.setSelectedIndex(-1);
        }
        if (bCon.getSubject() != null) {
            int aux = this.findIndex(this.subjectList, this.bCon.getSubject().nombreSujeto());
            this.subjectList.setSelectedIndex(aux);
        } else {
            this.subjectList.setSelectedIndex(-1);
        }

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
                            System.out.println("Sync...");
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
                            JOptionPane.showMessageDialog(null, "Synchronization completed");
                            update();
                        }
                    };
                    // Iniciar el trabajo en segundo plano
                    worker.execute();
                } else {
                    // Mostrar mensaje de advertencia si no se cumple la condición
                    JOptionPane.showMessageDialog(
                            null,
                            "Synchronization conditions are not met",
                            "Warning",
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
                            String markerPath = PreprocesarEmotiv.generarArchivoMarcadores(value);
                            if (markerPath != null) {
                                sync.setRutaMarcadoresEmotiv(markerPath);
                                sync.setMarkerName(PreprocesarEmotiv.extractFirstElementFromFile(markerPath));
                                sync.setRutaPowers(powers);
                                System.out.println(sync.getMarkerName());
                                labelEmotiv.setText("File Loaded");
                            } else {
                                JOptionPane.showMessageDialog(null, "No MarkerFile");
                                sync.resetSync();
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "Processing error");
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, "Processing error");
                    }
                } else {
                    JOptionPane.showMessageDialog(
                            null,
                            "The conditions to perform this action are not met.",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        });
        this.neulogFIle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (bCon.getSubject() != null) {
                    if (flag == 2) {
                        try {
                            String value = PreprocesarNeulog.traerArchivo();
                            if (value != null) {
                                JOptionPane.showMessageDialog(null, value);
                                sync.setRutaNeulog(value);
                                sync.setRutaMarcadoresNeulog(PreprocesarNeulog.generarArchivoMarcadores(value, sync.getMarkerName(), sync.rutaMarcadoresEmotiv));
                                labelNeulog.setText("File Loaded");
                                flag += 2;
                            } else {
                                JOptionPane.showMessageDialog(null, "Processing error");
                            }
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(null, "Processing error");
                        }
                    } else {
                        JOptionPane.showMessageDialog(
                                null,
                                "The EMOTIV file must be loaded to proceed.",
                                "Warning",
                                JOptionPane.WARNING_MESSAGE
                        );
                    }
                } else {
                    JOptionPane.showMessageDialog(
                            null,
                            "The conditions to perform this action are not met.",
                            "Warning",
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
                JLabel mensaje = new JLabel(String.format("Do you want to delete a Stage: '%s'", bCon.subject.nombreSujeto()));
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
                JOptionPane.showMessageDialog(null, "No stage selected");
            }
        });
        this.powerBoton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Condición para ejecutar la acción

                if (bCon.study != null) {
                    // Aquí pones el código que quieres ejecutar
                    if (bCon.study.isVideo()) {
                        int respuesta = JOptionPane.showConfirmDialog(null,
                                "An FFT & Power file already exists.\nDo you want to generate other file?",
                                "Confirmation",
                                JOptionPane.YES_NO_OPTION);

                        if (respuesta == JOptionPane.YES_OPTION) {
                            try {
                                // Si elige "Sí", se ejecuta la acción
                                bCon.generarImagenes();
                            } catch (InterruptedException ex) {
                                Logger.getLogger(Form_Dashboard.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (ExecutionException ex) {
                                Logger.getLogger(Form_Dashboard.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            update();
                        } else {
                            // Si elige "No", se cancela la acción
                            JOptionPane.showMessageDialog(null, "Nothing happen.");
                        }
                    } else {
                        try {
                            bCon.generarImagenes();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Form_Dashboard.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ExecutionException ex) {
                            Logger.getLogger(Form_Dashboard.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        update();
                    }

                } else {
                    JOptionPane.showMessageDialog(null, "No study selected",
                            "Warning", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        this.cleanBoton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Condición para ejecutar la acción

                if (bCon.study != null) {
                    // Aquí pones el código que quieres ejecutar
                    int respuesta = JOptionPane.showConfirmDialog(null,
                            "Do you want to open Brainstorm GUI\n to clean the data?",
                            "Confirmation",
                            JOptionPane.YES_NO_OPTION);

                    if (respuesta == JOptionPane.YES_OPTION) {
                        // Si elige "Sí", se ejecuta la acción
                        bCon.openGUI();
                        main.setState(ICONIFIED);
                        main.flagBrainStorm = 1;
                    } else {
                        // Si elige "No", se cancela la acción
                        JOptionPane.showMessageDialog(null, "Nothing happen.");
                    }

                } else {
                    JOptionPane.showMessageDialog(null, "No study selected",
                            "Warning", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

    }

    public void actualizar() {
        card1.setData(new ModelCard(null, null, null, bCon.currentProtocolName(), "Protocol"));
        card2.setData(new ModelCard(null, null, null, bCon.currentSujectName(), "Subject"));
        this.main.updateTitleProtocolo();
        this.actionListenerSujetos();
        this.llenarSujetos();
    }

    public int findIndex(JComboBox<String> combo, String string) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).equals(string)) {
                return i; // Se encontró la palabra, se devuelve su índice
            }
        }
        return -1; // La palabra no se encontró en el JComboBox
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

    public String[] protocolLista() {
        return bCon.protocolList();
    }

    public void llenar() {
        for (String opcion : this.protocolLista()) {
//            this.protocolLista.addItem(opcion);
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
            this.actionListenerSujetos();
        }

    }

    public void update() {
        this.main.showForm(new Form_Dashboard(main));
        this.main.updateTitleProtocolo();
        this.main.updateTitleStudy();
        this.main.updateTitleSujeto();

    }

    public void reset() {
        bCon.resetContext();
        this.main.showForm(new Form_Dashboard(main));
        String[] aux = {""};
        this.main.addMenuItem(aux);
        this.main.updateTitleProtocolo();
        this.main.updateTitleStudy();
        this.main.updateTitleSujeto();

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        card1 = new interfaz.Card();
        card2 = new interfaz.Card();
        roundPanel1 = new interfaz.swing.RoundPanel();
        protocolList = new javax.swing.JComboBox<>();
        subjectList = new javax.swing.JComboBox<>();
        crearProtocolo = new javax.swing.JButton();
        eliminarProtocolo = new javax.swing.JButton();
        newSubject = new javax.swing.JButton();
        deleteSubject = new javax.swing.JButton();
        syncButton = new javax.swing.JButton();
        neulogFIle = new javax.swing.JButton();
        emotivFiles = new javax.swing.JButton();
        labelEmotiv = new javax.swing.JLabel();
        labelNeulog = new javax.swing.JLabel();
        syncLabel2 = new javax.swing.JLabel();
        syncLabel3 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        roundPanel2 = new interfaz.swing.RoundPanel();
        syncLabel = new javax.swing.JLabel();
        cleanBoton = new javax.swing.JButton();
        syncLabel1 = new javax.swing.JLabel();
        powerBoton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();

        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(879, 661));

        card1.setIcon(new ImageIcon("src\\images\\brain.png"));
        card1.setValues("");

        card2.setColor1(new java.awt.Color(95, 211, 226));
        card2.setColor2(new java.awt.Color(26, 166, 170));
        card2.setIcon(new ImageIcon("src\\images\\person.png"));
        card2.setValues("");

        roundPanel1.setBackground(new java.awt.Color(255, 255, 255));
        roundPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        roundPanel1.setPreferredSize(new java.awt.Dimension(819, 451));
        roundPanel1.setRound(10);

        subjectList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subjectListActionPerformed(evt);
            }
        });

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

        newSubject.setText("New Subject");
        newSubject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newSubjectActionPerformed(evt);
            }
        });

        deleteSubject.setText("Delete Subject");
        deleteSubject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteSubjectActionPerformed(evt);
            }
        });

        syncButton.setText("SYNCHRONIZE");
        syncButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                syncButtonActionPerformed(evt);
            }
        });

        neulogFIle.setText(" GSR &  BPM FILES");
        neulogFIle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                neulogFIleActionPerformed(evt);
            }
        });

        emotivFiles.setText("EEG FILE");
        emotivFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                emotivFilesActionPerformed(evt);
            }
        });

        labelEmotiv.setText("No file uploaded");

        labelNeulog.setText("No file uploaded");

        syncLabel2.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        syncLabel2.setText("Synchronization");

        syncLabel3.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        syncLabel3.setText("Context");

        jSeparator3.setMinimumSize(new java.awt.Dimension(50, 10));
        jSeparator3.setPreferredSize(new java.awt.Dimension(50, 10));

        javax.swing.GroupLayout roundPanel1Layout = new javax.swing.GroupLayout(roundPanel1);
        roundPanel1.setLayout(roundPanel1Layout);
        roundPanel1Layout.setHorizontalGroup(
            roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, roundPanel1Layout.createSequentialGroup()
                .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(roundPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(labelNeulog))
                    .addGroup(roundPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, roundPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 56, Short.MAX_VALUE)
                                .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(neulogFIle, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(emotivFiles, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                                .addComponent(labelEmotiv))
                            .addComponent(syncLabel3, javax.swing.GroupLayout.Alignment.LEADING))))
                .addContainerGap(57, Short.MAX_VALUE))
            .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(roundPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(syncLabel2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(roundPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(roundPanel1Layout.createSequentialGroup()
                        .addComponent(crearProtocolo, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(eliminarProtocolo))
                    .addComponent(protocolList, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(subjectList, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, roundPanel1Layout.createSequentialGroup()
                            .addComponent(newSubject, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(deleteSubject, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(syncButton, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        roundPanel1Layout.setVerticalGroup(
            roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundPanel1Layout.createSequentialGroup()
                .addComponent(syncLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, 21, Short.MAX_VALUE)
                .addComponent(protocolList, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, 17, Short.MAX_VALUE)
                .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(crearProtocolo)
                    .addComponent(eliminarProtocolo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addComponent(subjectList, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, 19, Short.MAX_VALUE)
                .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(deleteSubject)
                    .addComponent(newSubject, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
                .addComponent(syncLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
                .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelEmotiv)
                    .addComponent(emotivFiles))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelNeulog)
                    .addComponent(neulogFIle))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                .addComponent(syncButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(25, Short.MAX_VALUE))
        );

        roundPanel2.setBackground(new java.awt.Color(255, 255, 255));
        roundPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        roundPanel2.setMinimumSize(new java.awt.Dimension(0, 0));
        roundPanel2.setPreferredSize(new java.awt.Dimension(819, 451));
        roundPanel2.setRound(10);

        syncLabel.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        syncLabel.setText("Clean ");

        cleanBoton.setText("Clean Data");
        cleanBoton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cleanBotonActionPerformed(evt);
            }
        });

        syncLabel1.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        syncLabel1.setText("FFT & Powers");

        powerBoton.setText("GENERATE FFT & POWERS");
        powerBoton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                powerBotonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout roundPanel2Layout = new javax.swing.GroupLayout(roundPanel2);
        roundPanel2.setLayout(roundPanel2Layout);
        roundPanel2Layout.setHorizontalGroup(
            roundPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, roundPanel2Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(powerBoton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(roundPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(roundPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(syncLabel)
                    .addGroup(roundPanel2Layout.createSequentialGroup()
                        .addGap(0, 113, Short.MAX_VALUE)
                        .addComponent(cleanBoton, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(syncLabel1))
                .addContainerGap(119, Short.MAX_VALUE))
            .addComponent(jSeparator1)
        );
        roundPanel2Layout.setVerticalGroup(
            roundPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(syncLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(cleanBoton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(syncLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(powerBoton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(card1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(roundPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(roundPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE)
                    .addComponent(card2, javax.swing.GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE))
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(roundPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE)
                    .addComponent(roundPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE))
                .addGap(30, 30, 30))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void syncButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_syncButtonActionPerformed

    }//GEN-LAST:event_syncButtonActionPerformed

    private void neulogFIleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_neulogFIleActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_neulogFIleActionPerformed

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
        String nombre = JOptionPane.showInputDialog(null,
                "Enter Protocol Name:",
                "Protocol Name",
                JOptionPane.QUESTION_MESSAGE);

        // Verifica si el usuario no presionó "Cancelar"
        if (nombre != null) {
            // Muestra el nombre ingresado
            nombre = nombre.replaceAll("[^a-zA-Z0-9]", "_");
            bCon.createProtocol(nombre);
            this.update();
        } else {
            // El usuario presionó "Cancelar"
            JOptionPane.showMessageDialog(null,
                    "No protocol was created",
                    "canceled operation",
                    JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_crearProtocoloActionPerformed

    private void newSubjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newSubjectActionPerformed
        // TODO add your handling code here:
        if (bCon.getProtocol() != null) {
            String nombre = JOptionPane.showInputDialog(null,
                    "Enter Subject Name:",
                    "Subject Name",
                    JOptionPane.QUESTION_MESSAGE);

            // Verifica si el usuario no presionó "Cancelar"
            if (nombre != null) {
                // Muestra el nombre ingresado
                nombre = nombre.replaceAll("[^a-zA-Z0-9]", "_");
                double aux = bCon.createSujeto(nombre);
                if (aux <= 0) {
                    JOptionPane.showMessageDialog(null,
                            "Fail to create Subject",
                            "Subject name already use",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    bCon.setSubject((int) aux);
                    this.update();
                }
            } else {
                // El usuario presionó "Cancelar"
                JOptionPane.showMessageDialog(null,
                        "No Subject was created",
                        "canceled operation",
                        JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "You must select a Protocol");
        }

    }//GEN-LAST:event_newSubjectActionPerformed

    private void powerBotonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_powerBotonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_powerBotonActionPerformed

    private void cleanBotonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cleanBotonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cleanBotonActionPerformed

    private void subjectListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subjectListActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_subjectListActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private interfaz.Card card1;
    private interfaz.Card card2;
    private javax.swing.JButton cleanBoton;
    private javax.swing.JButton crearProtocolo;
    private javax.swing.JButton deleteSubject;
    private javax.swing.JButton eliminarProtocolo;
    private javax.swing.JButton emotivFiles;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel labelEmotiv;
    private javax.swing.JLabel labelNeulog;
    private javax.swing.JButton neulogFIle;
    private javax.swing.JButton newSubject;
    private javax.swing.JButton powerBoton;
    private javax.swing.JComboBox<String> protocolList;
    private interfaz.swing.RoundPanel roundPanel1;
    private interfaz.swing.RoundPanel roundPanel2;
    private javax.swing.JComboBox<String> subjectList;
    private javax.swing.JButton syncButton;
    private javax.swing.JLabel syncLabel;
    private javax.swing.JLabel syncLabel1;
    private javax.swing.JLabel syncLabel2;
    private javax.swing.JLabel syncLabel3;
    // End of variables declaration//GEN-END:variables
}
