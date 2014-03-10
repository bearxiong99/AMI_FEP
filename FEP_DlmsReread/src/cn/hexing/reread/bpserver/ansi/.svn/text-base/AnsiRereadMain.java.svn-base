package cn.hexing.reread.bpserver.ansi;

import cn.hexing.reread.bpserver.parent.RereadMainParent;

/**
 * @ClassName:DlmsRereadMain
 * @Description: 补召程序主程序，由Spring负责启动
 * 作用：
 * 	1、为每个任务模板启动一个定时补召任务
 * 	2、持续扫描是否有待完成的主站补召任务
 * @author kexl
 * @date 2012-9-24 上午10:29:35
 *
 */
public class AnsiRereadMain extends RereadMainParent{

	@Override
	protected Class<?> getProcessor() {
		return AnsiRereadProcessor.class;
	}
}
