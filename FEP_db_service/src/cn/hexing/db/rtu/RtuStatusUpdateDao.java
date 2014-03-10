package cn.hexing.db.rtu;

import java.util.Collection;

import cn.hexing.fk.model.ComRtu;

public interface RtuStatusUpdateDao {
	void update(final Collection<ComRtu> rtus);
}
