
package org.apache.beam.sdk.io.kafka;

public class LongStringKafkaIO {
  public static KafkaIO.Read<Long, String> read() {
    return KafkaIO.<Long, String>read();
  }
}
