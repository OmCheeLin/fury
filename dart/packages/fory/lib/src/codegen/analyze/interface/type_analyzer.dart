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

import 'package:fory/src/codegen/analyze/analysis_wrappers.dart';
import 'package:fory/src/codegen/analyze/annotation/location_level_ensure.dart';
import 'package:fory/src/codegen/const/location_level.dart';
import 'package:fory/src/codegen/entity/location_mark.dart';
import 'package:fory/src/codegen/meta/impl/type_spec_gen.dart';

abstract class TypeAnalyzer{

  TypeSpecGen getTypeImmutableAndTag(
    TypeDecision typeDecision,
    @LocationEnsure(LocationLevel.fieldLevel) LocationMark locationMark,
  );
}