package cn.hexing.fas.protocol.zj.codec;

import cn.hexing.fas.protocol.data.DataMappingZJ;
import cn.hexing.fas.protocol.zj.parse.ParseTool;
import cn.hexing.fas.protocol.zj.parse.Parser03;
import cn.hexing.fk.message.IMessage;

/**
 * 
 * @author gaoll
 *
 * @time 2013-8-19 上午09:29:18
 *
 * @info 软件升级帧解析
 */
public class C30MessageDecoder extends AbstractMessageDecoder {

	@Override
	public Object decode(IMessage message) {
		if(ParseTool.getOrientation(message)==DataMappingZJ.ORIENTATION_TO_APP){
			
			int rtype=(ParseTool.getErrCode(message));
			
			if(DataMappingZJ.ERROR_CODE_OK==rtype){		//终端正常应答
    			byte[] data=ParseTool.getData(message);
    			if(data.length>4){
    				int loc = 0;
    				int fileNameLength=data[0];
    				loc +=1;
    				byte[] fileName = new byte[fileNameLength];
    				System.arraycopy(data, loc, fileName, 0, fileNameLength);
    				loc +=fileNameLength;
    				loc+=1;
    				String strFileName = new String(fileName);
    				Object contentNum=Parser03.parsevalue(data, loc, 2, 0);
    				StringBuilder sb = new StringBuilder();
    				sb.append(strFileName).append("#").append(contentNum);
    				return sb.toString();
    			}
			}else{
				return "FAIL";
			}
			
			
		}
		return null;
	}

}
