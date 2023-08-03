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

package moe.rafal.cory;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import moe.rafal.cory.subject.LoginPacket;
import moe.rafal.cory.subject.LoginPacketListener;
import moe.rafal.cory.subject.LoginRequestPacket;
import moe.rafal.cory.subject.LogoutPacket;
import moe.rafal.cory.subject.MalformedPacket;

public final class PacketTestsUtils {

  public static final Duration MAXIMUM_RESPONSE_PERIOD = Duration.ofSeconds(2);
  public static final String BROADCAST_CHANNEL_NAME = "test-channel";
  public static final byte[] BROADCAST_TEST_PAYLOAD = "Hello world".getBytes(
      StandardCharsets.UTF_8);

  public static final byte[] BROADCAST_REQUEST_TEST_PAYLOAD = "John".getBytes(
      StandardCharsets.UTF_8);
  public static final UUID NIL_UNIQUE_ID = new UUID(0, 0);
  public static final String INITIAL_USERNAME = "jdoe";
  public static final String INITIAL_PASSWORD = "my-secret-password";
  public static final String INCOMING_USERNAME = "kdoe";
  public static final String INCOMING_PASSWORD = "my-little-red-roses";
  public static final String DEFAULT_VALUE = "";
  public static final CompletableFuture<? extends Packet> EMPTY_FUTURE = CompletableFuture.completedFuture(null);

  private PacketTestsUtils() {

  }

  public static LoginPacket getEmptyLoginPacket() {
    return new LoginPacket();
  }

  public static LoginPacket getLoginPacket() {
    return new LoginPacket(INITIAL_USERNAME, INITIAL_PASSWORD);
  }

  public static LoginRequestPacket getLoginRequestPacket() {
    return new LoginRequestPacket(INITIAL_USERNAME, INITIAL_PASSWORD);
  }

  public static LoginPacketListener getLoginPacketListener() {
    return new LoginPacketListener();
  }

  public static LogoutPacket getLogoutPacket() {
    return new LogoutPacket(INITIAL_USERNAME);
  }

  public static MalformedPacket getMalformedPacket() {
    return new MalformedPacket(INITIAL_USERNAME);
  }
}
