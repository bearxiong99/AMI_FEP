package cn.hexing.fas.model;

/**
 * д�����������
 */
public class FaalWriteParamsRequest extends FaalRequest {
    private static final long serialVersionUID = 6624139932292640887L;
    public FaalWriteParamsRequest() {
        super();
        type = FaalRequest.TYPE_WRITE_PARAMS;
    }
}
