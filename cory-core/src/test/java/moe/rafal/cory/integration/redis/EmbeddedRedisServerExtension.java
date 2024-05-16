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

package moe.rafal.cory.integration.redis;

import static java.lang.String.format;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotatedFields;

import com.github.fppt.jedismock.RedisServer;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.function.Predicate;
import moe.rafal.cory.integration.FieldInjectionException;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.platform.commons.support.ModifierSupport;

public class EmbeddedRedisServerExtension implements
    BeforeAllCallback, AfterAllCallback, TestInstancePostProcessor, ParameterResolver {

  private final RedisServer underlyingServer;

  public EmbeddedRedisServerExtension() {
    this.underlyingServer = RedisServer.newRedisServer();
  }

  public static String getRedisConnectionUri(RedisServer embeddedRedisServer) {
    return format("redis://%s:%d",
        embeddedRedisServer.getHost(),
        embeddedRedisServer.getBindPort());
  }

  @Override
  public void beforeAll(ExtensionContext extensionContext) throws Exception {
    underlyingServer.start();
  }

  @Override
  public void afterAll(ExtensionContext extensionContext) throws IOException {
    underlyingServer.stop();
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext,
      ExtensionContext extensionContext) throws ParameterResolutionException {
    return parameterContext.isAnnotated(InjectRedisServer.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext,
      ExtensionContext extensionContext) throws ParameterResolutionException {
    return underlyingServer;
  }

  @Override
  public void postProcessTestInstance(Object testInstance, ExtensionContext extensionContext) {
    injectFields(extensionContext.getRequiredTestClass(), testInstance,
        ModifierSupport::isNotStatic);
  }

  private void injectFields(Class<?> testClass, Object testInstance, Predicate<Field> predicate) {
    findAnnotatedFields(testClass, InjectRedisServer.class, predicate)
        .forEach(field -> {
          try {
            field.setAccessible(true);
            field.set(testInstance, underlyingServer);
          } catch (Exception exception) {
            throw new FieldInjectionException(
                format("Could not inject %s into %s test class.",
                    underlyingServer.getClass().getName(),
                    testClass.getSimpleName()),
                exception);
          }
        });
  }
}
