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

package moe.rafal.cory.concurrent;

import static java.lang.String.format;
import static java.util.logging.Level.SEVERE;

import java.util.logging.Logger;

public final class CompletableFutureUtils {

  private static final String EXCEPTION_CAUGHT_MESSAGE =
      "Caught an exception in future execution: %s";
  private static final Logger LOGGER = Logger.getGlobal();

  private CompletableFutureUtils() {}

  public static <T> T delegateCaughtException(final Throwable cause) {
    LOGGER.log(SEVERE, format(EXCEPTION_CAUGHT_MESSAGE, cause.getMessage()), cause);
    return null;
  }
}
