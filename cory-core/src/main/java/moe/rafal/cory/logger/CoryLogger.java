/*
 *    Copyright 2023-2024 cory
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package moe.rafal.cory.logger;

import static java.lang.String.format;
import static java.util.logging.Level.ALL;
import static java.util.logging.Level.FINER;
import static moe.rafal.cory.logger.payload.PayloadUtils.getPacketPayload;
import static moe.rafal.cory.logger.payload.PayloadUtils.getPacketPreview;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import moe.rafal.cory.Packet;
import moe.rafal.cory.serdes.PacketSerdesContext;

class CoryLogger implements LoggerFacade {

  private static final int FINER_LEVEL_VALUE = FINER.intValue();
  private final PacketSerdesContext serdesContext;
  private final boolean debug;
  private final Logger logger;

  CoryLogger(final PacketSerdesContext serdesContext, final boolean debug) {
    this.serdesContext = serdesContext;
    this.debug = debug;
    this.logger = getConfiguredLogger();
  }

  @Override
  public void log(final Level level, final String message, final Object... parameters) {
    if (isIgnored(level)) {
      return;
    }

    final Object[] parametersWithPayloadPreview = new Object[parameters.length];
    for (int index = 0; index < parametersWithPayloadPreview.length; index++) {
      Object parameter = parameters[index];
      if (parameter instanceof Packet) {
        parameter =
            getPacketPreview(serdesContext, getPacketPayload(serdesContext, (Packet) parameter));
      }
      parametersWithPayloadPreview[index] = parameter;
    }

    logger.log(level, format(message, parametersWithPayloadPreview));
  }

  private Logger getConfiguredLogger() {
    final Logger underlyingLogger = Logger.getLogger(getClass().getName());
    for (final Handler handler : underlyingLogger.getHandlers()) {
      underlyingLogger.removeHandler(handler);
    }

    final Handler handler = new ConsoleHandler();
    handler.setLevel(ALL);

    underlyingLogger.addHandler(handler);
    underlyingLogger.setLevel(ALL);
    return underlyingLogger;
  }

  private boolean isIgnored(final Level level) {
    return !debug && isTrace(level);
  }

  private boolean isTrace(final Level level) {
    return level.intValue() <= FINER_LEVEL_VALUE;
  }
}
