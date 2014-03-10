package cn.hexing.reread.bpserver.gg;

import cn.hexing.reread.bpserver.parent.RereadMainParent;
/**
 * 
 * @author gaoll
 *
 * @time 2012-11-3 下午1:44:56
 *
 * @info 广规补招任务主程序
 */
public class GgRereadMain  extends RereadMainParent{
	@Override
	protected Class<?> getProcessor() {
		return GgRereadProcessor.class;
	}
}
