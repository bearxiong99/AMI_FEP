package dcsml.base.applayer;

import java.io.IOException;
import java.util.Date;

import com.hx.dlms.DecodeStream;

import cn.hexing.fk.utils.HexDump;
import dcsml.base.DcsmlOctetString;
import dcsml.base.DcsmlSequence;
import dcsml.base.DcsmlTagAdjunct;
import dcsml.base.DcsmlTime;
import dcsml.base.DcsmlType;
import dcsml.base.DcsmlUnsigned16;
import dcsml.base.DcsmlUnsigned32;
import dcsml.base.ListOfDcsmlServerId;
import dcsml.base.SmlTreePath;

public class DcsmlGetMeterDataRequest extends DcsmlSequence{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7675598241173302633L;

	
	private ListOfDcsmlServerId serverIds = new ListOfDcsmlServerId();
	private DcsmlOctetString subscribeID = new DcsmlOctetString();//OPTIONAL
	private DcsmlUnsigned32 questionId = new DcsmlUnsigned32();
	private DcsmlUnsigned16 ansewerId = new DcsmlUnsigned16();
	private DcsmlTime beginTime =new DcsmlTime(); //OPTIONAL
	private DcsmlTime endTime = new DcsmlTime();//OPTIONAL
	private SmlTreePath parameterTreePath = new SmlTreePath();
	
	public DcsmlGetMeterDataRequest(){
		adjunct = DcsmlTagAdjunct.contextSpecificExplicit(0x10000800);
		members= new DcsmlType[]{this.serverIds,this.subscribeID,this.questionId,this.ansewerId,this.beginTime,this.endTime,this.parameterTreePath};
		memberSize=7;
	}
	
	public DcsmlGetMeterDataRequest(ListOfDcsmlServerId serverIds,SmlTreePath parameterTreePath,DcsmlUnsigned32 questionId,DcsmlUnsigned16 answerId){
		this();
		this.serverIds.assignValue(serverIds);
		this.parameterTreePath.assignValue(parameterTreePath);
		beginTime.setOptional(true);
		subscribeID.setOptional(true);
		endTime.setOptional(true);
		this.questionId.assignValue(questionId);
		this.ansewerId.assignValue(answerId);
	}
	
	public DcsmlGetMeterDataRequest(String[] serverIds,String[] treePaths){
		DcsmlOctetString[] ids = new DcsmlOctetString[serverIds.length];
		DcsmlOctetString[] paths = new DcsmlOctetString[treePaths.length];
		for(int i=0;i<serverIds.length;i++){
			ids[i] = new DcsmlOctetString(serverIds[i].getBytes());
		}
		for(int i=0;i<treePaths.length;i++){
			paths[i] = new DcsmlOctetString(treePaths[i].getBytes());
		}
		
	}
	
	public static void main(String[] args) throws IOException {
		ListOfDcsmlServerId serverIds=new ListOfDcsmlServerId(
				new String[]{
						"HXE00001",
						"HXE00002",
						"HXE00003",
						"HXE00004",
						"HXE00005"});
		SmlTreePath treePath = new SmlTreePath(new DcsmlOctetString[]{
				new DcsmlOctetString("1.8.0".getBytes()),
				new DcsmlOctetString("2.8.0".getBytes()),
				new DcsmlOctetString("5.8.0".getBytes()),
				new DcsmlOctetString("6.8.0".getBytes()),
				new DcsmlOctetString("7.8.0".getBytes())
		});
		DcsmlUnsigned32 questionId=new DcsmlUnsigned32(260650470);
		DcsmlUnsigned16 answerId=new DcsmlUnsigned16(21809);
		DcsmlGetMeterDataRequest dcmd = new DcsmlGetMeterDataRequest(serverIds, treePath, questionId, answerId);
		dcmd.setBeginTime(new Date());
		dcmd.setEndTime(new Date());

		SmlMessage sm = new SmlMessage();
		sm.chooseMessageBody(dcmd);
		System.out.println(HexDump.toHex(sm.encode()));
		sm.decode(new DecodeStream(HexDump.toArray("76053030303062016202726510000800777509485845303030303109485845303030303209485845303030303309485845303030303409485845303030303501650F8935E663553165519ED19265519ED1927506312E382E3006322E382E3006352E382E3006362E382E3006372E382E3063000C00")));
	}

	public DcsmlTime getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(Date date) {
		this.beginTime.setDateTime(date);
	}

	public DcsmlTime getEndTime() {
		return endTime;
	}

	public void setEndTime(Date date) {
		this.endTime.setDateTime(date);
	}
	
}
