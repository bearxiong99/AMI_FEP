package cn.hexing.fas.protocol.conf;

/**
 * 编码/解码器配置
 */
public class CodecConfig {

    /** 功能码 */
    private int funCode;
    /** 编码器实现类 */
    private String encoderClass;
    /** 解码器实现类 */
    private String decoderClass;
    
    /**
     * @return Returns the funCode.
     */
    public int getFunCode() {
        return funCode;
    }
    /**
     * @param funCode The funCode to set.
     */
    public void setFunCode(int funCode) {
        this.funCode = funCode;
    }
    /**
     * @return Returns the encoderClass.
     */
    public String getEncoderClass() {
        return encoderClass;
    }
    /**
     * @param encoderClass The encoderClass to set.
     */
    public void setEncoderClass(String encoderClass) {
        this.encoderClass = encoderClass;
    }
    /**
     * @return Returns the decoderClass.
     */
    public String getDecoderClass() {
        return decoderClass;
    }
    /**
     * @param decoderClass The decoderClass to set.
     */
    public void setDecoderClass(String decoderClass) {
        this.decoderClass = decoderClass;
    }    
}
