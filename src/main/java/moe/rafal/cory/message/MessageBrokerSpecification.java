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

import java.time.Duration;

public class MessageBrokerSpecification {

  private static final String NON_SPECIFIED_USERNAME = "";
  private static final String NON_SPECIFIED_PASSWORD = "";
  private final String connectionUri;
  private final String username;
  private final String password;
  private final Duration requestCleanupInterval;

  public MessageBrokerSpecification(String connectionUri, String username, String password,
      Duration requestCleanupInterval) {
    this.connectionUri = connectionUri;
    this.username = username;
    this.password = password;
    this.requestCleanupInterval = requestCleanupInterval;
  }

  public static MessageBrokerSpecification of(String connectionUri) {
    return new MessageBrokerSpecification(connectionUri,
        NON_SPECIFIED_USERNAME, NON_SPECIFIED_PASSWORD, Duration.ofSeconds(5));
  }

  public static MessageBrokerSpecification of(String connectionUri,
      String username, String password, Duration requestCleanupInterval) {
    return new MessageBrokerSpecification(connectionUri, username, password,
        requestCleanupInterval);
  }

  public String getConnectionUri() {
    return connectionUri;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public Duration getRequestCleanupInterval() {
    return requestCleanupInterval;
  }
}
