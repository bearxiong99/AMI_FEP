/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testswing;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.nio.ByteBuffer;

import javax.swing.JOptionPane;

import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.ASN1OctetString;
import com.hx.dlms.DecodeStream;
import com.hx.dlms.TagAdjunct;
import com.hx.dlms.aa.AareApdu;

/**
 *
 * @author Administrator
 */
public class Decriptor extends javax.swing.JFrame {

    public enum METER_MODE{NORMAL,BENGAL};
    
    public enum MSG_MODE{UP,DOWN};
    
   
    /**
     * 表计类型
     */
    public METER_MODE meterMode = METER_MODE.NORMAL;
    public MSG_MODE msgMode = MSG_MODE.UP;
    /**
     * Creates new form TestSwing
     */
    public Decriptor() {
        initComponents();
    }
    
    private static byte[] masterSystitle=new byte[]{ 0x48, 0x58, 0x45, 0x11, 0, 0, 0, 0 };
    private static byte[] defaultKey = new byte[16];
    
    
    private byte[] systitle =masterSystitle;
    
    private byte[] encryptKey = defaultKey;
    private byte[] authKey = defaultKey;

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

    	
        l_meterType = new javax.swing.JLabel();
        c_meterType = new javax.swing.JComboBox();
        t_aare = new javax.swing.JTextField();
        l_aare = new javax.swing.JLabel();
        b_createSystitle = new javax.swing.JButton();
        l_msgType = new javax.swing.JLabel();
        c_msgType = new javax.swing.JComboBox();
        l_systitle = new javax.swing.JLabel();
        t_systitle = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        t_encryptKey = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        t_authKey = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        t_iv = new javax.swing.JTextField();
        t_iv.setEnabled(false);
        b_encrypt = new javax.swing.JButton();
        t_encryText = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        t_plainText = new javax.swing.JTextArea();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        
        l_meterType.setText("表计类型:");

