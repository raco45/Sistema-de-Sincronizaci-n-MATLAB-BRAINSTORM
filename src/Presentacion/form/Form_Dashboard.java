package presentacion.form;

import brainstorm.BrainstormStart;
import brainstorm.info.BrainstormContext;
import com.mathworks.engine.EngineException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import presentacion.card.ModelCard;
import presentacion.main.Main;
import presentacion.menu.Menu;

public class Form_Dashboard extends javax.swing.JPanel {

    private BrainstormContext bCon;
    private Main main;

    public Form_Dashboard(Main main) {
        try {
            bCon = BrainstormStart.getInstance();
        } catch (EngineException ex) {
            Logger.getLogger(Form_Dashboard.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Form_Dashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.main=main;
        initComponents();
        init();
    }

    private void init() {

        card1.setData(new ModelCard(null, null, null, bCon.currentProtocolName(), "Protocolo"));
        card2.setData(new ModelCard(null, null, null, bCon.currentSujectName(), "Sujeto"));
//        card3.setData(new ModelCard(null, null, null, "", "Archivo"));
        this.llenar();
        this.llenarSujetos();
        this.protocolList.setSelectedIndex(-1);
        this.subjectList.setSelectedIndex(-1);
        
        this.protocolList.addActionListener(e -> {
            String seleccion = (String) this.protocolList.getSelectedItem();
            if (seleccion != null) {
                double index = bCon.protocolIndex(seleccion);
                bCon.setProtocol(index);
                this.actualizar();
            }
        });

    }

    public String cambio() {
        return "Prueba";
    }

    public void actualizar() {
        card1.setData(new ModelCard(null, null, null, bCon.currentProtocolName(), "Protocolo"));
        card2.setData(new ModelCard(null, null, null, bCon.currentSujectName(), "Sujeto"));
        this.main.updateTitleProtocolo();
        this.actionListenerSujetos();
        this.llenarSujetos();
    }
    
    public void actionListenerSujetos(){
        
        this.subjectList.addActionListener(e -> {
        String seleccion = (String) this.subjectList.getSelectedItem();
        String[] listaSujetos = bCon.protocolSubjects();
        if(seleccion != null){
            for(int i=1 ; i <= listaSujetos.length ; i++ ){
                if(seleccion.equals(listaSujetos[i-1])){
                    int iSubject= i;
                    bCon.setSubject(iSubject);
                    this.actualizarSujetos(listaSujetos[i-1]);
                    bCon.subjectStudies(listaSujetos[i-1]);
                    String[] prueba=bCon.subjectStudiesArray(listaSujetos[i-1]);
                    this.main.addMenuItem(prueba);
                }
            }   
        }
        });
    }
    public void actualizarSujetos(String sujeto){
        this.main.updateTitleSujeto();
        card2.setData(new ModelCard(null, null, null, sujeto, "Sujeto"));
    }

    public String[] protocolList() {
        return bCon.protocolList();
    }

    public void llenar() {
        for (String opcion : this.protocolList()) {
//            this.protocolList.addItem(opcion);
            if(bCon.protocolIndex(opcion)==0){
                System.out.println("Vacio");
            }else{
                this.protocolList.addItem(opcion);                
            }
        }
    }

    public void llenarSujetos() {
        this.subjectList.removeAllItems();
        String[] sujetos = bCon.protocolSubjects();
        if (sujetos[0] == "") {
            System.out.println("No hay sujetos");
        } else {
            for (String opcion : sujetos) {
                this.subjectList.addItem(opcion);
            }
        }
        bCon.protocolStudies();

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        card1 = new Presentacion.Card();
        card2 = new Presentacion.Card();
        card3 = new Presentacion.Card();
        roundPanel1 = new presentacion.swing.RoundPanel();
        protocolList = new javax.swing.JComboBox<>();
        subjectList = new javax.swing.JComboBox<>();

        setOpaque(false);

        card2.setColor1(new java.awt.Color(95, 211, 226));
        card2.setColor2(new java.awt.Color(26, 166, 170));
        card2.setIcon(javaswingdev.GoogleMaterialDesignIcon.PIE_CHART);

        card3.setColor1(new java.awt.Color(95, 243, 140));
        card3.setColor2(new java.awt.Color(3, 157, 27));
        card3.setIcon(javaswingdev.GoogleMaterialDesignIcon.RING_VOLUME);

        roundPanel1.setBackground(new java.awt.Color(255, 255, 255));
        roundPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        roundPanel1.setRound(10);

        protocolList.setModel(protocolList.getModel());
        protocolList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                protocolListActionPerformed(evt);
            }
        });

        subjectList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subjectListActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout roundPanel1Layout = new javax.swing.GroupLayout(roundPanel1);
        roundPanel1.setLayout(roundPanel1Layout);
        roundPanel1Layout.setHorizontalGroup(
            roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundPanel1Layout.createSequentialGroup()
                .addGap(112, 112, 112)
                .addComponent(protocolList, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 86, Short.MAX_VALUE)
                .addComponent(subjectList, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(111, 111, 111))
        );
        roundPanel1Layout.setVerticalGroup(
            roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundPanel1Layout.createSequentialGroup()
                .addGap(71, 71, 71)
                .addGroup(roundPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(protocolList, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(subjectList, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(339, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(roundPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(card1, javax.swing.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE)
                        .addGap(30, 30, 30)
                        .addComponent(card2, javax.swing.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE)
                        .addGap(30, 30, 30)
                        .addComponent(card3, javax.swing.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE)))
                .addGap(30, 30, 30))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(card3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(card2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(card1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addComponent(roundPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(30, 30, 30))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void subjectListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subjectListActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_subjectListActionPerformed

    private void protocolListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_protocolListActionPerformed
        // TODO add your handling code here:


    }//GEN-LAST:event_protocolListActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private Presentacion.Card card1;
    private Presentacion.Card card2;
    private Presentacion.Card card3;
    private javax.swing.JComboBox<String> protocolList;
    private presentacion.swing.RoundPanel roundPanel1;
    private javax.swing.JComboBox<String> subjectList;
    // End of variables declaration//GEN-END:variables
}
