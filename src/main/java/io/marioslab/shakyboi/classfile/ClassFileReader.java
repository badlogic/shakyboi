package io.marioslab.shakyboi.classfile;

import io.marioslab.shakyboi.util.ByteArrayInput;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Reads a <code>.class</code> file given as a byte array and returns a {@link ClassFile} instance.
 * <p>
 * See {@link #readClassFile(String, byte[])}
 */
public class ClassFileReader {
    public static ClassFile readClassFile(String name, byte[] data) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInput(data));
        try {
            ClassFile clz = new ClassFile(name, data);

            if (in.readInt() != 0xcafebabe) throw new RuntimeException("Magic 0xcafebabe not found");

            clz.minorVersion = in.readUnsignedShort();
            clz.majorVersion = in.readUnsignedShort();

            readConstantPool(in, clz);

            clz.accessFlags = in.readUnsignedShort();
            clz.thisClass = in.readUnsignedShort();
            clz.superClass = in.readUnsignedShort();

            readInterfaces(in, clz);
            readMembers(in, clz, clz.fields);
            readMembers(in, clz, clz.methods);
            readAttributes(in, clz, clz.attributes);

            return clz;
        } catch (Throwable t) {
            throw new IOException("Error reading class " + name, t);
        }
    }

    static void readConstantPool(DataInputStream in, ClassFile clz) throws IOException {
        int constantPoolCount = in.readUnsignedShort();
        clz.constantPool.add(null);
        for (int i = 1; i <= constantPoolCount - 1; i++) {
            int tag = in.readUnsignedByte();
            ClassFile.ConstantPoolEntry entry = null;

            switch (tag) {
                case ClassFile.CONSTANT_UTF8:
                    int len = in.readUnsignedShort();
                    byte[] bytes = new byte[len];
                    for (int j = 0; j < len; j++)
                        bytes[j] = in.readByte();
                    entry = new ClassFile.Utf8Entry(clz, bytes);
                    break;

                case ClassFile.CONSTANT_INTEGER:
                    entry = new ClassFile.IntEntry(clz, in.readInt());
                    break;

                case ClassFile.CONSTANT_FLOAT:
                    entry = new ClassFile.FloatEntry(clz, in.readFloat());
                    break;

                case ClassFile.CONSTANT_LONG:
                    entry = new ClassFile.LongEntry(clz, in.readInt(), in.readInt());
                    break;

                case ClassFile.CONSTANT_DOUBLE:
                    entry = new ClassFile.DoubleEntry(clz, in.readInt(), in.readInt());
                    break;

                case ClassFile.CONSTANT_PACKAGE:
                    entry = new ClassFile.PackageEntry(clz, in.readUnsignedShort());
                    break;

                case ClassFile.CONSTANT_MODULE:
                    entry = new ClassFile.ModuleEntry(clz, in.readUnsignedShort());
                    break;

                case ClassFile.CONSTANT_CLASS:
                    entry = new ClassFile.ClassInfoEntry(clz, in.readUnsignedShort());
                    break;

                case ClassFile.CONSTANT_STRING:
                    entry = new ClassFile.StringEntry(clz, in.readUnsignedShort());
                    break;

                case ClassFile.CONSTANT_FIELDREF:
                    entry = new ClassFile.FieldRefEntry(clz, in.readUnsignedShort(), in.readUnsignedShort());
                    break;

                case ClassFile.CONSTANT_METHODREF:
                    entry = new ClassFile.MethodRefEntry(clz, in.readUnsignedShort(), in.readUnsignedShort());
                    break;

                case ClassFile.CONSTANT_INTERFACEMETHODREF:
                    entry = new ClassFile.InterfaceMethodRefEntry(clz, in.readUnsignedShort(), in.readUnsignedShort());
                    break;

                case ClassFile.CONSTANT_NAMEANDTYPE:
                    entry = new ClassFile.NameAndTypeEntry(clz, in.readUnsignedShort(), in.readUnsignedShort());
                    break;

                case ClassFile.CONSTANT_METHODHANDLE:
                    entry = new ClassFile.MethodHandleEntry(clz, in.readUnsignedByte(), in.readUnsignedShort());
                    break;

                case ClassFile.CONSTANT_METHODTYPE:
                    entry = new ClassFile.MethodTypeEntry(clz, in.readUnsignedShort());
                    break;

                case ClassFile.CONSTANT_DYNAMIC:
                    entry = new ClassFile.DynamicEntry(clz, in.readUnsignedShort(), in.readUnsignedShort());
                    break;

                case ClassFile.CONSTANT_INVOKEDYNAMIC:
                    entry = new ClassFile.InvokeDynamicEntry(clz, in.readUnsignedShort(), in.readUnsignedShort());
                    break;

                default:
                    throw new RuntimeException("Unknown constant pool entry tag: " + tag);
            }
            clz.constantPool.add(entry);
            if (entry.tag == ClassFile.CONSTANT_LONG || entry.tag == ClassFile.CONSTANT_DOUBLE) {
                clz.constantPool.add(null);
                i++;
            }
        }
    }

    static void readInterfaces(DataInputStream in, ClassFile clz) throws IOException {
        int interfacesCount = in.readUnsignedShort();
        for (int i = 0; i < interfacesCount; i++)
            clz.interfaces.add(in.readUnsignedShort());
    }

    static ClassFile.MemberInfo readMember(DataInputStream in, ClassFile clz) throws IOException {
        int accessFlag = in.readUnsignedShort();
        int nameIndex = in.readUnsignedShort();
        int descriptorIndex = in.readUnsignedShort();
        ClassFile.MemberInfo memberInfo = new ClassFile.MemberInfo(clz, accessFlag, nameIndex, descriptorIndex);
        readAttributes(in, clz, memberInfo.attributes);
        return memberInfo;
    }

    static void readMembers(DataInputStream in, ClassFile clz, List<ClassFile.MemberInfo> members) throws IOException {
        int fieldsCount = in.readUnsignedShort();
        for (int i = 0; i < fieldsCount; i++)
            members.add(readMember(in, clz));
    }

    static void readAttributes(DataInputStream in, ClassFile clz, List<ClassFile.AttributeInfo> infos) throws IOException {
        int attributesCount = in.readUnsignedShort();
        for (int i = 0; i < attributesCount; i++) {
            infos.add(readAttribute(in, clz));
        }
    }

    static ClassFile.AttributeInfo readAttribute(DataInputStream in, ClassFile clz) throws IOException {
        int attributeNameIndex = in.readUnsignedShort();
        int attributeLength = in.readInt();
        String attributeName = clz.getUtf8String(attributeNameIndex);
        return new ClassFile.AttributeInfo(clz, attributeNameIndex, attributeLength, in);
    }
}
