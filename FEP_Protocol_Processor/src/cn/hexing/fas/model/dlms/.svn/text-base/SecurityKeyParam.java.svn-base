/**
 * Key change operation parameters
 */
package cn.hexing.fas.model.dlms;

import java.io.Serializable;

/**
 * @author Bao Hongwei
 *
 */
public class SecurityKeyParam implements Serializable {
	private static final long serialVersionUID = -6512483673733745683L;
	public enum KEY_CHANGE_TYPE{ SOFT_EKAK, SOFT_EK, SOFT_AK, ESAM_GCM_KEYS,ESAM_KEYPAIR,ESAM_KEY_MS};
	
	private KEY_CHANGE_TYPE keyType = KEY_CHANGE_TYPE.SOFT_EKAK;
	private byte[] rootKey = null;
	private byte[] newEncKey = null;	//new encryption key, 16 bytes length.
	private byte[] newAuthKey = null;   //Used by AES-GCM mode.
	private byte[] cipheredEncKey = null;
	private byte[] cipheredAuthKey = null;
	
	public final void changeESAMGcm(){
		keyType = KEY_CHANGE_TYPE.ESAM_GCM_KEYS;
	}
	
	public final void changeESAMKeyPair(){
		keyType = KEY_CHANGE_TYPE.ESAM_KEYPAIR;
	}
	
	public final void changeESAMMsPair(){
		keyType = KEY_CHANGE_TYPE.ESAM_KEY_MS;
	}
	
	public final KEY_CHANGE_TYPE getKeyChangeType(){
		return keyType;
	}
	
	public final byte[] getNewEncKey() {
		return newEncKey;
	}
	public final void setNewEncKey(byte[] newEncKey) {
		this.newEncKey = newEncKey;
	}
	public final byte[] getNewAuthKey() {
		return newAuthKey;
	}
	public final void setNewAuthKey(byte[] newAuthKey) {
		this.newAuthKey = newAuthKey;
	}
	public final byte[] getCipheredEncKey() {
		return cipheredEncKey;
	}
	public final void setCipheredEncKey(byte[] cipheredEncKey) {
		this.cipheredEncKey = cipheredEncKey;
	}
	public final byte[] getCipheredAuthKey() {
		return cipheredAuthKey;
	}
	public final void setCipheredAuthKey(byte[] cipheredAuthKey) {
		this.cipheredAuthKey = cipheredAuthKey;
	}
	public final byte[] getRootKey() {
		return rootKey;
	}
	public final void setRootKey(byte[] rootKey) {
		this.rootKey = rootKey;
	}
}
