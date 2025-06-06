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

import static org.apache.fory.Fory.NOT_NULL_VALUE_FLAG;
import static org.apache.fory.serializer.AbstractObjectSerializer.GenericTypeField;

import org.apache.fory.Fory;
import org.apache.fory.memory.MemoryBuffer;
import org.apache.fory.resolver.ClassInfo;
import org.apache.fory.resolver.ClassInfoHolder;
import org.apache.fory.resolver.ClassResolver;
import org.apache.fory.resolver.RefResolver;
import org.apache.fory.resolver.XtypeResolver;

// This polymorphic interface has cost, do not expose it as a public class
// If it's used in other packages in fory, duplicate it in those packages.
@SuppressWarnings({"rawtypes", "unchecked"})
// noinspection Duplicates
interface SerializationBinding {
  <T> void writeRef(MemoryBuffer buffer, T obj);

  <T> void writeRef(MemoryBuffer buffer, T obj, Serializer<T> serializer);

  void writeRef(MemoryBuffer buffer, Object obj, ClassInfoHolder classInfoHolder);

  void writeRef(MemoryBuffer buffer, Object obj, ClassInfo classInfo);

  void writeNonRef(MemoryBuffer buffer, Object obj);

  void writeNonRef(MemoryBuffer buffer, Object obj, ClassInfo classInfo);

  void writeNonRef(MemoryBuffer buffer, Object obj, ClassInfoHolder classInfoHolder);

  void writeNullable(MemoryBuffer buffer, Object obj);

  void writeNullable(MemoryBuffer buffer, Object obj, Serializer serializer);

  void writeNullable(MemoryBuffer buffer, Object obj, ClassInfoHolder classInfoHolder);

  void writeNullable(MemoryBuffer buffer, Object obj, ClassInfo classInfo);

  void writeNullable(
      MemoryBuffer buffer, Object obj, ClassInfoHolder classInfoHolder, boolean nullable);

  void writeNullable(MemoryBuffer buffer, Object obj, Serializer serializer, boolean nullable);

  void writeContainerFieldValue(MemoryBuffer buffer, Object fieldValue, ClassInfo classInfo);

  void write(MemoryBuffer buffer, Serializer serializer, Object value);

  Object read(MemoryBuffer buffer, Serializer serializer);

  <T> T readRef(MemoryBuffer buffer, Serializer<T> serializer);

  Object readRef(MemoryBuffer buffer, GenericTypeField field);

  Object readRef(MemoryBuffer buffer, ClassInfoHolder classInfoHolder);

  Object readRef(MemoryBuffer buffer);

  Object readNonRef(MemoryBuffer buffer);

  Object readNonRef(MemoryBuffer buffer, ClassInfoHolder classInfoHolder);

  Object readNonRef(MemoryBuffer buffer, GenericTypeField field);

  Object readNullable(MemoryBuffer buffer, Serializer<Object> serializer);

  Object readNullable(MemoryBuffer buffer, Serializer<Object> serializer, boolean nullable);

  Object readContainerFieldValue(MemoryBuffer buffer, GenericTypeField field);

  Object readContainerFieldValueRef(MemoryBuffer buffer, GenericTypeField fieldInfo);

  int preserveRefId(int refId);

  static SerializationBinding createBinding(Fory fory) {
    if (fory.isCrossLanguage()) {
      return new XlangSerializationBinding(fory);
    } else {
      return new JavaSerializationBinding(fory);
    }
  }

  final class JavaSerializationBinding implements SerializationBinding {
    private final Fory fory;
    private final ClassResolver classResolver;
    private final RefResolver refResolver;

    JavaSerializationBinding(Fory fory) {
      this.fory = fory;
      classResolver = fory.getClassResolver();
      refResolver = fory.getRefResolver();
    }

    @Override
    public <T> void writeRef(MemoryBuffer buffer, T obj) {
      fory.writeRef(buffer, obj);
    }

    @Override
    public <T> void writeRef(MemoryBuffer buffer, T obj, Serializer<T> serializer) {
      fory.writeRef(buffer, obj, serializer);
    }

