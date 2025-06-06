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

import 'package:fory/src/config/fory_config.dart';
import 'package:fory/src/const/obj_type.dart';
import 'package:fory/src/deserializer_pack.dart';
import 'package:fory/src/exception/deserialization_exception.dart';
import 'package:fory/src/memory/byte_reader.dart';
import 'package:fory/src/memory/byte_writer.dart';
import 'package:fory/src/meta/specs/enum_spec.dart';
import 'package:fory/src/serializer/custom_serializer.dart';
import 'package:fory/src/serializer/serializer_cache.dart';
import 'package:fory/src/serializer_pack.dart';

final class _EnumSerializerCache extends SerializerCache{

  static final Map<Type, EnumSerializer> _cache = {};

  const _EnumSerializerCache();

  @override
  EnumSerializer getSerializerWithSpec(ForyConfig conf, covariant EnumSpec spec, Type dartType){
    EnumSerializer? ser = _cache[dartType];
    if (ser != null) {
      return ser;
    }
    // In foryJava, EnumSer does not perform reference tracking
    ser = EnumSerializer(false, spec.values);
    _cache[dartType] = ser;
    return ser;
  }
}

final class EnumSerializer extends CustomSerializer<Enum>{

  static const SerializerCache cache = _EnumSerializerCache();

  final List<Enum> values;
  EnumSerializer(bool writeRef, this.values): super(ObjType.NAMED_ENUM, writeRef);

  @override
  Enum read(ByteReader br, int refId, DeserializerPack pack) {
    int index = br.readVarUint32Small7();
    // foryJava supports deserializeNonexistentEnumValueAsNull,
    // but here in Dart, it will definitely throw an error if the index is out of range
    if (index < 0 || index >= values.length) {
      throw DeserializationRangeException(index, values);
    }
    return values[index];
  }

  @override
  void write(ByteWriter bw, Enum v, SerializerPack pack) {
    bw.writeVarUint32Small7(v.index);
  }
}
