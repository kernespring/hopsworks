/*
 * Copyright (C) 2013 - 2018, Logical Clocks AB and RISE SICS AB. All rights reserved
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS  OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL  THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR  OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package io.hops.hopsworks.api.metadata.wscomm.message;

import io.hops.hopsworks.common.dao.metadata.EntityIntf;
import io.hops.hopsworks.common.dao.metadata.Field;
import io.hops.hopsworks.common.dao.metadata.MTable;
import io.hops.hopsworks.common.dao.metadata.Metadata;
import io.hops.hopsworks.common.dao.metadata.RawData;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Represents a message request for stored metadata filtered by table id and
 * inode id
 */
public class FetchMetadataMessage extends MetadataMessage {

  /**
   * Default constructor. Vital for the class loader
   */
  public FetchMetadataMessage() {
    super();
    this.TYPE = "FetchMetadataMessage";
  }

  /**
   * Used to send custom messages
   *
   * @param sender the message sender
   * @param message the actual message
   */
  public FetchMetadataMessage(String sender, String message) {
    this();
    this.sender = sender;
    this.message = message;
  }

  @Override
  public void init(JsonObject obj) {
    super.init(obj);
  }

  @Override
  public String encode() {
    String value = Json.createObjectBuilder()
            .add("sender", this.sender)
            .add("type", this.TYPE)
            .add("status", this.status)
            .add("message", this.message)
            .build()
            .toString();

    return value;
  }

  @Override
  public void setAction(String action) {
    this.action = action;
  }

  @Override
  public String getAction() {
    return this.action;
  }

  @Override
  public List<EntityIntf> parseSchema() {
    return super.parseSchema();
  }

  @Override
  public String buildSchema(List<EntityIntf> entities) {
    JsonObjectBuilder builder = Json.createObjectBuilder();

    MTable table = (MTable) entities.get(0);
    builder.add("table", table.getName());

    JsonArrayBuilder fields = Json.createArrayBuilder();

    List<Field> f = table.getFields();

    for (Field fi : f) {
      JsonObjectBuilder field = Json.createObjectBuilder();
      field.add("id", fi.getId());
      field.add("name", fi.getName());

      JsonArrayBuilder rd = Json.createArrayBuilder();
      List<RawData> data = fi.getRawData();

      for (RawData raw : data) {
        //JsonObjectBuilder rawdata = Json.createObjectBuilder();
        //rawdata.add("rawid", raw.getId());
        JsonArrayBuilder inner = Json.createArrayBuilder();

        List<Metadata> metadata = raw.getMetadata();
        for (Metadata m : metadata) {
          //load the actual metadata
          rd.add(m.getData());
        }
        //rd.add(inner);
      }
      field.add("data", rd);
      fields.add(field);
    }

    builder.add("fields", fields);

    return builder.build().toString();
  }

  @Override
  public String getMessage() {
    return this.message;
  }

  @Override
  public void setMessage(String msg) {
    this.message = msg;
  }

  @Override
  public String getSender() {
    return this.sender;
  }

  @Override
  public void setSender(String sender) {
    this.sender = sender;
  }

  @Override
  public String getStatus() {
    return this.status;
  }

  @Override
  public void setStatus(String status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return "{\"sender\": \"" + this.sender + "\", "
            + "\"type\": \"" + this.TYPE + "\", "
            + "\"status\": \"" + this.status + "\", "
            + "\"action\": \"" + this.action + "\", "
            + "\"message\": \"" + this.message + "\"}";
  }
}
