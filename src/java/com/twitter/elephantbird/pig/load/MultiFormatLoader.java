package com.twitter.elephantbird.pig.load;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.pig.LoadFunc;
import org.apache.thrift.TBase;

import com.google.protobuf.Message;
import com.twitter.elephantbird.mapreduce.input.MultiInputFormat;
import com.twitter.elephantbird.mapreduce.io.BinaryWritable;
import com.twitter.elephantbird.pig.util.PigUtil;
import com.twitter.elephantbird.util.TypeRef;

/**
 * A loader based on {@link MultiInputFormat} to read input written in
 * different file formats.
 *
 * @see MultiInputFormat
 */
public class MultiFormatLoader<M> extends FilterLoadFunc {

  private TypeRef<M> typeRef = null;

  /**
   * @param className Thrift or Protobuf class
   */
  public MultiFormatLoader(String className) {
    super(null);
    Class<?> clazz = PigUtil.getClass(className);
    typeRef = new TypeRef<M>(clazz){};

    /* Initialize loader
     * It does not matter that we are using 'B64Line' though the input
     * can be in a different format. These loaders depend only on the
     * class name and differ only what getInputFormat() returns. since
     * we override getInputFormat(), the difference does not matter.
     *
     * The loader is required to handle rest of the functionality of
     * LoadFunc, LoadMetadata etc.
     */
    LoadFunc ldr;
    if (Message.class.isAssignableFrom(clazz)) {
      ldr = new ProtobufPigLoader<Message>(className);

    } else if (TBase.class.isAssignableFrom(clazz)) {
      ldr = new ThriftPigLoader<TBase<?, ?>>(className);

    } else {
      throw new RuntimeException(className + " is not a Protobuf or Thrift class");
    }

    setLoader(ldr);
  }

  @Override
  public InputFormat<LongWritable, BinaryWritable<M>> getInputFormat() throws IOException {
    return new MultiInputFormat<M>(typeRef);
  }
}