        c_meterType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Normal", "Bengal" }));
        c_meterType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                c_meterTypeActionPerformed(evt);
            }
        });

        t_aare.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                t_aareActionPerformed(evt);
            }
        });

        l_aare.setText("AARE:");

        b_createSystitle.setText("生成Systitle");
        b_createSystitle.setToolTipText("使用AARE生成systitle ,用于解密下行数据");
        b_createSystitle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_createSystitleActionPerformed(evt);
            }
        });

        l_msgType.setText("消息类别:");

        c_msgType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "上行", "下行" }));
        c_msgType.setToolTipText("上行消息有默认systitle");
        c_msgType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                c_msgTypeActionPerformed(evt);
            }
        });

        l_systitle.setText("Systitle:");

        jLabel1.setText("EncryptKey:");

        t_encryptKey.setText(HexDump.toHex(encryptKey));
        t_encryptKey.setToolTipText("默认为6个零");
        t_encryptKey.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                t_encryptKeyActionPerformed(evt);
            }
        });

        jLabel2.setText("AuthKey:");

        t_authKey.setText(HexDump.toHex(authKey));
        t_authKey.setToolTipText("默认为6个零");
        t_authKey.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                t_authKeyActionPerformed(evt);
            }
        });

        jLabel3.setText("IV:");

        b_encrypt.setText("解密:");
        b_encrypt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_encryptActionPerformed(evt);
            }
        });

        t_plainText.setColumns(20);
        t_plainText.setRows(5);
        jScrollPane1.setViewportView(t_plainText);

        jLabel4.setText("解密结果:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addComponent(jLabel1))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(b_encrypt)
                                    .addComponent(jLabel4))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(t_authKey, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                            .addComponent(t_encryptKey)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(l_systitle)
                                    .addComponent(l_aare)
                                    .addComponent(l_meterType, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(c_meterType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(40, 40, 40)
                                        .addComponent(l_msgType, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(c_msgType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(t_aare, javax.swing.GroupLayout.PREFERRED_SIZE, 360, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(b_createSystitle))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(t_systitle, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(41, 41, 41)
                                        .addComponent(jLabel3)
                                        .addGap(18, 18, 18)
                                        .addComponent(t_iv, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)))))))
                .addContainerGap(62, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(103, 103, 103)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(t_encryText))
                .addGap(140, 140, 140))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(l_msgType, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(c_msgType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(l_meterType, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(t_aare, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(l_aare)
                            .addComponent(b_createSystitle)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(c_meterType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(l_systitle)
                    .addComponent(t_systitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(t_iv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(t_encryptKey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(t_authKey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(b_encrypt)
                    .addComponent(t_encryText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(52, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void c_meterTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_c_meterTypeActionPerformed
        // TODO add your handling code here:
        // index 0 = Normal
        // index 1 = Bengal
        int index=c_meterType.getSelectedIndex();
        meterMode = index==0?METER_MODE.NORMAL:METER_MODE.BENGAL;
        if(meterMode == METER_MODE.BENGAL ){
            t_authKey.setEnabled(false);
            t_iv.setEnabled(true);
            authKey = null;
            t_authKey.setText(null);
        }else{
            authKey = defaultKey;
            t_authKey.setEnabled(true);
            t_iv.setEnabled(false);
            t_authKey.setText(HexDump.toHex(authKey));
        }
    }//GEN-LAST:event_c_meterTypeActionPerformed

    private void t_aareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_t_aareActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_t_aareActionPerformed

    private void b_createSystitleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b_createSystitleActionPerformed
        //生成systtile按钮
        //1.check aare is not null
         String strAare=t_aare.getText();
        try{
            if(checkAAREIslegal(strAare)){
                AareApdu aare = new AareApdu();
                aare.decode(DecodeStream.wrap(strAare));
                byte[] apTitle=aare.getRespApTitle();
                if(apTitle!=null){
                    systitle = apTitle;
                    t_systitle.setText(HexDump.toHex(systitle));
                }else{
                	showMsg("解析出来的systitle为空");
                }
            }else{
            	showMsg("不是正确的AARE");
            }
        }catch(Exception e){
            //exception
        	showMsg("生成systitle出错");
        }
    }//GEN-LAST:event_b_createSystitleActionPerformed

    public boolean checkAAREIslegal(String aare){
        if(!checkStringIsNotNull(aare)) return false;
        
        if("61".equals(aare.substring(0, 2))) return true;
        
        return false;
    
    }
    
    public boolean checkStringIsNotNull(String value){
        if(value==null || "".equals(value.trim())) return false;
        return true;
    }
    
    
    private void c_msgTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_c_msgTypeActionPerformed
        int index= c_msgType.getSelectedIndex();
        //index  0 = up
        //index  1 = down
        msgMode=index==0?MSG_MODE.UP:MSG_MODE.DOWN;
        
        if(msgMode==MSG_MODE.DOWN){
            systitle = masterSystitle;
            t_systitle.setText( HexDump.toHex(systitle));
        }else{
            t_systitle.setText(null);
        }
    }//GEN-LAST:event_c_msgTypeActionPerformed

    private void t_encryptKeyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_t_encryptKeyActionPerformed
    }//GEN-LAST:event_t_encryptKeyActionPerformed

    private void t_authKeyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_t_authKeyActionPerformed
    }//GEN-LAST:event_t_authKeyActionPerformed

    private void b_encryptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b_encryptActionPerformed
        // TODO add your handling code here:
        String encryText=t_encryText.getText();
        if(!checkStringIsNotNull(encryText)){
        	showMsg("要解密的帧为空!");
        	return ;
        }
        
        byte[] ciphered = HexDump.toArray(encryText);
        byte[] plain = null;
        
        try {
			if(meterMode==METER_MODE.BENGAL){
				String strIv=t_iv.getText();
				if(checkStringIsNotNull(strIv) ){
					plain=Gcm128SoftCipher.getInstance().decrypt(encryptKey, authKey, ciphered, HexDump.toArray(strIv));
				}else{
					showMsg("iv 不能为空");
				}
			}else{
				if(systitle!=null){
					ASN1OctetString octs = new ASN1OctetString();
					ByteBuffer apdu = ByteBuffer.wrap(ciphered);
					TagAdjunct myAdjunct = TagAdjunct.contextSpecificImplicit(0xFF & apdu.get(0));
					octs.forceEncodeTag(true);
					myAdjunct.axdrCodec(true);
					octs.setTagAdjunct(myAdjunct);
					octs.decode(DecodeStream.wrap(apdu));
					byte[] val = octs.getValue();	// SH + C + T:  means security ctr + FC + cipher text + auth tag
					if( val[0] == 0x30 ){
						byte[] cipherText = new byte[val.length-5];
						byte[] iv=makeInitVector(systitle, val, 1);
						for(int i=0; i<cipherText.length; i++ )
							cipherText[i] = val[i+5];
						plain=Gcm128SoftCipher.getInstance().decrypt(encryptKey, authKey, cipherText, iv);
					}
				}else{
					showMsg("systitle 不能为空!");
				}
			}
			if(plain !=null){
				t_plainText.setText(HexDump.toHex(plain));				
			}else{
				showMsg("不能解密消息!");
			}
		} catch (Exception e) {
			showMsg("不能解密消息!");
		}
        
        
    }//GEN-LAST:event_b_encryptActionPerformed

    public void showMsg(String msg){
    	JOptionPane.showMessageDialog(this, msg);
    }
    
    
	public final byte[] makeInitVector( byte[] meterSystitle,byte[]cipherText, int offset ){
		ByteBuffer iv = ByteBuffer.allocate(12);
		iv.put(meterSystitle);
		iv.put(cipherText, offset, 4);
		return iv.array();
	}
    
    
    /**
     * @param args the command line arguments
     */
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
            java.util.logging.Logger.getLogger(Decriptor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Decriptor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Decriptor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Decriptor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        final Decriptor decriptor =new Decriptor();

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
            	decriptor.setVisible(true);
            	Toolkit kit = Toolkit.getDefaultToolkit();    // 定义工具包
                Dimension screenSize = kit.getScreenSize();   // 获取屏幕的尺寸
                int screenWidth = screenSize.width/2;         // 获取屏幕的宽
                int screenHeight = screenSize.height/2;       // 获取屏幕的高
                int height = decriptor.getHeight();
                int width = decriptor.getWidth();
                decriptor.setLocation(screenWidth-width/2, screenHeight-height/2);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton b_createSystitle;
    private javax.swing.JButton b_encrypt;
    private javax.swing.JComboBox c_meterType;
    private javax.swing.JComboBox c_msgType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel l_aare;
    private javax.swing.JLabel l_meterType;
    private javax.swing.JLabel l_msgType;
    private javax.swing.JLabel l_systitle;
    private javax.swing.JTextField t_aare;
    private javax.swing.JTextField t_authKey;
    private javax.swing.JTextField t_encryText;
    private javax.swing.JTextField t_encryptKey;
    private javax.swing.JTextField t_iv;
    private javax.swing.JTextArea t_plainText;
    private javax.swing.JTextField t_systitle;
    // End of variables declaration//GEN-END:variables
}
