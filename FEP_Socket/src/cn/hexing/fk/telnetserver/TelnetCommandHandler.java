/**
 * Called by TelnetCommandDispatcher
 */
package cn.hexing.fk.telnetserver;

/**
 *
 */
public interface TelnetCommandHandler {
	String getCommand();
	void setCommand(String cmd);
	void setServer(TelnetServer server);
	void execute(TelnetSession session, String[] args) throws Exception;
}
