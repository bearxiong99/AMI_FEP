import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IEventHandler;


public class TestSocketClientHandler implements IEventHandler{

	@Override
	public void handleEvent(IEvent event) {
		if(event.getType()==EventType.MSG_RECV){
			System.out.println("");
		}
	}

}
