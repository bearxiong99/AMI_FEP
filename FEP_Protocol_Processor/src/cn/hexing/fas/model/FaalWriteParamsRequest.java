package cn.hexing.fas.model;

/**
 * 写对象参数请求
 */
public class FaalWriteParamsRequest extends FaalRequest {
    private static final long serialVersionUID = 6624139932292640887L;
    public FaalWriteParamsRequest() {
        super();
        type = FaalRequest.TYPE_WRITE_PARAMS;
    }
}
