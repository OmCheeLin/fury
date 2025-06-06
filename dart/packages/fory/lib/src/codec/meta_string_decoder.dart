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

import 'dart:typed_data';

import 'package:fory/src/codec/meta_string_codecs.dart';
import 'package:fory/src/codec/meta_string_encoding.dart';
import 'package:fory/src/meta/meta_string_byte.dart';

abstract class MetaStringDecoder extends MetaStringCodecs {
  const MetaStringDecoder(super.specialChar1, super.specialChar2);

  String decode(Uint8List data, MetaStrEncoding encoding);
  String decodeMetaString(MetaStringBytes data);
}