package test.reactor;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

/**
 * @author yihua.huang@dianping.com
 */
public class Reactor implements Runnable {

	Selector selector;

	public Reactor() throws IOException {
		selector = Selector.open();
	}

	public void run() {
		while (!Thread.interrupted()) {
			try {
				selector.select();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Set selected = selector.selectedKeys();
			Iterator it = selected.iterator();
			while (it.hasNext())
				dispatch((SelectionKey) (it.next()));
			selected.clear();
		}
	}

	void dispatch(SelectionKey k) {
		Runnable r = (Runnable) (k.attachment());
		if (r != null)
			r.run();
	}
}
