package com.minecraft.common.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ANSIConstants;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;

/**
 * 控制台日志级别着色：ERROR 红、WARN 黄、INFO 绿，其余级别（DEBUG/TRACE）白色。
 *
 * @author Minecraft System
 */
public class LevelColorConverter extends ForegroundCompositeConverterBase<ILoggingEvent> {

    @Override
    protected String getForegroundColorCode(ILoggingEvent event) {
        int level = event.getLevel().toInt();
        if (level == Level.ERROR_INT) {
            return ANSIConstants.RED_FG;
        }
        if (level == Level.WARN_INT) {
            return ANSIConstants.YELLOW_FG;
        }
        if (level == Level.INFO_INT) {
            return ANSIConstants.GREEN_FG;
        }
        return ANSIConstants.WHITE_FG;
    }
}