    @Override
    public void writeRef(MemoryBuffer buffer, Object obj, ClassInfoHolder classInfoHolder) {
      fory.writeRef(buffer, obj, classInfoHolder);
    }

    @Override
    public void writeRef(MemoryBuffer buffer, Object obj, ClassInfo classInfo) {
      fory.writeRef(buffer, obj, classInfo);
    }

    @Override
    public <T> T readRef(MemoryBuffer buffer, Serializer<T> serializer) {
      return fory.readRef(buffer, serializer);
    }

    @Override
    public Object readRef(MemoryBuffer buffer, GenericTypeField field) {
      return fory.readRef(buffer, field.classInfoHolder);
    }

    @Override
    public Object readRef(MemoryBuffer buffer, ClassInfoHolder classInfoHolder) {
      return fory.readRef(buffer, classInfoHolder);
    }

    @Override
    public Object readRef(MemoryBuffer buffer) {
      return fory.readRef(buffer);
    }

    @Override
    public Object readNonRef(MemoryBuffer buffer) {
      return fory.readNonRef(buffer);
    }

    @Override
    public Object readNonRef(MemoryBuffer buffer, ClassInfoHolder classInfoHolder) {
      return fory.readNonRef(buffer, classInfoHolder);
    }

    @Override
    public Object readNonRef(MemoryBuffer buffer, GenericTypeField field) {
      return fory.readNonRef(buffer, field.classInfoHolder);
    }

    @Override
    public Object readNullable(MemoryBuffer buffer, Serializer<Object> serializer) {
      return fory.readNullable(buffer, serializer);
    }

    @Override
    public Object readNullable(
        MemoryBuffer buffer, Serializer<Object> serializer, boolean nullable) {
      if (nullable) {
        return readNullable(buffer, serializer);
      } else {
        return read(buffer, serializer);
      }
    }

    @Override
    public Object readContainerFieldValue(MemoryBuffer buffer, GenericTypeField field) {
      return fory.readNonRef(buffer, field.classInfoHolder);
    }

    @Override
    public Object readContainerFieldValueRef(MemoryBuffer buffer, GenericTypeField fieldInfo) {
      RefResolver refResolver = fory.getRefResolver();
      int nextReadRefId = refResolver.tryPreserveRefId(buffer);
      if (nextReadRefId >= NOT_NULL_VALUE_FLAG) {
        // ref value or not-null value
        Object o = fory.readData(buffer, classResolver.readClassInfo(buffer));
        refResolver.setReadObject(nextReadRefId, o);
        return o;
      } else {
        return refResolver.getReadObject();
      }
    }

    @Override
    public void write(MemoryBuffer buffer, Serializer serializer, Object value) {
      serializer.write(buffer, value);
    }

    @Override
    public Object read(MemoryBuffer buffer, Serializer serializer) {
      return serializer.read(buffer);
    }

    @Override
    public void writeNonRef(MemoryBuffer buffer, Object obj) {
      fory.writeNonRef(buffer, obj);
    }

    @Override
    public void writeNonRef(MemoryBuffer buffer, Object obj, ClassInfo classInfo) {
      fory.writeNonRef(buffer, obj, classInfo);
    }

    @Override
    public void writeNonRef(MemoryBuffer buffer, Object obj, ClassInfoHolder classInfoHolder) {
      fory.writeNonRef(buffer, obj, classResolver.getClassInfo(obj.getClass(), classInfoHolder));
    }

    @Override
    public void writeNullable(MemoryBuffer buffer, Object obj) {
      if (obj == null) {
        buffer.writeByte(Fory.NULL_FLAG);
      } else {
        buffer.writeByte(NOT_NULL_VALUE_FLAG);
        writeNonRef(buffer, obj);
      }
    }

    @Override
    public void writeNullable(MemoryBuffer buffer, Object obj, Serializer serializer) {
      if (obj == null) {
        buffer.writeByte(Fory.NULL_FLAG);
      } else {
        buffer.writeByte(NOT_NULL_VALUE_FLAG);
        serializer.write(buffer, obj);
      }
    }

