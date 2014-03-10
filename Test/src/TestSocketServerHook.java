import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.events.BasicEventHook;
import cn.hexing.fk.common.spi.IEvent;


public class TestSocketServerHook extends BasicEventHook{

	@Override
	public void handleEvent(IEvent event) {
		if(event.getType()== EventType.MSG_RECV){
			Object s = event.getSource();
		}
	}

	
}
