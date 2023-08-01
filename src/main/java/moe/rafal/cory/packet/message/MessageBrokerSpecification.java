package moe.rafal.cory.packet.message;

class MessageBrokerSpecification {

  private final String connectionUri;
  private final String username;
  private final String password;

  MessageBrokerSpecification(String connectionUri, String username, String password) {
    this.connectionUri = connectionUri;
    this.username = username;
    this.password = password;
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
}