    @Override
    public void writeNullable(MemoryBuffer buffer, Object obj, ClassInfoHolder classInfoHolder) {
      if (obj == null) {
        buffer.writeByte(Fory.NULL_FLAG);
      } else {
        buffer.writeByte(NOT_NULL_VALUE_FLAG);
        fory.writeNonRef(buffer, obj, classResolver.getClassInfo(obj.getClass(), classInfoHolder));
      }
    }

    @Override
    public void writeNullable(MemoryBuffer buffer, Object obj, ClassInfo classInfo) {
      if (obj == null) {
        buffer.writeByte(Fory.NULL_FLAG);
      } else {
        buffer.writeByte(NOT_NULL_VALUE_FLAG);
        fory.writeNonRef(buffer, obj, classInfo);
      }
    }

    @Override
    public void writeNullable(
        MemoryBuffer buffer, Object obj, ClassInfoHolder classInfoHolder, boolean nullable) {
      if (nullable) {
        writeNullable(buffer, obj, classInfoHolder);
      } else {
        writeNonRef(buffer, obj, classInfoHolder);
      }
    }

    @Override
    public void writeNullable(
        MemoryBuffer buffer, Object obj, Serializer serializer, boolean nullable) {
      if (nullable) {
        writeNullable(buffer, obj, serializer);
      } else {
        write(buffer, serializer, obj);
      }
    }

    @Override
    public void writeContainerFieldValue(
        MemoryBuffer buffer, Object fieldValue, ClassInfo classInfo) {
      fory.writeNonRef(buffer, fieldValue, classInfo);
    }

    @Override
    public int preserveRefId(int refId) {
      return refResolver.preserveRefId(refId);
    }
  }

  final class XlangSerializationBinding implements SerializationBinding {

    private final Fory fory;
    private final XtypeResolver xtypeResolver;
    private final RefResolver refResolver;

    XlangSerializationBinding(Fory fory) {
      this.fory = fory;
      xtypeResolver = fory.getXtypeResolver();
      refResolver = fory.getRefResolver();
    }

    @Override
    public <T> void writeRef(MemoryBuffer buffer, T obj) {
      fory.xwriteRef(buffer, obj);
    }

    @Override
    public <T> void writeRef(MemoryBuffer buffer, T obj, Serializer<T> serializer) {
      fory.xwriteRef(buffer, obj, serializer);
    }

    @Override
    public void writeRef(MemoryBuffer buffer, Object obj, ClassInfoHolder classInfoHolder) {
      fory.xwriteRef(buffer, obj);
    }

    @Override
    public void writeRef(MemoryBuffer buffer, Object obj, ClassInfo classInfo) {
      fory.xwriteRef(buffer, obj);
    }

    @Override
    public <T> T readRef(MemoryBuffer buffer, Serializer<T> serializer) {
      return (T) fory.xreadRef(buffer, serializer);
    }

    @Override
    public Object readRef(MemoryBuffer buffer, GenericTypeField field) {
      if (field.isArray) {
        fory.getGenerics().pushGenericType(field.genericType);
        Object o = fory.xreadRef(buffer);
        fory.getGenerics().popGenericType();
        return o;
      } else {
        return fory.xreadRef(buffer);
      }
    }

    @Override
    public Object readRef(MemoryBuffer buffer, ClassInfoHolder classInfoHolder) {
      return fory.xreadRef(buffer);
    }

    @Override
    public Object readRef(MemoryBuffer buffer) {
      return fory.xreadRef(buffer);
    }

    @Override
    public Object readNonRef(MemoryBuffer buffer) {
      return fory.xreadNonRef(buffer);
    }

    @Override
    public Object readNonRef(MemoryBuffer buffer, ClassInfoHolder classInfoHolder) {
      return fory.xreadNonRef(buffer, xtypeResolver.readClassInfo(buffer, classInfoHolder));
    }

    @Override
    public Object readNonRef(MemoryBuffer buffer, GenericTypeField field) {
      if (field.isArray) {
        fory.getGenerics().pushGenericType(field.genericType);
        Object o = fory.xreadNonRef(buffer);
        fory.getGenerics().popGenericType();
        return o;
      } else {
        return fory.xreadNonRef(buffer);
      }
    }

    @Override
    public Object readNullable(MemoryBuffer buffer, Serializer<Object> serializer) {
      return fory.xreadNullable(buffer, serializer);
    }

