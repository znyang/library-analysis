package com.zen.plugin.lib.analysis.log;

/**
 * @author zen
 * @version 2016/6/28
 */
public class Logger implements ILog {

    private static final Logger sInstance = new Logger();

    private final StringBuffer mBuffer = new StringBuffer();

    public static Logger getInstance() {
        return sInstance;
    }

    @Override
    public void d(String msg, Object... args) {
        if (args == null || args.length == 0) {
            mBuffer.append(msg);
        } else {
            mBuffer.append(String.format(msg, args));
        }
        mBuffer.append('\n');
    }

    @Override
    public String getLog() {
        return mBuffer.toString();
    }
}
