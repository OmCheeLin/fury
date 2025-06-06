/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.fory.serializer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.apache.fory.Fory;
import org.apache.fory.memory.ByteBufferUtil;
import org.apache.fory.memory.MemoryBuffer;

/** Serializers for buffer related classes. */
public class BufferSerializers {
  /**
   * Note that this serializer only serialize data, but not the byte buffer meta. Since ByteBuffer
   * doesn't implement {@link java.io.Serializable}, it's ok to only serialize data. Also Note that
   * a direct buffer may be returned if the serialized buffer is a heap buffer.
   */
  public static final class ByteBufferSerializer
      extends Serializers.CrossLanguageCompatibleSerializer<ByteBuffer> {

    public ByteBufferSerializer(Fory fory, Class<ByteBuffer> cls) {
      super(fory, cls);
    }

    @Override
    public void write(MemoryBuffer buffer, ByteBuffer value) {
      fory.writeBufferObject(buffer, new BufferObject.ByteBufferBufferObject(value));
    }

    @Override
    public ByteBuffer copy(ByteBuffer value) {
      ByteBuffer dst = ByteBuffer.allocate(value.remaining());
      dst.put(value.duplicate());
      ByteBufferUtil.rewind(dst);
      return dst;
    }

    @Override
    public ByteBuffer read(MemoryBuffer buffer) {
      MemoryBuffer newBuffer = fory.readBufferObject(buffer);
      int readerIndex = newBuffer.readerIndex();
      int size = newBuffer.remaining();
      ByteBuffer originalBuffer = newBuffer.sliceAsByteBuffer(readerIndex, size - 1);
      byte isBigEndian = newBuffer.getByte(readerIndex + size - 1);
      originalBuffer.order(
          isBigEndian == (byte) 1 ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
      return originalBuffer;
    }
  }

  // TODO(chaokunyang) add support for MemoryBuffer serialization.
}
