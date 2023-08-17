/*
 *    Copyright 2023 cory
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

package moe.rafal.cory.integration.nats;

import static java.lang.String.format;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotatedFields;

import java.lang.reflect.Field;
import java.util.function.Predicate;
import np.com.madanpokharel.embed.nats.EmbeddedNatsConfig;
import np.com.madanpokharel.embed.nats.EmbeddedNatsServer;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.platform.commons.support.ModifierSupport;

public class EmbeddedNatsServerExtension implements
    BeforeAllCallback, AfterAllCallback, TestInstancePostProcessor, ParameterResolver {

  private final EmbeddedNatsServer underlyingServer;

  public EmbeddedNatsServerExtension() {
    this.underlyingServer = new EmbeddedNatsServer(EmbeddedNatsConfig.defaultNatsServerConfig());
  }

  public static String getNatsConnectionUri(EmbeddedNatsServer embeddedNatsServer) {
    return format("nats://%s:%d",
        embeddedNatsServer.getRunningHost(),
        embeddedNatsServer.getRunningPort());
  }

  @Override
  public void beforeAll(ExtensionContext extensionContext) throws Exception {
    underlyingServer.startServer();
  }

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    underlyingServer.stopServer();
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext,
      ExtensionContext extensionContext) throws ParameterResolutionException {
    return parameterContext.isAnnotated(InjectNatsServer.class);
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
    findAnnotatedFields(testClass, InjectNatsServer.class, predicate)
        .forEach(field -> {
          try {
            field.setAccessible(true);
            field.set(testInstance, underlyingServer);
          } catch (Exception ex) {
            throw new RuntimeException(ex);
          }
        });
  }
}
