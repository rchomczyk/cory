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

package moe.rafal.cory.message;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MessageBrokerSpecificationTests {

  private static final String NON_SPECIFIED_USERNAME = "";
  private static final String NON_SPECIFIED_PASSWORD = "";
  private static final String EXPECTED_CONNECTION_URI = "nats://127.0.0.1:4222";
  private static final String EXPECTED_USERNAME = "shitzuu";
  private static final String EXPECTED_PASSWORD = "my-secret-password-123-!@#";

  private final MessageBrokerSpecification specification = new MessageBrokerSpecification(
      EXPECTED_CONNECTION_URI,
      EXPECTED_USERNAME,
      EXPECTED_PASSWORD);

  @Test
  void getSpecificationWithDefaultsTest() {
    MessageBrokerSpecification specification = MessageBrokerSpecification.withDefaults(
        EXPECTED_CONNECTION_URI);
    assertThat(specification)
        .extracting(
            MessageBrokerSpecification::getConnectionUri,
            MessageBrokerSpecification::getUsername,
            MessageBrokerSpecification::getPassword)
        .containsExactly(
            EXPECTED_CONNECTION_URI,
            NON_SPECIFIED_USERNAME,
            NON_SPECIFIED_PASSWORD);
  }

  @Test
  void getSpecificationWithAuthorizationTest() {
    MessageBrokerSpecification specification = MessageBrokerSpecification.withAuthorization(
        EXPECTED_CONNECTION_URI,
        EXPECTED_USERNAME,
        EXPECTED_PASSWORD);
    assertThat(specification)
        .extracting(
            MessageBrokerSpecification::getConnectionUri,
            MessageBrokerSpecification::getUsername,
            MessageBrokerSpecification::getPassword)
        .containsExactly(
            EXPECTED_CONNECTION_URI,
            EXPECTED_USERNAME,
            EXPECTED_PASSWORD);
  }

  @Test
  void getConnectionUriTest() {
    assertThat(specification.getConnectionUri())
        .isEqualTo(EXPECTED_CONNECTION_URI);
  }

  @Test
  void getUsernameTest() {
    assertThat(specification.getUsername())
        .isEqualTo(EXPECTED_USERNAME);
  }

  @Test
  void getPasswordTest() {
    assertThat(specification.getPassword())
        .isEqualTo(EXPECTED_PASSWORD);
  }
}
