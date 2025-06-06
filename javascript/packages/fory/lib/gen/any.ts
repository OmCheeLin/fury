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

import { TypeInfo } from "../typeInfo";
import { CodecBuilder } from "./builder";
import { BaseSerializerGenerator } from "./serializer";
import { CodegenRegistry } from "./router";
import { InternalSerializerType, RefFlags, Serializer, TypeId } from "../type";
import { Scope } from "./scope";
import Fory from "../fory";

export class AnySerializer {
  constructor(private fory: Fory) {
  }

  readInner() {
    throw new Error("Anonymous serializer can't call directly");
  }

  writeInner() {
    throw new Error("Anonymous serializer can't call directly");
  }

  detectSerializer() {
    const typeId = this.fory.binaryReader.int16();
    let serializer: Serializer | undefined;
    if (TypeId.IS_NAMED_TYPE(typeId)) {
      const ns = this.fory.metaStringResolver.readNamespace(this.fory.binaryReader);
      const typeName = this.fory.metaStringResolver.readTypeName(this.fory.binaryReader);
      serializer = this.fory.classResolver.getSerializerByName(`${ns}$${typeName}`);
    } else {
      serializer = this.fory.classResolver.getSerializerById(typeId);
    }
    if (!serializer) {
      throw new Error(`cant find implements of typeId: ${typeId}`);
    }
    return serializer;
  }

  read() {
    const flag = this.fory.referenceResolver.readRefFlag();
    switch (flag) {
      case RefFlags.RefValueFlag:
        return this.detectSerializer().readInner(true);
      case RefFlags.RefFlag:
        return this.fory.referenceResolver.getReadObject(this.fory.binaryReader.varUInt32());
      case RefFlags.NullFlag:
        return null;
      case RefFlags.NotNullValueFlag:
        return this.detectSerializer().readInner(false);
    }
  }

  write(v: any) {
    if (v === null || v === undefined) {
      this.fory.binaryWriter.reserve(1);
      this.fory.binaryWriter.int8(RefFlags.NullFlag); // null
      return;
    }

    const serializer = this.fory.classResolver.getSerializerByData(v);
    if (!serializer) {
      throw new Error(`Failed to detect the Fory serializer from JavaScript type: ${typeof v}`);
    }
    this.fory.binaryWriter.reserve(serializer.fixedSize);
    serializer.write(v);
  }

  fixedSize = 11;

  needToWriteRef(): boolean {
    throw new Error("//todo unreachable code");
  }

  getTypeId(): number {
    throw new Error("//todo unreachable code");
  }
}

class AnySerializerGenerator extends BaseSerializerGenerator {
  typeInfo: TypeInfo;

  constructor(typeInfo: TypeInfo, builder: CodecBuilder, scope: Scope) {
    super(typeInfo, builder, scope);
    this.typeInfo = typeInfo;
  }

  writeStmt(): string {
    throw new Error("Type Any writeStmt can't inline");
  }

  readStmt(): string {
    throw new Error("Type Any readStmt can't inline");
  }

  toReadEmbed(accessor: (expr: string) => string, excludeHead = false): string {
    if (excludeHead) {
      throw new Error("Anonymous can't excludeHead");
    }
    return accessor(`${this.builder.getForyName()}.anySerializer.read()`);
  }

  toWriteEmbed(accessor: string, excludeHead = false): string {
    if (excludeHead) {
      throw new Error("Anonymous can't excludeHead");
    }
    return `${this.builder.getForyName()}.anySerializer.write(${accessor})`;
  }

  toSerializer() {
    this.scope.assertNameNotDuplicate("read");
    this.scope.assertNameNotDuplicate("readInner");
    this.scope.assertNameNotDuplicate("write");
    this.scope.assertNameNotDuplicate("writeInner");

    const declare = `
      const readInner = (fromRef) => {
        throw new Error("Type Any readInner can't call directly");
      };
      const read = () => {
        ${this.toReadEmbed(expr => `return ${expr}`)}
      };
      const writeInner = (v) => {
        throw new Error("Type Any writeInner can't call directly");
      };
      const write = (v) => {
        ${this.toWriteEmbed("v")}
      };
    `;
    return `
        return function (fory, external, options) {
            ${this.scope.generate()}
            ${declare}
            return {
              read,
              readInner,
              write,
              writeInner,
              fixedSize: ${this.getFixedSize()},
              needToWriteRef() {
                throw new Error("//todo unreachable code");
              }
            };
        }
        `;
  }

  getFixedSize(): number {
    return 11;
  }

  needToWriteRef(): boolean {
    throw new Error("//todo unreachable code");
  }
}

CodegenRegistry.register(InternalSerializerType.ANY, AnySerializerGenerator);
CodegenRegistry.registerExternal(AnySerializer);