    @Override
    public Object readNullable(
        MemoryBuffer buffer, Serializer<Object> serializer, boolean nullable) {
      if (nullable) {
        return readNullable(buffer, serializer);
      } else {
        return read(buffer, serializer);
      }
    }

    @Override
    public Object readContainerFieldValue(MemoryBuffer buffer, GenericTypeField field) {
      return fory.xreadNonRef(buffer, field.containerClassInfo);
    }

    @Override
    public Object readContainerFieldValueRef(MemoryBuffer buffer, GenericTypeField field) {
      int nextReadRefId = refResolver.tryPreserveRefId(buffer);
      if (nextReadRefId >= NOT_NULL_VALUE_FLAG) {
        Object o = fory.xreadNonRef(buffer, field.containerClassInfo);
        refResolver.setReadObject(nextReadRefId, o);
        return o;
      } else {
        return refResolver.getReadObject();
      }
    }

    @Override
    public void write(MemoryBuffer buffer, Serializer serializer, Object value) {
      serializer.xwrite(buffer, value);
    }

    @Override
    public Object read(MemoryBuffer buffer, Serializer serializer) {
      return serializer.xread(buffer);
    }

    @Override
    public void writeNonRef(MemoryBuffer buffer, Object obj) {
      fory.xwriteNonRef(buffer, obj);
    }

    @Override
    public void writeNonRef(MemoryBuffer buffer, Object obj, ClassInfo classInfo) {
      fory.xwriteNonRef(buffer, obj, classInfo);
    }

    @Override
    public void writeNonRef(MemoryBuffer buffer, Object obj, ClassInfoHolder classInfoHolder) {
      fory.xwriteNonRef(buffer, obj, xtypeResolver.getClassInfo(obj.getClass(), classInfoHolder));
    }

    @Override
    public void writeNullable(MemoryBuffer buffer, Object obj) {
      if (obj == null) {
        buffer.writeByte(Fory.NULL_FLAG);
      } else {
        buffer.writeByte(NOT_NULL_VALUE_FLAG);
        fory.xwriteNonRef(buffer, obj);
      }
    }

    @Override
    public void writeNullable(MemoryBuffer buffer, Object obj, Serializer serializer) {
      if (obj == null) {
        buffer.writeByte(Fory.NULL_FLAG);
      } else {
        buffer.writeByte(NOT_NULL_VALUE_FLAG);
        serializer.xwrite(buffer, obj);
      }
    }

    @Override
    public void writeNullable(MemoryBuffer buffer, Object obj, ClassInfoHolder classInfoHolder) {
      if (obj == null) {
        buffer.writeByte(Fory.NULL_FLAG);
      } else {
        buffer.writeByte(NOT_NULL_VALUE_FLAG);
        fory.xwriteNonRef(buffer, obj, xtypeResolver.getClassInfo(obj.getClass(), classInfoHolder));
      }
    }

    @Override
    public void writeNullable(MemoryBuffer buffer, Object obj, ClassInfo classInfo) {
      if (obj == null) {
        buffer.writeByte(Fory.NULL_FLAG);
      } else {
        buffer.writeByte(NOT_NULL_VALUE_FLAG);
        fory.xwriteNonRef(buffer, obj, classInfo);
      }
    }

    @Override
    public void writeNullable(
        MemoryBuffer buffer, Object obj, ClassInfoHolder classInfoHolder, boolean nullable) {
      if (nullable) {
        writeNullable(buffer, obj, classInfoHolder);
      } else {
        writeNonRef(buffer, obj, classInfoHolder);
      }
    }

    @Override
    public void writeNullable(
        MemoryBuffer buffer, Object obj, Serializer serializer, boolean nullable) {
      if (nullable) {
        writeNullable(buffer, obj, serializer);
      } else {
        write(buffer, serializer, obj);
      }
    }

    @Override
    public void writeContainerFieldValue(
        MemoryBuffer buffer, Object fieldValue, ClassInfo classInfo) {
      fory.xwriteData(buffer, classInfo, fieldValue);
    }

    @Override
    public int preserveRefId(int refId) {
      return refResolver.preserveRefId(refId);
    }
  }
}
